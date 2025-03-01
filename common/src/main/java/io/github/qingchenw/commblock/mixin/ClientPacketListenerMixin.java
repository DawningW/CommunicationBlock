package io.github.qingchenw.commblock.mixin;

import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.github.qingchenw.commblock.client.gui.CommunicationBlockEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "method_38542", at = @At("TAIL"))
    private void injectMethod(ClientboundBlockEntityDataPacket packet, BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof CommunicationBlockEntity &&
                Minecraft.getInstance().screen instanceof CommunicationBlockEditScreen communicationBlockEditScreen) {
            communicationBlockEditScreen.updateGui();
        }
    }
}
