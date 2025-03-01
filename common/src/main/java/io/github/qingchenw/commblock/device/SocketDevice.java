package io.github.qingchenw.commblock.device;

import com.google.gson.JsonObject;

public class SocketDevice extends Device {
    public static final String TYPE = "TCP";

    public SocketDevice(String url) {
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
    public void connect() {

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

    public static SocketDevice create(JsonObject json) {
        return new SocketDevice(null);
    }
}
