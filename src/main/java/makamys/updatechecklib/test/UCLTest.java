package makamys.updatechecklib.test;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import makamys.updatechecklib.UpdateCheckLib;
import net.minecraft.launchwrapper.Launch;

@Mod(modid = UCLTest.MODID, version = "0.0")
public class UCLTest {
	
	public static final String MODID = "UpdateCheckLibTest"; 
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	if(Loader.isModLoaded("UpdateCheckLib")) {
    		initUpdateCheck();
    	}
    }
    
    @cpw.mods.fml.common.Optional.Method(modid = "UpdateCheckLib")
    private void initUpdateCheck() {
    	// outdated mod via network (json returns 1.7.10-35.4.1 as of now)
    	UpdateCheckLib.submitModTask(MODID, "https://raw.githubusercontent.com/makamys/MAtmos/master/updatejson/update-matmos.json", "https://github.com/makamys/MAtmos/releases");
    	
    	// outdated mod mock
    	UpdateCheckLib.submitTask("outdated mod", "0.8", UpdateCheckLib.MODS_CATEGORY_ID, "mock://1.0", "https://example.com");
    	
    	// up to date mod mock
    	UpdateCheckLib.submitTask("up to date mod", "1.0", UpdateCheckLib.MODS_CATEGORY_ID, "mock://1.0", "https://example.com");
    	
    	// very new mod mock
    	UpdateCheckLib.submitTask("very new mod", "1.2", UpdateCheckLib.MODS_CATEGORY_ID, "mock://1.0", "https://example.com");
    	
    	// bad json url, non-mod test
    	UpdateCheckLib.registerCategory("resourcePacks", "1.1.1", "Resource pack");
    	UpdateCheckLib.submitTask("bad res pack", "0.1", "resourcePacks", "bad json url", "bad update url");
    	
    	// category with no interesting elements
    	UpdateCheckLib.registerCategory("thingy", "1.1.1", "Thingy");
    	UpdateCheckLib.submitTask("up to date thingy", "1.1", "thingy", "mock://1.1", "https://example.com");
    }
    
}
