package mods.cartlivery;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import mods.cartlivery.client.LiveryTextureRegistry;
import mods.cartlivery.client.model.ModelCartLivery;
import mods.cartlivery.client.model.RailcraftModel;
import mods.cartlivery.common.CartLivery;
import mods.cartlivery.common.item.ItemCutter;
import mods.cartlivery.common.item.ItemSticker;
import mods.cartlivery.common.item.LiveryStickerColoringRecipe;
import mods.cartlivery.common.network.LiveryGuiPatternHandler;
import mods.cartlivery.common.network.LiveryGuiPatternMessage;
import mods.cartlivery.common.network.LiveryRequestHandler;
import mods.cartlivery.common.network.LiveryRequestMessage;
import mods.cartlivery.common.network.LiveryUpdateHandler;
import mods.cartlivery.common.network.LiveryUpdateMessage;
import mods.cartlivery.common.utils.NetworkUtil;
import mods.railcraft.client.emblems.EmblemToolsClient;
import mods.railcraft.common.emblems.EmblemToolsServer;
import mods.railcraft.common.emblems.ItemEmblem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.RecipeSorter.Category;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RailcraftClientProxy extends RailcraftCommonProxy{
	public RailcraftClientProxy() {
		//this.init();
	}
	
	@Override
	public void init() {
		super.init();
		
		((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new LiveryTextureRegistry());
		
		ModCartLivery.log.info(I18n.format("message.cartlivery.overwriteModel"));
		if (Loader.isModLoaded("Railcraft")) {
			replaceRailcraftCartModel();	
		} else {
			replaceMinecraftCartModel();
		}
		
		FMLInterModComms.sendMessage("Waila", "register", "mods.cartlivery.integration.waila.CartLiveryWailaModule.register");
	}
	
	private void replaceMinecraftCartModel() {
		try {
			Field modelMinecart = null;
			for (Field field : RenderMinecart.class.getDeclaredFields()) {
				if (ModelBase.class.equals(field.getType())) {
					modelMinecart = field;
					break;
				}
			}
			modelMinecart.setAccessible(true);
			for (Class<?> entityClass : ImmutableList.<Class<?>>of(EntityMinecart.class, EntityMinecartTNT.class, EntityMinecartMobSpawner.class)) {
				RenderMinecart renderer = (RenderMinecart) RenderManager.instance.entityRenderMap.get(entityClass);
				modelMinecart.set(renderer, new ModelCartLivery());
			}
		} catch (Exception e) {
			ModCartLivery.log.warn(I18n.format("message.cartlivery.overwriteModelMinecraftFail"));
		}
	}
	
	private void replaceRailcraftCartModel() {
		try {
			Class<?> modelManagerClass = Class.forName("mods.railcraft.client.render.carts.CartModelManager");
			Field defaultCore = modelManagerClass.getDeclaredField("modelMinecart");
			Field modifiers = Field.class.getDeclaredField("modifiers");
			defaultCore.setAccessible(true);
			modifiers.setAccessible(true);
			
			modifiers.set(defaultCore, defaultCore.getModifiers() & ~Modifier.FINAL);
			defaultCore.set(null, new RailcraftModel());
			modifiers.set(defaultCore, defaultCore.getModifiers() | Modifier.FINAL);
		} catch (Exception e) {
			ModCartLivery.log.warn(I18n.format("message.cartlivery.overwriteModelRailcraftFail"));
		}
	}
}
