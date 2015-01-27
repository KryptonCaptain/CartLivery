package mods.cartlivery.common;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class CartLivery implements IExtendedEntityProperties {

	public static final String EXT_PROP_NAME = "CartLivery";
	
	public String pattern = "";
	public int baseColor = 7;
	public int patternColor = 7;
	public String emblem = "";
	
	public void saveNBTData(NBTTagCompound compound) {
		compound.setInteger("liveryBaseColor", baseColor);
		compound.setInteger("liveryPatternColor", patternColor);
		compound.setString("liveryPattern", pattern);
		compound.setString("liveryEmblem", emblem);
	}

	public void loadNBTData(NBTTagCompound compound) {
		if (compound.hasKey("liveryBaseColor")) baseColor = compound.getInteger("liveryBaseColor");
		patternColor = compound.getInteger("liveryPatternColor");
		pattern = compound.getString("liveryPattern");
		emblem = compound.getString("liveryEmblem");
	}

	public void init(Entity entity, World world) {
	}
	
	public String toString(){
		return "Pattern: "+pattern+", Primary Color: "+Integer.toString(baseColor)+", Secondary Color: "+Integer.toString(patternColor)+", Emblem: "+emblem;
	}

}
