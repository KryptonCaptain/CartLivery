package mods.cartlivery.common.item;

import mods.cartlivery.common.utils.ColorUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class LiveryStickerColoringRecipe implements IRecipe {

	ItemStack sample = ItemSticker.create("");
	
	public boolean matches(InventoryCrafting inv, World world) {
		if (inv.getSizeInventory() < getRecipeSize())
            return false;
		for(int row = 0; row < 3; row++){
			if(inv.getStackInRowAndColumn(0, row) != null){
				return false;
			}
			if(inv.getStackInRowAndColumn(2, row) != null){
				return false;
			}
		}
        if (inv.getStackInRowAndColumn(1, 0) == null || ColorUtils.getDyeColor(inv.getStackInRowAndColumn(1, 0)) == -1){
        	return false;
        }
        if (inv.getStackInRowAndColumn(1, 1) == null || !(inv.getStackInRowAndColumn(1, 1).getItem() instanceof ItemSticker)){
            return false;
        }
        if (inv.getStackInRowAndColumn(1, 2) == null || ColorUtils.getDyeColor(inv.getStackInRowAndColumn(1, 2)) == -1){
            return false;
        }
		return true;
	}

	public ItemStack getCraftingResult(InventoryCrafting inv) {
		if (inv.getSizeInventory() < getRecipeSize())
            return null;
		String pattern = null;
		int colorPrimary = 0;
		int colorSecondary = 0;
		ItemStack dyePrimary = inv.getStackInRowAndColumn(1, 0);
        if (ColorUtils.getDyeColor(dyePrimary) != -1)
        	colorPrimary = ColorUtils.getDyeColor(dyePrimary);
        ItemStack sticker = inv.getStackInRowAndColumn(1, 1);
        if (sticker.getItem() instanceof ItemSticker && sticker.getTagCompound() != null)
			pattern = sticker.getTagCompound().getString("pattern");
        ItemStack dyeSecondary = inv.getStackInRowAndColumn(1, 2);
        if (ColorUtils.getDyeColor(dyeSecondary) != -1)
        	colorSecondary = ColorUtils.getDyeColor(dyeSecondary);
		return ItemSticker.create(pattern, colorPrimary, colorSecondary);
	}

	public int getRecipeSize() {
		return 9;
	}

	public ItemStack getRecipeOutput() {
		return sample;
	}

}
