package io.github.qingchenw.commblock.client.gui;

import io.github.qingchenw.commblock.device.Device;
import io.github.qingchenw.commblock.device.DeviceManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DeviceManagerScreen extends Screen implements DeviceManager.DeviceManagerEventListener {
    private DeviceList deviceList;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button discoverButton;

    public DeviceManagerScreen() {
        super(GameNarrator.NO_TITLE);
        DeviceManager.get().addListener(this);
    }

    @Override
    protected void init() {
        this.deviceList = new DeviceList(this.minecraft, this.width, this.height, 32, this.height - 64, 20);
        this.addRenderableWidget(this.deviceList);

        this.discoverButton = Button.builder(Component.translatable("gui.commblock.refresh"), button -> {
            this.refreshDeviceList();
        }).bounds(this.width - 100, 15, 40, 15).build();
        this.addRenderableWidget(this.discoverButton);

        int buttonWidth = 80;
        int spacing = 4;
        int totalWidth = buttonWidth * 4 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 52;

        this.addButton = Button.builder(Component.translatable("gui.commblock.add"), button -> {
            this.minecraft.setScreen(new AddDeviceScreen(this, null));
        }).bounds(startX, y, buttonWidth, 20).build();
        this.addRenderableWidget(this.addButton);

        this.editButton = Button.builder(Component.translatable("gui.commblock.edit"), button -> {
            DeviceEntry entry = this.deviceList.getSelected();
            if (entry != null) {
                this.minecraft.setScreen(new AddDeviceScreen(this, entry.device));
            }
        }).bounds(startX + buttonWidth + spacing, y, buttonWidth, 20).build();
        this.addRenderableWidget(this.editButton);

        this.deleteButton = Button.builder(Component.translatable("gui.commblock.delete"), button -> {
            DeviceEntry entry = this.deviceList.getSelected();
            if (entry != null) {
                DeviceManager.get().removeDevice(entry.device.getName());
            }
        }).bounds(startX + (buttonWidth + spacing) * 2, y, buttonWidth, 20).build();
        this.addRenderableWidget(this.deleteButton);

        Button backButton = Button.builder(Component.translatable("gui.back"), button -> {
            this.onClose();
        }).bounds(startX + (buttonWidth + spacing) * 3, y, buttonWidth, 20).build();
        this.addRenderableWidget(backButton);

        this.updateButtonStates();
        this.refreshDeviceList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.commblock.device_manager"),
                this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        DeviceManager.get().removeListener(this);
        super.onClose();
    }

    private void updateButtonStates() {
        DeviceEntry selected = this.deviceList.getSelected();
        boolean hasSelection = selected != null && !selected.discovered;
        this.editButton.active = hasSelection;
        this.deleteButton.active = hasSelection;
    }

    private void refreshDeviceList() {
        this.deviceList.clearEntries();
        DeviceManager.get().getAllDevices().forEach(device -> this.deviceList.addEntry(new DeviceEntry(device)));
        DeviceManager.get().startDiscovery();
    }

    @Override
    public void onDeviceAdded(Device device) {
        this.deviceList.addEntry(new DeviceEntry(device));
    }

    @Override
    public void onDeviceRemoved(Device device) {
        this.deviceList.children().removeIf(entry -> entry.device.equals(device));
        if (this.deviceList.getSelected() != null && device.equals(this.deviceList.getSelected().device)) {
            this.deviceList.setSelected(null);
            updateButtonStates();
        }
    }

    @Override
    public void onDeviceDiscovered(String type, String name) {
        Device device = DeviceManager.get().createDevice(type, name);
        if (device != null) {
            this.deviceList.addEntry(new DeviceEntry(device, true));
        }
    }

    @Environment(EnvType.CLIENT)
    private class DeviceList extends ObjectSelectionList<DeviceEntry> {
        public DeviceList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
            this.setRenderBackground(false);
        }

        @Override
        protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
            int left = this.getRowLeft();
            guiGraphics.fill(left - 2, top - 2, left + width + 2, top + height + 2, outerColor);
            guiGraphics.fill(left, top, left + width, top + height, innerColor);
        }

        @Override
        public int addEntry(DeviceEntry entry) {
            return super.addEntry(entry);
        }

        @Override
        public boolean removeEntry(DeviceEntry entry) {
            return super.removeEntry(entry);
        }

        @Override
        public void clearEntries() {
            super.clearEntries();
        }
    }

    @Environment(EnvType.CLIENT)
    private class DeviceEntry extends ObjectSelectionList.Entry<DeviceEntry> {
        private final Device device;
        private final boolean discovered;
        private final Button actionButton;

        public DeviceEntry(Device device, boolean discovered) {
            this.device = device;
            this.discovered = discovered;
            if (!discovered) {
                this.actionButton = Button.builder(Component.empty(), button -> {
                    if (device.isConnected()) {
                        this.device.disconnect();
                    } else {
                        this.device.connect();
                    }
                    updateConnectButton();
                }).bounds(0, 0, 30, 16).build();
                updateConnectButton();
            } else {
                this.actionButton = Button.builder(Component.translatable("gui.commblock.add"), button -> {
                    DeviceManagerScreen.this.minecraft.setScreen(new AddDeviceScreen(DeviceManagerScreen.this, device));
                }).bounds(0, 0, 30, 16).build();
            }
        }

        public DeviceEntry(Device device) {
            this(device, false);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isHovered, float partialTick) {
            String text = this.device.getName() + " - " + this.device.getType();
            int textColor = this.discovered ? 0xFF808080 : 0xFFFFFF;
            guiGraphics.drawString(font, text, left + 3, top + (height - font.lineHeight) / 2, textColor);

            if (!discovered) {
                boolean connected = this.device.isConnected();
                int statusColor = connected ? 0xFF00FF00 : 0xFFFF0000;
                guiGraphics.fill(left + width - this.actionButton.getWidth() - 15, top + height / 2 - 3,
                        left + width - this.actionButton.getWidth() - 9, top + height / 2 + 3, statusColor);
            }

            if (this.actionButton != null) {
                this.actionButton.setX(left + width - this.actionButton.getWidth() - 5);
                this.actionButton.setY(top);
                this.actionButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        private void updateConnectButton() {
            if (this.actionButton != null) {
                boolean connected = this.device.isConnected();
                Component message = Component.translatable(connected ? "gui.commblock.disconnect" : "gui.commblock.connect");
                this.actionButton.setMessage(message);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.actionButton != null && this.actionButton.isMouseOver(mouseX, mouseY)) {
                this.actionButton.onClick(mouseX, mouseY);
                return true;
            }
            DeviceManagerScreen.this.deviceList.setSelected(this);
            DeviceManagerScreen.this.updateButtonStates();
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.device.getName());
        }
    }
}
