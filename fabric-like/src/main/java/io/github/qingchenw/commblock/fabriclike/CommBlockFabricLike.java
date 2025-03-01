package io.github.qingchenw.commblock.fabriclike;

import io.github.qingchenw.commblock.CommBlockMod;
import io.github.qingchenw.commblock.client.CommBlockClientMod;
import io.github.qingchenw.commblock.server.CommBlockServerMod;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CommBlockFabricLike {
    public static void init() {
        CommBlockMod.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            CommBlockServerMod.registerCommands(dispatcher);
        });
    }

    public static void clientInit() {
        CommBlockClientMod.clientInit();
    }
}
