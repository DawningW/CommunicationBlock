package io.github.qingchenw.commblock.device;

import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class WebSocketDevice extends Device {
    public static final String TYPE = "WebSocket";
    private final WebSocketClient client;

    public WebSocketDevice(String url) {
        try {
            client = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    listeners.forEach(DeviceEventListener::onConnected);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    listeners.forEach(DeviceEventListener::onDisconnected);
                }

                @Override
                public void onMessage(String message) {
                    listeners.forEach(listener -> listener.onDataReceived(message.getBytes()));
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    listeners.forEach(listener -> {
                        byte[] data = new byte[bytes.remaining()];
                        bytes.get(data);
                        listener.onDataReceived(data);
                    });
                }

                @Override
                public void onError(Exception ex) {
                    listeners.forEach(listener -> listener.onError(ex));
                }
            };
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return client.getURI().toString();
    }

    @Override
    public boolean connect() {
        try {
            return client.connectBlocking();
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public void disconnect() {
        client.close();
    }

    @Override
    public boolean isConnected() {
        return client.isOpen();
    }

    @Override
    public int send(byte[] data) {
        client.send(data);
        return data.length;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("url", getName());
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {

    }

    public static WebSocketDevice create(JsonObject json) {
        String url = json.get("url").getAsString();
        return new WebSocketDevice(url);
    }
}
