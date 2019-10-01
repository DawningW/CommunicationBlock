package io.github.qingchenw.communicationblock;

import org.apache.logging.log4j.Logger;

import io.github.qingchenw.communicationblock.utils.SerialPortManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = SerialPortMod.MODID, name = SerialPortMod.NAME, version = SerialPortMod.VERSION)
public class SerialPortMod
{
    public static final String MODID = "serialport";
    public static final String NAME = "Communication Block Mod";
    public static final String VERSION = "1.0";

    @Instance(SerialPortMod.MODID)
    public static SerialPortMod instance;
    @SidedProxy(clientSide = "io.github.qingchenw.communicationblock.SerialPortMod$ClientProxy", serverSide = "io.github.qingchenw.communicationblock.SerialPortMod$CommonProxy")
    public static CommonProxy proxy;
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    	proxy.serverStarting(event);
    }
    
    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event)
    {
    	proxy.serverStopped(event);
    }
    
    
    @Mod.EventBusSubscriber(modid = MODID)
    public static class CommonProxy
    {
    	public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    	
    	public static final Block communicationBlock = new BlockCommunicationBlock(MapColor.BROWN).setBlockUnbreakable().setResistance(6000000.0F).setTranslationKey("communicationBlock");

		public void preInit(FMLPreInitializationEvent event)
		{
			instance.registerMessage(MessageUpdateCommBlock.Handler.class, MessageUpdateCommBlock.class, 0, Side.SERVER);
			GameRegistry.registerTileEntity(TileEntityCommunicationBlock.class, new ResourceLocation(MODID, "communication_block"));
		}

		public void init(FMLInitializationEvent event) {}

		public void serverStarting(FMLServerStartingEvent event) {}
		
		public void serverStopped(FMLServerStoppedEvent event)
		{
			SerialPortManager.disconnectAll();
		}
		
		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event)
		{
			event.getRegistry().register(createItemBlock(communicationBlock));
		}
		
		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event)
		{
			event.getRegistry().register(communicationBlock.setRegistryName("communication_block"));
		}
		
	    private static ItemBlock createItemBlock(Block block)
	    {
	        ItemBlock item = new ItemBlock(block);
	        item.setRegistryName(block.getRegistryName());
	        return item;
	    }
    }
    
    @Mod.EventBusSubscriber(modid = MODID, value = Side.CLIENT)
    public static class ClientProxy extends CommonProxy
    {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event)
        {
        	// ModelLoader.setCustomStateMapper(communicationBlock, new StateMap.Builder().ignore().build());
        	registerInventoryModel(Item.getItemFromBlock(communicationBlock));
		}
        
        private static void registerInventoryModel(Item item)
        {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
