package io.github.qingchenw.commblock.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class DeviceManagerScreen extends Screen {
    public DeviceManagerScreen() {
        super(GameNarrator.NO_TITLE);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, "设备管理器", this.width / 2, 20, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
