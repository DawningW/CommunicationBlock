package io.github.qingchenw.commblock.device;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class SerialDevice extends Device {
    public static final String TYPE = "Serial";
    private final SerialPort serialPort;
    private byte[] messageDelimiter = CRLF;

    public SerialDevice(String port) {
        serialPort = SerialPort.getCommPort(port);
    }

    public int getBaudRate() {
        return serialPort.getBaudRate();
    }

    public void setBaudRate(int baudRate) {
        serialPort.setBaudRate(baudRate);
    }

    public int getDataBits() {
        return serialPort.getNumDataBits();
    }

    public void setDataBits(int numDataBits) {
        serialPort.setNumDataBits(numDataBits);
    }

    public int getStopBits() {
        return serialPort.getNumStopBits();
    }

    public void setStopBits(int numStopBits) {
        serialPort.setNumStopBits(numStopBits);
    }

    public int getParity() {
        return serialPort.getParity();
    }

    public void setParity(int parity) {
        serialPort.setParity(parity);
    }

    public byte[] getMessageDelimiter() {
        return messageDelimiter;
    }

    public void setMessageDelimiter(byte[] delimiter) {
        this.messageDelimiter = delimiter;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return serialPort.getSystemPortName();
    }

    @Override
    public boolean connect() {
        serialPort.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {
                return messageDelimiter;
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    listeners.forEach(DeviceEventListener::onDisconnected);
                } else if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                    byte[] message = event.getReceivedData();
                    listeners.forEach(listener -> listener.onDataReceived(message));
                }
            }
        });
        boolean result = serialPort.openPort();
        if (result) {
            listeners.forEach(DeviceEventListener::onConnected);
        }
        return result;
    }

    @Override
    public void disconnect() {
        serialPort.closePort();
        serialPort.removeDataListener();
        listeners.forEach(DeviceEventListener::onDisconnected);
    }

    @Override
    public boolean isConnected() {
        return serialPort.isOpen();
    }

    @Override
    public int send(byte[] data) {
        int ret = serialPort.writeBytes(data, data.length);
        if (ret < 0) {
            return ret;
        }
        serialPort.writeBytes(messageDelimiter, messageDelimiter.length);
        return ret;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("port", getName());
        json.addProperty("baudRate", getBaudRate());
        json.addProperty("dataBits", getDataBits());
        json.addProperty("stopBits", getStopBits());
        json.addProperty("parity", getParity());
        json.addProperty("delimiter", new String(getMessageDelimiter()));
        return json;
    }

    public void fromJson(JsonObject json) {
        if (json.has("baudRate")) {
            setBaudRate(json.get("baudRate").getAsInt());
        }
        if (json.has("dataBits")) {
            setDataBits(json.get("dataBits").getAsInt());
        }
        if (json.has("stopBits")) {
            setStopBits(json.get("stopBits").getAsInt());
        }
        if (json.has("parity")) {
            setParity(json.get("parity").getAsInt());
        }
        if (json.has("delimiter")) {
            setMessageDelimiter(json.get("delimiter").getAsString().getBytes());
        }
    }

    public static SerialDevice create(JsonObject json) {
        String port = json.get("port").getAsString();
        SerialDevice device = new SerialDevice(port);
        device.fromJson(json);
        return device;
    }

    public static String[] getAvailablePorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toArray(String[]::new);
    }
}
