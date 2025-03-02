package io.github.qingchenw.commblock.client.gui;

import dev.architectury.networking.NetworkManager;
import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.github.qingchenw.commblock.client.gui.widget.Dropdown;
import io.github.qingchenw.commblock.network.C2SSetCommBlockPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CommunicationBlockEditScreen extends Screen {
    private final CommunicationBlockEntity communicationBlock;
    private Dropdown<Pair<String, Boolean>> deviceList;
    private MultiLineEditBox dataEdit;
    private EditBox logEdit;
    private Button doneButton;
    private Button cancelButton;
    private List<Pair<String, Boolean>> devices = List.of();

    public CommunicationBlockEditScreen(CommunicationBlockEntity communicationBlock) {
        super(GameNarrator.NO_TITLE);
        this.communicationBlock = communicationBlock;
    }

    @Override
    protected void init() {
        this.deviceList = new Dropdown<>(this.width / 2 - 150, 50, 300, 20,
                devices, new DeviceListEntryAdapter(), 0, Component.translatable("communication.device"));
        this.addWidget(this.deviceList);
        this.dataEdit = new MultiLineEditBox(this.font, this.width / 2 - 150, 90, 300, 40,
                Component.empty(), Component.translatable("communication.data"));
        this.dataEdit.setCharacterLimit(65536);
        this.addWidget(this.dataEdit);
        this.logEdit = new EditBox(this.font, this.width / 2 - 150 + 1, 155, 300 - 2, 20,
                Component.translatable("communication.log"));
        this.logEdit.setMaxLength(10000);
        this.logEdit.setEditable(false);
        this.addWidget(this.logEdit);
        this.setInitialFocus(this.dataEdit);
        this.doneButton = Button.builder(CommonComponents.GUI_DONE, arg -> this.onDone())
                .bounds(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20).build();
        this.addRenderableWidget(this.doneButton);
        this.cancelButton = Button.builder(CommonComponents.GUI_CANCEL, arg -> this.onClose())
                .bounds(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20).build();
        this.addRenderableWidget(this.cancelButton);
    }

    @Override
    public void tick() {
        this.dataEdit.tick();
        if (this.communicationBlock.isRemoved()) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, Component.translatable("communication.setData"),
                this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("communication.device"),
                this.width / 2 - 150, 40, 10526880);
        this.deviceList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, Component.translatable("communication.data"),
                this.width / 2 - 150, 80, 10526880);
        this.dataEdit.render(guiGraphics, mouseX, mouseY, partialTick);
        int i = 95;
        if (!this.logEdit.getValue().isEmpty()) {
            i += 5 * 9 + 1;
            guiGraphics.drawString(this.font, Component.translatable("communication.log"),
                    this.width / 2 - 150, i + 4, 10526880);
            this.logEdit.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        int selected = this.deviceList.getSelectedIndex();
        String data = this.dataEdit.getValue();
        this.init(minecraft, width, height);
        this.deviceList.setSelectedIndex(selected);
        this.dataEdit.setValue(data);
        this.logEdit.setValue(this.communicationBlock.getLastOutput().getString());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == 257 || keyCode == 335) {
            this.onDone();
            return true;
        }
        return false;
    }

    public void updateGui() {
        this.deviceList.setSelection(Pair.of(this.communicationBlock.getDevice(), false));
        this.dataEdit.setValue(new String(this.communicationBlock.getData()));
        this.logEdit.setValue(this.communicationBlock.getLastOutput().getString());
    }

    public void updateDeviceList(List<Pair<String, Boolean>> devices) {
        this.devices = devices;
        this.deviceList.setOptions(this.devices);
        this.deviceList.setSelection(Pair.of(this.communicationBlock.getDevice(), false));
    }

    private void onDone() {
        FriendlyByteBuf buf = C2SSetCommBlockPacket.createPacket(
                this.communicationBlock.getBlockPos(),
                this.deviceList.getSelectedOption().orElse(Pair.of("", false)).getLeft(),
                this.dataEdit.getValue().getBytes());
        NetworkManager.sendToServer(C2SSetCommBlockPacket.ID, buf);
        this.minecraft.setScreen(null);
    }

    private static class DeviceListEntryAdapter extends Dropdown.ListEntryAdapter<Pair<String, Boolean>> {
        @Override
        public boolean areEntriesEqual(Pair<String, Boolean> entry1, Pair<String, Boolean> entry2) {
            if (entry1 == null || entry2 == null) {
                return entry1 == entry2;
            }
            return entry1.getLeft().equals(entry2.getLeft());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int x, int y, boolean isHovered, float partialTick) {
            Font font = Minecraft.getInstance().font;
            if (isHovered) {
                guiGraphics.fill(x, y, x + this.getWidth(), y + this.getHeight(), 0xFF404040);
            }
            Pair<String, Boolean> device = parent.getOption(index).orElse(Pair.of("", false));
            if (device.getLeft().isEmpty()) {
                return;
            }
            guiGraphics.drawString(font, device.getLeft(), x + 5, y + (this.getHeight() - 8) / 2, 0xFFFFFF);
            guiGraphics.fill(x + this.getWidth() - 30 - 3, y + this.getHeight() / 2 - 3,
                    x + this.getWidth() - 30 + 3, y + this.getHeight() / 2 + 3,
                    device.getRight() ? 0xFF00FF00 : 0xFFFF0000);
        }
    }
}
