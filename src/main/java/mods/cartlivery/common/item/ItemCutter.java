package mods.cartlivery.common.item;

import mods.cartlivery.ModCartLivery;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ItemCutter extends ItemCartLivery {
	
	public static ResourceLocation texture = new ResourceLocation("cartlivery", "textures/items/cutter.png");

	public ItemCutter() {
		super();
		setUnlocalizedName("cutter");
		setMaxStackSize(1);
		setMaxDamage(100);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote && !player.isSneaking()) {
			player.openGui(ModCartLivery.instance, 0, world, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
		return stack;
	}

}
