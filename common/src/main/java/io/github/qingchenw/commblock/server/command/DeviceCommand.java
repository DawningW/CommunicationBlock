package io.github.qingchenw.commblock.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.qingchenw.commblock.device.Device;
import io.github.qingchenw.commblock.device.DeviceManager;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceCommand {
    private static final List<Pair<String, String>> discoveryResults = new ArrayList<>();
    private static final DeviceManager.DeviceManagerEventListener listener = new DeviceManager.DeviceManagerEventListener() {
        @Override
        public void onDeviceDiscovered(String type, String name) {
            discoveryResults.add(Pair.of(type, name));
        }
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        DeviceManager.get().addListener(listener);
        dispatcher.register(Commands.literal("device")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .executes(ctx -> showList(ctx.getSource()))
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("type", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getDeviceTypes(),
                                                builder
                                        ))
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(ctx ->
                                                addDevice(ctx.getSource(),
                                                        ctx.getArgument("type", String.class),
                                                        ctx.getArgument("name", String.class)))
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getAllDevices().stream().map(Device::getName),
                                                builder
                                        ))
                                .executes(ctx ->
                                        removeDevice(ctx.getSource(),
                                                ctx.getArgument("name", String.class)))
                        )
                )
                .then(Commands.literal("connect")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getAllDevices().stream().map(Device::getName),
                                                builder
                                        ))
                                .executes(ctx ->
                                        connectDevice(ctx.getSource(),
                                                ctx.getArgument("name", String.class)))
                        )
                )
                .then(Commands.literal("disconnect")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getAllDevices().stream().map(Device::getName),
                                                builder
                                        ))
                                .executes(ctx ->
                                        disconnectDevice(ctx.getSource(),
                                                ctx.getArgument("name", String.class)))
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getAllDevices().stream().map(Device::getName),
                                                builder
                                        ))
                                .executes(ctx ->
                                        getDeviceAttr(ctx.getSource(),
                                                ctx.getArgument("name", String.class)))
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getAllDevices().stream().map(Device::getName),
                                                builder
                                        ))
                                .then(Commands.argument("attr", StringArgumentType.string())
                                        .executes(ctx ->
                                                setDeviceAttr(ctx.getSource(),
                                                        ctx.getArgument("name", String.class),
                                                        ctx.getArgument("attr", String.class)))
                                )
                        )
                )
                .then(Commands.literal("send")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                DeviceManager.get().getAllDevices().stream().map(Device::getName),
                                                builder
                                        ))
                                .then(Commands.argument("data", StringArgumentType.string())
                                        .executes(ctx ->
                                                sendData(ctx.getSource(),
                                                        ctx.getArgument("name", String.class),
                                                        ctx.getArgument("data", String.class)))
                                )
                        )
                )
                .then(Commands.literal("discover")
                        .executes(ctx -> startDiscovery(ctx.getSource()))
                )
        );
    }

    static private int showList(CommandSourceStack source) {
        List<Device> deviceList = DeviceManager.get().getAllDevices();
        if (!deviceList.isEmpty()) {
            deviceList.forEach(device ->
                    source.sendSuccess(() -> Component.literal(device.getName() + ": " + device.getType()), false));
        } else {
            source.sendSuccess(() -> Component.literal("尚未连接至任何设备"), true);
        }
        return deviceList.size();
    }

    static private int addDevice(CommandSourceStack source, String type, String name) {
        try {
            Device device = DeviceManager.get().createDevice(type, name);
            if (device == null) {
                throw new CommandRuntimeException(Component.literal("设备添加失败"));
            }
            if (!DeviceManager.get().addDevice(device)) {
                throw new CommandRuntimeException(Component.literal("设备已存在"));
            }
            source.sendSuccess(() -> Component.literal("设备添加成功"), true);
        } catch (Exception e) {
            throw new CommandRuntimeException(Component.literal(e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }

    static private int removeDevice(CommandSourceStack source, String name) {
        Optional<Device> device = DeviceManager.get().getDevice(name);
        if (device.isEmpty()) {
            throw new CommandRuntimeException(Component.literal("设备不存在"));
        }
        device.get().disconnect();
        if (!DeviceManager.get().removeDevice(name)) {
            throw new CommandRuntimeException(Component.literal("设备移除失败"));
        }
        source.sendSuccess(() -> Component.literal("设备移除成功"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int connectDevice(CommandSourceStack source, String name) {
        Optional<Device> device = DeviceManager.get().getDevice(name);
        if (device.isEmpty()) {
            throw new CommandRuntimeException(Component.literal("设备不存在"));
        }
        if (device.get().isConnected()) {
            throw new CommandRuntimeException(Component.literal("设备已连接"));
        }
        if (!device.get().connect()) {
            throw new CommandRuntimeException(Component.literal("设备连接失败"));
        }
        source.sendSuccess(() -> Component.literal("设备连接成功"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int disconnectDevice(CommandSourceStack source, String name) {
        Optional<Device> device = DeviceManager.get().getDevice(name);
        if (device.isEmpty()) {
            throw new CommandRuntimeException(Component.literal("设备不存在"));
        }
        if (!device.get().isConnected()) {
            throw new CommandRuntimeException(Component.literal("设备未连接"));
        }
        device.get().disconnect();
        source.sendSuccess(() -> Component.literal("设备成功断开连接"), true);
        return Command.SINGLE_SUCCESS;
    }

    static private int getDeviceAttr(CommandSourceStack source, String name) {
        Optional<Device> device = DeviceManager.get().getDevice(name);
        if (device.isEmpty()) {
            throw new CommandRuntimeException(Component.literal("设备不存在"));
        }
        source.sendSuccess(() -> Component.literal(device.get().toJson().toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    static private int setDeviceAttr(CommandSourceStack source, String name, String attr) {
        Optional<Device> device = DeviceManager.get().getDevice(name);
        if (device.isEmpty()) {
            throw new CommandRuntimeException(Component.literal("设备不存在"));
        }
        boolean connected = device.get().isConnected();
        device.get().fromJson(GsonHelper.parse(attr));
        device.get().disconnect();
        if (connected) {
            device.get().connect();
        }
        source.sendSuccess(() -> Component.literal("属性设置成功"), true);
        return Command.SINGLE_SUCCESS;
    }

    static private int sendData(CommandSourceStack source, String name, String data) {
        Optional<Device> device = DeviceManager.get().getDevice(name);
        if (device.isEmpty()) {
            throw new CommandRuntimeException(Component.literal("设备不存在"));
        }
        if (!device.get().isConnected()) {
            throw new CommandRuntimeException(Component.literal("设备未连接"));
        }
        int ret = device.get().send(data.getBytes());
        if (ret < 0) {
            throw new CommandRuntimeException(Component.literal("发送失败"));
        }
        source.sendSuccess(() -> Component.literal("发送成功"), true);
        return ret;
    }

    static private int startDiscovery(CommandSourceStack source) {
        discoveryResults.clear();
        DeviceManager.get().startDiscovery();
        if (!discoveryResults.isEmpty()) {
            discoveryResults.forEach(pair ->
                    source.sendSuccess(() -> Component.literal(pair.getRight() + ": " + pair.getLeft()), false));
        } else {
            source.sendSuccess(() -> Component.literal("未搜索到设备"), true);
        }
        return discoveryResults.size();
    }
}
