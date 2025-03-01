package io.github.qingchenw.commblock.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.github.qingchenw.commblock.client.gui.DeviceManagerScreen;
import io.github.qingchenw.commblock.network.S2CDeviceListPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class CommBlockClientMod {
    public static final KeyMapping DEVICE_MANAGER_KEY = new KeyMapping(
            "key.commblock.device_manager_key",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_O,
            "category.commblock.default"
    );
    private static final Component DEVICE_MANAGER_NOT_AVAILABLE = Component.translatable("multiplayer.deviceManager.not_available");

    public static void clientInit() {
        KeyMappingRegistry.register(DEVICE_MANAGER_KEY);

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CDeviceListPacket.ID, S2CDeviceListPacket::receivePacket);

        ClientTickEvent.CLIENT_POST.register(mc -> {
            while (DEVICE_MANAGER_KEY.consumeClick()) {
                if (mc.isLocalServer()) {
                    mc.setScreen(new DeviceManagerScreen());
                } else {
                    mc.player.displayClientMessage(DEVICE_MANAGER_NOT_AVAILABLE, true);
                }
            }
        });
    }
}
