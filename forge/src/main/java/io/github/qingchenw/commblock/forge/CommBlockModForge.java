package io.github.qingchenw.commblock.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.qingchenw.commblock.CommBlockMod;
import io.github.qingchenw.commblock.client.CommBlockClientMod;
import io.github.qingchenw.commblock.server.CommBlockServerMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CommBlockMod.MOD_ID)
public final class CommBlockModForge {
    public CommBlockModForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(CommBlockMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        MinecraftForge.EVENT_BUS.addListener(this::commandRegister);

        CommBlockMod.init();
    }

    public void clientInit(FMLClientSetupEvent event) {
        CommBlockClientMod.clientInit();
    }

    public void commandRegister(RegisterCommandsEvent event) {
        CommBlockServerMod.registerCommands(event.getDispatcher());
    }
}
