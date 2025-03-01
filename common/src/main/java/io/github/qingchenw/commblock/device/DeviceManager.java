package io.github.qingchenw.commblock.device;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.qingchenw.commblock.CommBlockMod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;

public final class DeviceManager implements Device.DeviceEventListener {
    private static final DeviceManager INSTANCE = new DeviceManager();
    private final Map<String, Pair<Function<String, ? extends Device>, DeviceFactory<? extends Device>>> factoryMap =
            new HashMap<>();
    private final Map<String, Device> devices = new LinkedHashMap<>();
    private final List<DeviceManagerEventListener> listeners = new ArrayList<>();

    {
        registerDevice(SerialDevice.TYPE, SerialDevice::new, SerialDevice::create);
//        registerDevice(SocketDevice.TYPE, SocketDevice::new, SocketDevice::create);
        registerDevice(WebSocketDevice.TYPE, WebSocketDevice::new, WebSocketDevice::create);
//        registerDevice(BtSppDevice.TYPE, BtSppDevice::new, BtSppDevice::create);
//        registerDevice(BleGattDevice.TYPE, BleGattDevice::new, BleGattDevice::create);
//        registerDevice(SleDevice.TYPE, SleDevice::new, SleDevice::create);
    }

    private DeviceManager() {}

    public void addDevice(Device device) {
        devices.put(device.getName(), device);
        device.addListener(this);
        listeners.forEach(listener -> listener.onDeviceAdded(device));
        CommBlockMod.saveConfig(saveToJson());
    }

    public void removeDevice(String name) {
        Device device = devices.get(name);
        if (device != null) {
            device.removeListener(this);
            device.disconnect();
            listeners.forEach(listener -> listener.onDeviceRemoved(device));
        }
        devices.remove(name);
        CommBlockMod.saveConfig(saveToJson());
    }

    public Optional<Device> getDevice(String name) {
        return Optional.ofNullable(devices.get(name));
    }

    public List<Device> getAllDevices() {
        return new ArrayList<>(devices.values());
    }

    public void startDiscovery() {
        Arrays.stream(SerialDevice.getAvailablePorts())
                .forEach(port -> listeners.forEach(
                        listener -> listener.onDeviceDiscovered(SerialDevice.TYPE, port)));
    }

    public void addListener(DeviceManagerEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DeviceManagerEventListener listener) {
        listeners.remove(listener);
    }

    public <T extends Device> void registerDevice(String type, Function<String, T> newFactory, DeviceFactory<T> jsonFactory) {
        if (factoryMap.containsKey(type)) {
            throw new IllegalArgumentException("Device type already registered: " + type);
        }
        factoryMap.put(type, Pair.of(newFactory, jsonFactory));
    }

    public List<String> getDeviceTypes() {
        return new ArrayList<>(factoryMap.keySet());
    }

    public Device createDevice(String type, String id) {
        if (!factoryMap.containsKey(type)) {
            return null;
        }
        return factoryMap.get(type).getLeft().apply(id);
    }

    public Device createDevice(String type, JsonObject json) {
        if (!factoryMap.containsKey(type)) {
            return null;
        }
        return factoryMap.get(type).getRight().create(json);
    }

    public void loadFromJson(JsonArray json) {
        devices.forEach((name, device) -> {
            device.removeListener(this);
            device.disconnect();
            listeners.forEach(listener -> listener.onDeviceRemoved(device));
        });
        devices.clear();
        for (int i = 0; i < json.size(); i++) {
            JsonObject deviceJson = json.get(i).getAsJsonObject();
            String type = deviceJson.get("type").getAsString();
            Device device = createDevice(type, deviceJson);
            if (device == null) {
                CommBlockMod.LOGGER.warn("Unknown device: {}", deviceJson);
                continue;
            }
            if (deviceJson.get("connected").getAsBoolean()) {
                device.connect();
            }
            devices.put(device.getName(), device);
            device.addListener(this);
            listeners.forEach(listener -> listener.onDeviceAdded(device));
        }
    }

    public JsonArray saveToJson() {
        JsonArray json = new JsonArray();
        for (Device device : devices.values()) {
            JsonObject deviceJson = device.toJson();
            deviceJson.addProperty("type", device.getType());
            deviceJson.addProperty("connected", device.isConnected());
            json.add(deviceJson);
        }
        return json;
    }

    @Override
    public void onConnected() {
        CommBlockMod.saveConfig(saveToJson());
    }

    @Override
    public void onDisconnected() {
        CommBlockMod.saveConfig(saveToJson());
    }

    public static DeviceManager get() {
        return INSTANCE;
    }

    public interface DeviceFactory<T extends Device> {
        T create(JsonObject json);
    }

    public interface DeviceManagerEventListener {
        default void onDeviceAdded(Device device) {}
        default void onDeviceRemoved(Device device) {}
        default void onDeviceDiscovered(String type, String name) {}
    }
}
