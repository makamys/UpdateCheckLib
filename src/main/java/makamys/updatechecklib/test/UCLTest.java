package makamys.updatechecklib.test;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import makamys.updatechecklib.UpdateCheckLib;

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
    	UpdateCheckLib.submitModTask(MODID, "https://raw.githubusercontent.com/makamys/MAtmos/master/updatejson/update-matmos.json", "https://github.com/makamys/MAtmos/releases");
    	
    	// bad url test
    	UpdateCheckLib.registerCategory("resourcePacks", "1.1.1", "Resource pack");
    	UpdateCheckLib.submitTask("mod name", "0.1", "resourcePacks", "bad json url", "bad update url");
    }
    
}
