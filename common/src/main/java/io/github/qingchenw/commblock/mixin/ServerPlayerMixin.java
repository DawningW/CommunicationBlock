package io.github.qingchenw.commblock.mixin;

import dev.architectury.networking.NetworkManager;
import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.github.qingchenw.commblock.device.DeviceManager;
import io.github.qingchenw.commblock.entity.PlayerExtension;
import io.github.qingchenw.commblock.network.S2CDeviceListPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerExtension {
    @Override
    public void openCommunicationBlock(CommunicationBlockEntity communicationBlockEntity) {
        ((ServerPlayer) (Object) this).connection.send(
                ClientboundBlockEntityDataPacket.create(communicationBlockEntity, BlockEntity::saveWithoutMetadata));
        List<Pair<String, Boolean>> devices = DeviceManager.get().getAllDevices().stream().map(device ->
                Pair.of(device.getName(), device.isConnected())).toList();
        NetworkManager.sendToPlayer((ServerPlayer) (Object) this, S2CDeviceListPacket.ID, S2CDeviceListPacket.createPacket(devices));
    }
}
