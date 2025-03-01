package io.github.qingchenw.commblock.blockentity;

import io.github.qingchenw.commblock.device.Device;
import io.github.qingchenw.commblock.device.DeviceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static io.github.qingchenw.commblock.CommBlockMod.COMMUNICATION_BLOCK_ENTITY;

public class CommunicationBlockEntity extends BlockEntity implements Nameable, DeviceManager.DeviceManagerEventListener, Device.DeviceEventListener {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Component DEFAULT_NAME = Component.literal("@");
    private Component name = DEFAULT_NAME;
    private String device = "";
    private byte[] data = new byte[0];
    private int successCount;
    private long lastExecution = -1L;
    private Component lastOutput;
    private boolean powered;

    public CommunicationBlockEntity(BlockPos pos, BlockState blockState) {
        super(COMMUNICATION_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!this.level.isClientSide) {
            DeviceManager.get().addListener(this);
            DeviceManager.get().getDevice(this.device)
                    .ifPresent(device -> device.addListener(this));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!this.level.isClientSide) {
            DeviceManager.get().removeListener(this);
            DeviceManager.get().getDevice(this.device)
                    .ifPresent(device -> device.removeListener(this));
        }
    }

    @Override
    public Component getName() {
        return this.name;
    }

    public void setName(Component name) {
        this.name = name != null ? name : DEFAULT_NAME;
    }

    public String getDevice() {
        return this.device;
    }

    public void setDevice(String device) {
        this.device = device;
        this.setChanged();
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.successCount = 0;
        this.setChanged();
    }

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public Component getLastOutput() {
        return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
    }

    public void setLastOutput(Component message) {
        this.lastOutput = message;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public boolean sendData(Level level) {
        if (level.isClientSide || level.getGameTime() == this.lastExecution) {
            return false;
        }
        this.successCount = 0;
        if (!StringUtil.isNullOrEmpty(this.device) && this.data.length > 0) {
            Optional<Device> device = DeviceManager.get().getDevice(this.device);
            if (device.isPresent()) {
                if (device.get().isConnected()) {
                    int result = device.get().send(this.data);
                    if (result > 0) {
                        this.successCount++;
                        this.sendLogMessage(Component.translatable("communication.send.success", result, this.device));
                    } else {
                        this.sendLogMessage(Component.translatable("communication.send.failure", this.device));
                    }
                } else {
                    this.sendLogMessage(Component.translatable("communication.device.disconnected", this.device));
                }
            } else {
                this.sendLogMessage(Component.translatable("communication.device.unknown", this.device));
            }
        }
        this.lastExecution = level.getGameTime();
        return true;
    }

    protected void sendLogMessage(Component component) {
        this.lastOutput = Component.literal("[" + TIME_FORMAT.format(new Date()) + "] ").append(component);
        this.onUpdated();
    }

    public void onUpdated() {
        BlockState blockState = this.level.getBlockState(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, 3);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("CustomName", Component.Serializer.toJson(this.name));
        tag.putString("Device", this.device);
        tag.putByteArray("Data", this.data);
        tag.putInt("SuccessCount", this.successCount);
        if (this.lastExecution > 0L) {
            tag.putLong("LastExecution", this.lastExecution);
        }
        if (this.lastOutput != null) {
            tag.putString("LastOutput", Component.Serializer.toJson(this.lastOutput));
        }
        tag.putBoolean("powered", this.powered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CustomName", 8)) {
            this.setName(Component.Serializer.fromJson(tag.getString("CustomName")));
        }
        this.device = tag.getString("Device");
        this.data = tag.getByteArray("Data");
        this.successCount = tag.getInt("SuccessCount");
        if (tag.contains("LastExecution")) {
            this.lastExecution = tag.getLong("LastExecution");
        } else {
            this.lastExecution = -1L;
        }
        if (tag.contains("LastOutput", 8)) {
            this.lastOutput = Component.Serializer.fromJson(tag.getString("LastOutput"));
        } else {
            this.lastOutput = null;
        }
        this.powered = tag.getBoolean("powered");
    }

    @Override
    public void onDeviceAdded(Device device) {
        if (device.getName().equals(this.device)) {
            device.addListener(this);
        }
    }

    @Override
    public void onDeviceRemoved(Device device) {
        if (device.getName().equals(this.device)) {
            device.removeListener(this);
        }
    }

    @Override
    public void onDataReceived(byte[] data) {

    }
}
