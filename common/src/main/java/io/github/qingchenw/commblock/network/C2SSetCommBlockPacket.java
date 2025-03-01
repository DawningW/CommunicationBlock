package io.github.qingchenw.commblock.network;

import dev.architectury.networking.NetworkManager;
import io.github.qingchenw.commblock.CommBlockMod;
import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class C2SSetCommBlockPacket {
    public static final ResourceLocation ID = new ResourceLocation(CommBlockMod.MOD_ID, "set_comm_block");

    public static FriendlyByteBuf createPacket(BlockPos pos, String device, byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeUtf(device);
        buf.writeByteArray(data);
        return buf;
    }

    public static void receivePacket(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        final BlockPos pos = buf.readBlockPos();
        final String device = buf.readUtf();
        final byte[] data = buf.readByteArray();
        final Player player = context.getPlayer();
        context.queue(() -> {
            if (!player.canUseGameMasterBlocks()) {
                player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
            }
            if (player.level().getBlockEntity(pos) instanceof CommunicationBlockEntity communicationBlockEntity) {
                communicationBlockEntity.setDevice(device);
                communicationBlockEntity.setData(data);
                communicationBlockEntity.onUpdated();
                player.sendSystemMessage(Component.translatable("communication.setData.success"));
            }
        });
    }
}
