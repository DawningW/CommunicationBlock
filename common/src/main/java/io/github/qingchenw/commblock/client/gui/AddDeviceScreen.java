package io.github.qingchenw.commblock.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class AddDeviceScreen extends Screen {
    protected AddDeviceScreen() {
        super(GameNarrator.NO_TITLE);
    }
}
