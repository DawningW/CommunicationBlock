package io.github.qingchenw.commblock.client.gui;

import io.github.qingchenw.commblock.CommBlockMod;
import io.github.qingchenw.commblock.device.Device;
import io.github.qingchenw.commblock.device.DeviceManager;
import io.github.qingchenw.commblock.device.SerialDevice;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class AddDeviceScreen extends Screen {
    private static final int PADDING = 8;
    private static final int TAB_WIDTH = 120;
    private static final int LABEL_WIDTH = 80;
    private static final int FIELD_HEIGHT = 15;

    private final Screen parent;
    private final Device editDevice;
    private final List<String> deviceTypes;
    private final List<TabButton> tabButtons;
    private EditBox nameEdit;
    private EditBox baudRateEdit;
    private EditBox dataBitsEdit;
    private EditBox stopBitsEdit;
    private EditBox parityEdit;
    private EditBox delimiterEdit;
    private Button confirmButton;
    private Button cancelButton;
    private int selectedTab;

    public AddDeviceScreen(Screen parent, Device device) {
        super(GameNarrator.NO_TITLE);
        this.parent = parent;
        this.editDevice = device;
        this.deviceTypes = DeviceManager.get().getDeviceTypes();
        this.tabButtons = new ArrayList<>();
        this.selectedTab = device != null ? this.deviceTypes.indexOf(device.getType()) : 0;
    }

    @Override
    protected void init() {
        this.tabButtons.clear();
        int tabX = (this.width - TAB_WIDTH * this.deviceTypes.size()) / 2;
        int tabY = 40;

        for (int i = 0; i < this.deviceTypes.size(); i++) {
            String type = this.deviceTypes.get(i);
            TabButton button = new TabButton(
                    tabX + i * TAB_WIDTH, tabY, TAB_WIDTH, 20,
                    Component.literal(type), this.font,
                    this.selectedTab == i
            ) {
                @Override
                public void onPress() {
                    selectTab(this);
                }
            };
            button.active = this.editDevice == null;
            this.tabButtons.add(button);
            this.addRenderableWidget(button);
        }

        int y = tabY + 30;
        int x = this.width / 2 - 100;
        int fieldWidth = 200;

        this.nameEdit = new EditBox(this.font, x, y, fieldWidth, FIELD_HEIGHT,
                Component.translatable("gui.commblock.device_name"));
        if (this.editDevice != null) {
            this.nameEdit.setValue(this.editDevice.getName());
            this.nameEdit.setEditable(false);
        }
        this.addRenderableWidget(this.nameEdit);

        y += FIELD_HEIGHT + PADDING;

        if (this.deviceTypes.get(this.selectedTab).equals(SerialDevice.TYPE)) {
            SerialDevice serialDevice = this.editDevice instanceof SerialDevice ?
                    (SerialDevice) this.editDevice : null;

            this.baudRateEdit = new EditBox(this.font, x, y, fieldWidth, FIELD_HEIGHT,
                    Component.translatable("gui.commblock.baud_rate"));
            this.baudRateEdit.setValue(serialDevice != null ?
                    String.valueOf(serialDevice.getBaudRate()) : "9600");
            this.addWidget(this.baudRateEdit);

            y += FIELD_HEIGHT + PADDING;

            this.dataBitsEdit = new EditBox(this.font, x, y, fieldWidth, FIELD_HEIGHT,
                    Component.translatable("gui.commblock.data_bits"));
            this.dataBitsEdit.setValue(serialDevice != null ?
                    String.valueOf(serialDevice.getDataBits()) : "8");
            this.addWidget(this.dataBitsEdit);

            y += FIELD_HEIGHT + PADDING;

            this.stopBitsEdit = new EditBox(this.font, x, y, fieldWidth, FIELD_HEIGHT,
                    Component.translatable("gui.commblock.stop_bits"));
            this.stopBitsEdit.setValue(serialDevice != null ?
                    String.valueOf(serialDevice.getStopBits()) : "1");
            this.addWidget(this.stopBitsEdit);

            y += FIELD_HEIGHT + PADDING;

            this.parityEdit = new EditBox(this.font, x, y, fieldWidth, FIELD_HEIGHT,
                    Component.translatable("gui.commblock.parity"));
            this.parityEdit.setValue(serialDevice != null ?
                    String.valueOf(serialDevice.getParity()) : "0");
            this.addWidget(this.parityEdit);

            y += FIELD_HEIGHT + PADDING;

            this.delimiterEdit = new EditBox(this.font, x, y, fieldWidth, FIELD_HEIGHT,
                    Component.translatable("gui.commblock.delimiter"));
            this.delimiterEdit.setValue(
                    new String(serialDevice != null ? serialDevice.getMessageDelimiter() : Device.CRLF));
            this.addWidget(this.delimiterEdit);
        }

        y = this.height - 40;

        int buttonWidth = 100;
        int spacing = 4;
        int startX = (this.width - (buttonWidth * 2 + spacing)) / 2;

        this.confirmButton = Button.builder(Component.translatable("gui.done"), button -> this.onDone())
                .bounds(startX, y, buttonWidth, 20).build();
        this.addRenderableWidget(this.confirmButton);

        this.cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .bounds(startX + buttonWidth + spacing, y, buttonWidth, 20).build();
        this.addRenderableWidget(this.cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        Component title = Component.translatable(this.editDevice != null ?
                "gui.commblock.edit_device" : "gui.commblock.add_device");
        guiGraphics.drawCenteredString(this.font, title, this.width / 2, 20, 0xFFFFFF);

        int x = this.width / 2 - 100 - LABEL_WIDTH - PADDING;
        int y = this.nameEdit.getY() + 4;
        guiGraphics.drawString(this.font, Component.translatable("gui.commblock.device_name"),
                x, y, 0xFFFFFF);

        if (this.deviceTypes.get(this.selectedTab).equals(SerialDevice.TYPE)) {
            y = this.baudRateEdit.getY() + 4;
            guiGraphics.drawString(this.font, Component.translatable("gui.commblock.baud_rate"),
                    x, y, 0xFFFFFF);
            this.baudRateEdit.render(guiGraphics, mouseX, mouseY, partialTick);

            y = this.dataBitsEdit.getY() + 4;
            guiGraphics.drawString(this.font, Component.translatable("gui.commblock.data_bits"),
                    x, y, 0xFFFFFF);
            this.dataBitsEdit.render(guiGraphics, mouseX, mouseY, partialTick);

            y = this.stopBitsEdit.getY() + 4;
            guiGraphics.drawString(this.font, Component.translatable("gui.commblock.stop_bits"),
                    x, y, 0xFFFFFF);
            this.stopBitsEdit.render(guiGraphics, mouseX, mouseY, partialTick);

            y = this.parityEdit.getY() + 4;
            guiGraphics.drawString(this.font, Component.translatable("gui.commblock.parity"),
                    x, y, 0xFFFFFF);
            this.parityEdit.render(guiGraphics, mouseX, mouseY, partialTick);

            y = this.delimiterEdit.getY() + 4;
            guiGraphics.drawString(this.font, Component.translatable("gui.commblock.delimiter"),
                    x, y, 0xFFFFFF);
            this.delimiterEdit.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void selectTab(TabButton selected) {
        for (TabButton button : this.tabButtons) {
            button.setSelected(button == selected);
        }
        this.selectedTab = this.tabButtons.indexOf(selected);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void onDone() {
        String name = this.nameEdit.getValue().trim();
        String type = this.deviceTypes.get(this.selectedTab);
        if (!name.isEmpty()) {
            Device newDevice = this.editDevice != null ? this.editDevice :
                    DeviceManager.get().createDevice(type, name);

            if (type.equals(SerialDevice.TYPE) && newDevice instanceof SerialDevice serialDevice) {
                serialDevice.setBaudRate(Integer.parseInt(this.baudRateEdit.getValue()));
                serialDevice.setDataBits(Integer.parseInt(this.dataBitsEdit.getValue()));
                serialDevice.setStopBits(Integer.parseInt(this.stopBitsEdit.getValue()));
                serialDevice.setParity(Integer.parseInt(this.parityEdit.getValue()));
                serialDevice.setMessageDelimiter(this.delimiterEdit.getValue().getBytes());
            }

            DeviceManager.get().addDevice(newDevice);
            if (this.editDevice != null) {
                CommBlockMod.saveConfig(DeviceManager.get().saveToJson());
            }
        }
        this.onClose();
    }

    @Environment(EnvType.CLIENT)
    private static class TabButton extends Button {
        private boolean selected;

        public TabButton(int x, int y, int width, int height, Component message, Font font, boolean selected) {
            super(x, y, width, height, message, button -> {}, DEFAULT_NARRATION);
            this.selected = selected;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
                    this.selected ? 0xFF808080 : 0xFF404040);
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                    this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
