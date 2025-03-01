package io.github.qingchenw.commblock.network;

import dev.architectury.networking.NetworkManager;
import io.github.qingchenw.commblock.CommBlockMod;
import io.github.qingchenw.commblock.client.gui.CommunicationBlockEditScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class S2CDeviceListPacket {
    public static final ResourceLocation ID = new ResourceLocation(CommBlockMod.MOD_ID, "device_list");

    public static FriendlyByteBuf createPacket(List<Pair<String, Boolean>> devices) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeCollection(devices, (byteBuf, pair) -> {
            byteBuf.writeUtf(pair.getLeft());
            byteBuf.writeBoolean(pair.getRight());
        });
        return buf;
    }

    @Environment(EnvType.CLIENT)
    public static void receivePacket(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        final List<Pair<String, Boolean>> devices = buf.readCollection(ArrayList::new,
                byteBuf -> Pair.of(byteBuf.readUtf(), byteBuf.readBoolean()));
        context.queue(() -> {
            if (Minecraft.getInstance().screen instanceof CommunicationBlockEditScreen communicationBlockEditScreen) {
                communicationBlockEditScreen.updateDeviceList(devices);
            }
        });
    }
}
