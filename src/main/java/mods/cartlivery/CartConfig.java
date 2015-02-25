package mods.cartlivery;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CartConfig {
	public final Configuration config;
	
	public static boolean ENABLE_AUTOCUTTER = true;
	public static boolean AUTOCUTTER_USES_RF = true;
	public static boolean ENABLE_EMBLEMS = true;
	public static boolean PLAY_SOUNDS = true;

	public CartConfig(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
	}

	public boolean isEnabled(String name, boolean def) {
		return config.get("enable", name, def).getBoolean(def);
	}

	public void preInit() {
		// General
		PLAY_SOUNDS = config.getBoolean("playSounds", "general", true, "change to 'playSounds=false' to disable extra sounds playing when using the mod");
		
		//Machines
		ENABLE_AUTOCUTTER = config.getBoolean("enableLiveryCuttingPress", "machines", true, "change to 'enableLiveryCuttingPress=false' to disable the Livery Cutting Press");
		AUTOCUTTER_USES_RF = config.getBoolean("requirePower", "machines", true, "change to 'requirePower=false' to disable the Power Requirements for the Livery Cutting Press");

		// Railcraft integration
		if(Loader.isModLoaded("Railcraft")) {
			ENABLE_EMBLEMS = config.getBoolean("enableEmblems", "railcraft", true, "change to 'enableEmblems=false' to disable the ability to place emblems on carts");
		}
	}

	public void setCategoryComment(String category, String comment) {
		config.setCategoryComment(category, comment);
	}

	public void save() {
		config.save();
	}
}
