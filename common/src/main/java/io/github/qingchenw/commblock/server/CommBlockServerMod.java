package io.github.qingchenw.commblock.server;

import com.mojang.brigadier.CommandDispatcher;
import io.github.qingchenw.commblock.server.command.DeviceCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class CommBlockServerMod {
    public static void serverInit(MinecraftServer server) {

    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        DeviceCommand.register(dispatcher);
    }
}
