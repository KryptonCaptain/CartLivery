package mods.cartlivery.common.item;

import java.util.List;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.cartlivery.CommonProxy;
import mods.cartlivery.client.LiveryTextureInfo;
import mods.cartlivery.client.LiveryTextureRegistry;
import mods.cartlivery.common.utils.ColorUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

public class ItemSticker extends ItemCartLivery {
	
    protected final IIcon[] itemIcons = new IIcon[3];
	
	public ItemSticker() {
		super();
		setUnlocalizedName("sticker");
		setMaxStackSize(4);
		setHasSubtypes(true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for (String pattern : LiveryTextureRegistry.getAvailableLiveries()) list.add(create(pattern));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInfo) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) return;
		LiveryTextureInfo info = LiveryTextureRegistry.map.get(tag.getString("pattern"));
		
		if (info == null) {
			list.add("Pattern: " + I18n.format("cartlivery.unknown", tag.getString("pattern")));
		} else {
			list.add("Pattern: " + I18n.format("cartlivery." + tag.getString("pattern") + ".name"));
		}
		list.add("Primary Color: " + ColorUtils.getColorName(tag.getInteger("primaryColor")));
		list.add("Secondary Color: " + ColorUtils.getColorName(tag.getInteger("secondaryColor")));
	}

	public static ItemStack create(String pattern, int pcolor, int scolor) {
		ItemStack result = new ItemStack(CommonProxy.itemSticker);
		NBTTagCompound tag = new NBTTagCompound();
		result.setTagCompound(tag);
		tag.setString("pattern", pattern);
		tag.setInteger("primaryColor", pcolor);
		tag.setInteger("secondaryColor", scolor);
		return result;
	}
	
	public static ItemStack create(String pattern) {
		return create(pattern, 15, 15);
	}
	
	public static void setPattern(ItemStack stack, String pattern){
		NBTTagCompound tag = new NBTTagCompound();
		stack.setTagCompound(tag);
		tag.setString("pattern", pattern);
	}
	
	public static void setPrimaryColor(ItemStack stack, int pcolor){
		NBTTagCompound tag = new NBTTagCompound();
		stack.setTagCompound(tag);
		tag.setInteger("primaryColor", pcolor);
	}
	
	public static void setSecondaryColor(ItemStack stack, int scolor){
		NBTTagCompound tag = new NBTTagCompound();
		stack.setTagCompound(tag);
		tag.setInteger("secondaryColor", scolor);
	}
	
	public static String getPattern(ItemStack stack){
		return stack.getTagCompound().getString("pattern");
	}
	
	public static int getPrimaryColor(ItemStack stack){
		return stack.getTagCompound().getInteger("primaryColor");
	}
	
	public static int getSecondaryColor(ItemStack stack){
		return stack.getTagCompound().getInteger("secondaryColor");
	}
	
	@Override
    public void registerIcons(IIconRegister iconRegister) {
        itemIcons[0] = iconRegister.registerIcon("cartlivery:sticker_primary");
        itemIcons[1] = iconRegister.registerIcon("cartlivery:sticker_secondary");
        itemIcons[2] = iconRegister.registerIcon("cartlivery:sticker_blank");
    }
	
	@Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 3;
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass) {
        switch (pass) {
            case 0:
                return ColorUtils.getColor(stack.getTagCompound().getByte("primaryColor"));
            case 1:
            	return ColorUtils.getColor(stack.getTagCompound().getByte("secondaryColor"));
            default:
                return super.getColorFromItemStack(stack, pass);
        }
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        if (pass >= itemIcons.length || itemIcons[pass] == null)
            return itemIcons[2];
        return itemIcons[pass];
    }
}
