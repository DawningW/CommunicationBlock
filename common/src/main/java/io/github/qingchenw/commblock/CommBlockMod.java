package io.github.qingchenw.commblock;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.qingchenw.commblock.block.CommunicationBlock;
import io.github.qingchenw.commblock.blockentity.CommunicationBlockEntity;
import io.github.qingchenw.commblock.device.DeviceManager;
import io.github.qingchenw.commblock.network.C2SSetCommBlockPacket;
import io.github.qingchenw.commblock.server.CommBlockServerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class CommBlockMod {
    public static final String MOD_ID = "commblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<CommunicationBlock> COMMUNICATION_BLOCK = BLOCKS.register("communication_block", CommunicationBlock::new);

    public static final RegistrySupplier<BlockEntityType<CommunicationBlockEntity>> COMMUNICATION_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("communication_block", () -> BlockEntityType.Builder.of(CommunicationBlockEntity::new, COMMUNICATION_BLOCK.get()).build(null));

    static {
        registerBlockItem(COMMUNICATION_BLOCK, new Item.Properties().arch$tab(CreativeModeTabs.OP_BLOCKS));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<? extends Block> block, Supplier<? extends BlockItem> item) {
        return ITEMS.register(block.getId(), item);
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<? extends Block> block, Item.Properties properties) {
        return registerBlockItem(block, () -> new BlockItem(block.get(), properties));
    }

    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(CommBlockServerMod::serverInit);

        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2SSetCommBlockPacket.ID, C2SSetCommBlockPacket::receivePacket);

        DeviceManager.get().loadFromJson(loadConfig());
    }

    public static JsonArray loadConfig() {
        JsonArray json = null;
        Path path = Platform.getConfigFolder().resolve("devices.json");
        try (FileReader reader = new FileReader(path.toFile())) {
            json = GsonHelper.parseArray(reader);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Config file not found, using default config");
        } catch (IOException e) {
            LOGGER.error("Error reading config file", e);
        }
        if (json == null) {
            json = new JsonArray();
            saveConfig(json);
        }
        return json;
    }

    public static void saveConfig(JsonArray json) {
        Path path = Platform.getConfigFolder().resolve("devices.json");
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(GsonHelper.toStableString(json));
        } catch (IOException e) {
            LOGGER.error("Error writing config file", e);
        }
    }
}
