package mods.cartlivery;

import mods.cartlivery.RailcraftCommonProxy;
import mods.cartlivery.RailcraftClientProxy;
import mods.cartlivery.client.LiveryTextureRegistry;
import mods.cartlivery.client.gui.GuiAutoCutter;
import mods.cartlivery.common.block.BlockAutoCutter;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;
import mods.cartlivery.common.container.ContainerAutoCutter;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid=ModCartLivery.MOD_ID, name=ModCartLivery.MOD_NAME, version=ModCartLivery.VERSION, useMetadata=true, dependencies="required-after:Forge@[10.13.2.1230,);after:Railcraft")
public class ModCartLivery {

	public static final String MOD_ID = "CartLivery";
	public static final String MOD_NAME = "Cart Livery";
	public static final String CHANNEL_NAME = "cartLiv";
	public static final String VERSION = "0.10.8";
	public static final String COMMON_PROXY_NAME = "mods.cartlivery.CommonProxy";
	public static final String CLIENT_PROXY_NAME = "mods.cartlivery.ClientProxy";

	@Mod.Instance
	public static ModCartLivery instance;
	
	@SidedProxy(serverSide=ModCartLivery.COMMON_PROXY_NAME, clientSide=ModCartLivery.CLIENT_PROXY_NAME)
	public static CommonProxy proxy;
	
	public static Logger log;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if(Loader.isModLoaded("Railcraft")){
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				proxy = new mods.cartlivery.RailcraftCommonProxy();
			}else{
				proxy = new mods.cartlivery.RailcraftClientProxy();
			}
		}
		proxy.init();
	}
	
	@Mod.EventHandler
	public void handleIMCMessage(FMLInterModComms.IMCEvent event) {
		for (IMCMessage message : event.getMessages()) {
			if ("addClassExclusion".equals(message.key) && message.isStringMessage()) {
				try {
					Class<?> clazz = Class.forName(message.getStringValue());
					CommonProxy.excludedClasses.add(clazz);
				} catch (ClassNotFoundException e) {
					I18n.format("message.cartlivery.invalidExclusion", message.getSender(), message.getStringValue());
				}
			}
			if ("addBuiltInLiveries".equals(message.key) && message.isStringMessage() && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				String[] liveries = message.getStringValue().split(",");
				log.info(I18n.format("message.cartlivery.registerBuiltIn", liveries.length, message.getSender()));
				for(String livery : liveries) LiveryTextureRegistry.builtInLiveries.put(livery, message.getSender());
			}
		}
	}
}
