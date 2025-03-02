package io.github.qingchenw.commblock.device;

import com.google.gson.JsonObject;

public class BleGattDevice extends Device {
    public static final String TYPE = "BLE_GATT";

    public BleGattDevice(String address) {
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public int send(byte[] data) {
        return 0;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject();
    }

    @Override
    public void fromJson(JsonObject json) {

    }

    public static BleGattDevice create(JsonObject json) {
        return new BleGattDevice(null);
    }
}
