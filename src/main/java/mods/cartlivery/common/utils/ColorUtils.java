package mods.cartlivery.common.utils;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ColorUtils {

	private final static int[] colors = {0x2D2D2D,0xA33835,0x394C1E,0x5C3A24,0x3441A2,0x843FBF,0x36809E,0x888888,0x444444,0xE585A0,0x3FAA36,0xFFC700,0x7F9AD1,0xFF64FF,0xFF6A00,0xFFFFFF};
	
	public static int getDyeColor(ItemStack dyeStack) {
		for (int idx = 0; idx < 16; idx++) {
			String oreName = new StringBuilder("dye").append(ItemDye.field_150923_a[idx]).toString().toLowerCase();
			for (int oreId : OreDictionary.getOreIDs(dyeStack)) {
				if (OreDictionary.getOreName(oreId).toLowerCase().startsWith(oreName)) return idx;
			}
		}
		return -1;
	}

	public static String getColorName(int color) {
		return (color < 0 || color > 15) ? "???" : I18n.format("color." + Integer.toString(color) + ".name");
	}
	
	public static int getColor(int color) {
		return colors[color];
	}
	
}
