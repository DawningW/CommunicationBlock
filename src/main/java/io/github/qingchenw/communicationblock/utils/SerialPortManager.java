package io.github.qingchenw.communicationblock.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import io.github.qingchenw.communicationblock.SerialPortMod;

/**
 * 串口管理
 * 
 * @author yangle
 * @author QingChenW
 */
public class SerialPortManager
{
	public static final int DEFAULT_BAUDRATE = 9600;
	
	public static Map<String, NRSerialPort> portCacheMap = new HashMap();

	public static final List<String> findPorts()
	{
		// 获得当前所有可用串口
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		List<String> portNameList = new ArrayList<String>();
		// 将可用串口名添加到List并返回该List
		while (portList.hasMoreElements())
		{
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		SerialPortMod.logger.info("Available serial ports: " + portNameList.toString());
		return portNameList;
	}

	public static NRSerialPort getPort(String port)
	{
		if (!portCacheMap.containsKey(port))
		{
			NRSerialPort serial = new NRSerialPort(port, DEFAULT_BAUDRATE);
			if (!serial.connect())
			{
				SerialPortMod.logger.error("Can't connect to the serial: " + port);
				return null;
			}
			portCacheMap.put(port, serial);
			SerialPortMod.logger.info("Connect to serial port: " + port);
		}
		NRSerialPort serial = portCacheMap.get(port);
		if (!serial.isConnected())
		{
			portCacheMap.remove(port);
			SerialPortMod.logger.error("This serial has been disconnected: " + port);
			return null;
		}
		return serial;
	}
	
	public static void disconnectAll()
	{
		for (NRSerialPort serial : portCacheMap.values())
		{
			serial.disconnect();
		}
		portCacheMap.clear();
		SerialPortMod.logger.info("Disconnect all serial ports");
	}

	public static void addListener(SerialPort serialPort, DataAvailableListener listener)
	{
		try
		{
			// 给串口添加监听器
			serialPort.addEventListener(new SerialPortListener(listener));
			// 设置当有数据到达时唤醒监听接收线程
			serialPort.notifyOnDataAvailable(true);
			// 设置当通信中断时唤醒中断线程
			serialPort.notifyOnBreakInterrupt(true);
		}
		catch (TooManyListenersException e)
		{
			e.printStackTrace();
		}
	}

	public static class SerialPortListener implements SerialPortEventListener
	{
		private DataAvailableListener mDataAvailableListener;

		public SerialPortListener(DataAvailableListener mDataAvailableListener)
		{
			this.mDataAvailableListener = mDataAvailableListener;
		}

		@Override
		public void serialEvent(SerialPortEvent serialPortEvent)
		{
			switch (serialPortEvent.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE: // 1.串口存在有效数据
				if (mDataAvailableListener != null)
				{
					mDataAvailableListener.dataAvailable((SerialPort) serialPortEvent.getSource());
				}
				break;

			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2.输出缓冲区已清空
				break;

			case SerialPortEvent.CTS: // 3.清除待发送数据
				break;

			case SerialPortEvent.DSR: // 4.待发送数据准备好了
				break;

			case SerialPortEvent.RI: // 5.振铃指示
				break;

			case SerialPortEvent.CD: // 6.载波检测
				break;

			case SerialPortEvent.OE: // 7.溢位（溢出）错误
				break;

			case SerialPortEvent.PE: // 8.奇偶校验错误
				break;

			case SerialPortEvent.FE: // 9.帧错误
				break;

			case SerialPortEvent.BI: // 10.通讯中断
				System.out.println("与串口设备通讯中断");
				break;

			default:
				break;
			}
		}
	}

	public interface DataAvailableListener
	{
		void dataAvailable(SerialPort serial);
	}
	
	public static class SerialPortDataListener implements DataAvailableListener
	{
		@Override
		public void dataAvailable(SerialPort serial)
		{
			try
			{
				DataInputStream ins = new DataInputStream(serial.getInputStream());
				int b = ins.read();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
