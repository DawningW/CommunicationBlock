package io.github.qingchenw.commblock.mixin;

import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.github.qingchenw.commblock.client.gui.CommunicationBlockEditScreen;
import io.github.qingchenw.commblock.entity.PlayerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin implements PlayerExtension {
    @Override
    public void openCommunicationBlock(CommunicationBlockEntity communicationBlockEntity) {
        Minecraft.getInstance().setScreen(new CommunicationBlockEditScreen(communicationBlockEntity));
    }
}
