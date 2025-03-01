package io.github.qingchenw.commblock.device;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Device {
    public static final byte[] CRLF = new byte[] { 0x0D, 0x0A };
    protected List<DeviceEventListener> listeners = new ArrayList<>();

    abstract public String getType();
    abstract public String getName();

    abstract public void connect();
    abstract public void disconnect();
    abstract public boolean isConnected();
    abstract public int send(byte[] data);

    abstract public JsonObject toJson();
    abstract public void fromJson(JsonObject json);

    public void addListener(DeviceEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DeviceEventListener listener) {
        listeners.remove(listener);
    }

    public interface DeviceEventListener {
        default void onConnected() {}
        default void onDisconnected() {}
        default void onDataReceived(byte[] data) {}
        default void onError(Exception e) {}
    }
}
