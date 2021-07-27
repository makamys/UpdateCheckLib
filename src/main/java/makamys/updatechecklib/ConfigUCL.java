package makamys.updatechecklib;

import java.io.File;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class ConfigUCL {
	
	public static boolean enabled;
	
	private static boolean loaded;
	
	public static void loadIfNotAlready() {
		if(!loaded) {
			reload();
			loaded = true;
		}
	}
	
	public static void reload() {
		Configuration config = new Configuration(new File(Launch.minecraftHome, "config/" + UpdateCheckLib.MODID + ".cfg"));
        
        config.load();
        
        enabled = config.getBoolean("enable", "_general", true, "Set this to false to disable this mod");
        
        if (config.hasChanged()) {
            config.save();
        }
	}
	
}
