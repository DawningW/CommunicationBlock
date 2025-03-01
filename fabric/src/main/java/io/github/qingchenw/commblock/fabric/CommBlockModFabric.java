package io.github.qingchenw.commblock.fabric;

import io.github.qingchenw.commblock.fabriclike.CommBlockFabricLike;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public final class CommBlockModFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        CommBlockFabricLike.init();
    }

    @Override
    public void onInitializeClient() {
        CommBlockFabricLike.clientInit();
    }
}
