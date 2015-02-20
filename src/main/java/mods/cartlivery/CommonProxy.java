package mods.cartlivery;

import java.util.Random;
import java.util.Set;

import mods.cartlivery.common.CartLivery;
import mods.cartlivery.common.block.BlockAutoCutter;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;
import mods.cartlivery.common.item.ItemCutter;
import mods.cartlivery.common.item.ItemSticker;
import mods.cartlivery.common.item.LiveryStickerColoringRecipe;
import mods.cartlivery.common.network.LiveryGuiPatternHandler;
import mods.cartlivery.common.network.LiveryGuiPatternMessage;
import mods.cartlivery.common.network.LiveryRequestHandler;
import mods.cartlivery.common.network.LiveryRequestMessage;
import mods.cartlivery.common.network.LiveryUpdateHandler;
import mods.cartlivery.common.network.LiveryUpdateMessage;
import mods.cartlivery.common.utils.ColorUtils;
import mods.cartlivery.common.utils.NetworkUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.minecart.MinecartEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

	public static SimpleNetworkWrapper network = new SimpleNetworkWrapper(ModCartLivery.CHANNEL_NAME);
	public static Set<Class<?>> excludedClasses = Sets.newHashSet();
	
	public static ItemSticker itemSticker = new ItemSticker();
	public static ItemCutter itemCutter = new ItemCutter();
	public static Block autoCutter = new BlockAutoCutter(false).setStepSound(Block.soundTypeMetal).setCreativeTab(CreativeTabs.tabDecorations);
	public static Block autoCutterLive = new BlockAutoCutter(true).setStepSound(Block.soundTypeMetal);

	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		NetworkRegistry.INSTANCE.registerGuiHandler(ModCartLivery.instance, new GuiHandler());
		network.registerMessage(LiveryRequestHandler.class, LiveryRequestMessage.class, 0, Side.SERVER);
		network.registerMessage(LiveryUpdateHandler.class, LiveryUpdateMessage.class, 1, Side.CLIENT);
		network.registerMessage(LiveryGuiPatternHandler.class, LiveryGuiPatternMessage.class, 2, Side.SERVER);
		
		GameRegistry.registerItem(itemCutter, "cutter");
		GameRegistry.registerItem(itemSticker, "sticker");
		GameRegistry.registerBlock(autoCutter, "autoCutter");
		GameRegistry.registerBlock(autoCutterLive, "autoCutterLive");
			
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCutter), "i i", "rir", "r r", 'i', "ingotIron", 'r', "dyeRed"));
		GameRegistry.addRecipe(new LiveryStickerColoringRecipe());
		RecipeSorter.register("cartlivery:coloring", LiveryStickerColoringRecipe.class, Category.SHAPED, "after:minecraft:shaped");
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(autoCutter, 1), "iii", "ici", "iii", 'i', "ingotIron", 'c', itemCutter));
		
		FMLInterModComms.sendMessage(ModCartLivery.MOD_ID, "addClassExclusion", "mods.railcraft.common.carts.EntityLocomotive");
		FMLInterModComms.sendMessage(ModCartLivery.MOD_ID, "addClassExclusion", "mods.railcraft.common.carts.EntityTunnelBore");
		FMLInterModComms.sendMessage(ModCartLivery.MOD_ID, "addBuiltInLiveries", "stripe1,stripe2,arrowup,dblarrow,corners1,bottom,thissideup,love,db,railtech,fragile,electrical_hazard,fallout,radioactive,railroad,warning,flammable,biohazard,thick_diagonal,diagonal");
		
		registerTileEntities();
	}
	
	@SubscribeEvent
	public void handleMinecartConstruct(EntityConstructing event) {
		if (event.entity instanceof EntityMinecart) {
			for (Class<?> excluded : excludedClasses) if (excluded.isInstance(event.entity)) return;
			event.entity.registerExtendedProperties(CartLivery.EXT_PROP_NAME, new CartLivery());
		}
	}
	
	@SubscribeEvent
	public void handleMinecartJoinWorld(EntityJoinWorldEvent event) {
		if (!event.entity.worldObj.isRemote) return;
		
		if (event.entity instanceof EntityMinecart && event.entity.getExtendedProperties(CartLivery.EXT_PROP_NAME) != null) {
			CommonProxy.network.sendToServer(new LiveryRequestMessage(event.entity));
		}
	}
	
	@SubscribeEvent
	public void handleMinecartPainting(EntityInteractEvent event) {
		if (event.entityPlayer.worldObj.isRemote) return;
		
		if (event.entityPlayer.isSneaking() && event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME) != null) {
			ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
			CartLivery livery = (CartLivery) event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME);
			int newColor = ColorUtils.getDyeColor(stack);	
			
			if (newColor != -1 && newColor != livery.baseColor) {
				if((livery.pattern == null || livery.pattern.isEmpty() || livery.pattern.equals("cartlivery.unknown")) && livery.baseColor != 7){
					dropDye(event, livery);
				}else if(!(livery.pattern == null || livery.pattern.isEmpty() || livery.pattern.equals("cartlivery.unknown"))){
					dropSticker(event, livery);
				}
				livery.baseColor = newColor;
				livery.pattern = "";
				stack.stackSize--;
				if (stack.stackSize == 0) event.entityPlayer.setCurrentItemOrArmor(0, null);
				
				event.target.playSound("mob.chicken.plop", 1.0F, 1.0F);
				CommonProxy.network.sendToAllAround(new LiveryUpdateMessage(event.target, livery), NetworkUtil.targetEntity(event.target));
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void handleMinecartStickerApply(EntityInteractEvent event) {
		if (event.entityPlayer.worldObj.isRemote) return;
		
		if (event.entityPlayer.isSneaking() && event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME) != null) {
			ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
			if (stack == null || !(stack.getItem() instanceof ItemSticker) || stack.getTagCompound() == null) return;
			
			String pattern = stack.getTagCompound().getString("pattern");
			int primaryColor = stack.getTagCompound().getInteger("primaryColor");
			int secondaryColor = stack.getTagCompound().getInteger("secondaryColor");
			if (pattern.isEmpty()) return;
			
			CartLivery livery = (CartLivery) event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME);
			if(!(livery.pattern.equals(pattern) && livery.baseColor==primaryColor && livery.patternColor==secondaryColor)){
				if((livery.pattern == null || livery.pattern.isEmpty() || livery.pattern.equals("cartlivery.unknown")) && livery.baseColor != 7){
					dropDye(event, livery);
				}else if(!(livery.pattern == null || livery.pattern.isEmpty() || livery.pattern.equals("cartlivery.unknown"))){
					dropSticker(event, livery);
				}
				livery.pattern = pattern;
				livery.baseColor = primaryColor;
				livery.patternColor = secondaryColor;
				
				stack.stackSize--;
				if (stack.stackSize == 0) event.entityPlayer.setCurrentItemOrArmor(0, null);
				
				event.target.playSound("mob.chicken.plop", 1.0F, 1.0F);
				CommonProxy.network.sendToAllAround(new LiveryUpdateMessage(event.target, livery), NetworkUtil.targetEntity(event.target));
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleMinecartStickerRemove(EntityInteractEvent event) {
		if (event.entityPlayer.worldObj.isRemote) return;

		if (event.entityPlayer.isSneaking() && event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME) != null) {
			ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
			if (stack == null || !(stack.getItem() instanceof ItemCutter)) return;
			
			CartLivery livery = (CartLivery) event.target.getExtendedProperties(CartLivery.EXT_PROP_NAME);
			if((livery.pattern == null || livery.pattern.isEmpty() || livery.pattern.equals("cartlivery.unknown")) && livery.baseColor != 7){
				dropDye(event, livery);
				
				ItemStack tool = event.entityPlayer.inventory.getStackInSlot(event.entityPlayer.inventory.currentItem);
				tool.setItemDamage(tool.getItemDamage() + 1);
				if (tool.getItemDamage() > tool.getMaxDamage()) {
					event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, null);
				}
			}else if(!(livery.pattern == null || livery.pattern.isEmpty() || livery.pattern.equals("cartlivery.unknown"))){
				dropSticker(event, livery);
				
				ItemStack tool = event.entityPlayer.inventory.getStackInSlot(event.entityPlayer.inventory.currentItem);
				tool.setItemDamage(tool.getItemDamage() + 1);
				if (tool.getItemDamage() > tool.getMaxDamage()) {
					event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, null);
				}
			}
			
			event.target.playSound("mob.sheep.shear", 1.0F, 1.0F);
			CommonProxy.network.sendToAllAround(new LiveryUpdateMessage(event.target, livery), NetworkUtil.targetEntity(event.target));
			event.setCanceled(true);
		}
	}
	
	public void registerTileEntities() {
		registerTileEntity(TileEntityAutoCutter.class, "autoCutter");
	}

	private void registerTileEntity(Class<? extends TileEntity> cls, String baseName) {
		GameRegistry.registerTileEntity(cls, "tile.autoCutter." + baseName);
	}
	
	public static void dropSticker(EntityInteractEvent event, CartLivery livery){
		EntityItem ent = event.target.entityDropItem(ItemSticker.create(livery.pattern, livery.baseColor, livery.patternColor), 1.0F);
		Random rand = new Random();
		ent.motionY += rand.nextFloat() * 0.05F;
        ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
        ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
		livery.pattern = "";
		livery.baseColor = 7; //Return color to default
	}
	
	public static void dropDye(EntityInteractEvent event, CartLivery livery){
		EntityItem ent = event.target.entityDropItem(new ItemStack(Items.dye, 1, livery.baseColor), 1.0F);
		Random rand = new Random();
		ent.motionY += rand.nextFloat() * 0.05F;
        ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
        ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
		livery.pattern = "";
		livery.baseColor = 7; //Return color to default
	}
}
