package io.github.qingchenw.commblock.quilt;

import io.github.qingchenw.commblock.fabriclike.CommBlockFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public final class CommBlockModQuilt implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        CommBlockFabricLike.init();
    }

    @Override
    public void onInitializeClient(ModContainer mod) {
        CommBlockFabricLike.clientInit();
    }
}
