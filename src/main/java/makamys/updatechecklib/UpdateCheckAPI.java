package makamys.updatechecklib;

import java.util.concurrent.CompletableFuture;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import makamys.updatechecklib.UpdateCheckLib.UpdateCategory;

import static makamys.updatechecklib.UpdateCheckLib.*;

public class UpdateCheckAPI {
	
	public static String MODS_CATEGORY_ID = "mods";
	
	public static void submitModTask(String modid, String updateJSONUrl, String updateURL) {
    	if(!isEnabled()) return;
    	submitModTask(modid, null, updateJSONUrl, updateURL);
    }
    
    public static void submitModTask(String modid, String currentVersion, String updateJSONUrl, String updateURL) {
    	if(!isEnabled()) return;
    	ModContainer mc = Loader.instance().getIndexedModList().get(modid);
    	if(mc == null) {
    		LOGGER.warn("Tried to register update check for non-existent modid: " + modid);
    		return;
    	}
    	submitTask(mc.getName(), currentVersion != null ? currentVersion : mc.getVersion(), MODS_CATEGORY_ID, updateJSONUrl, updateURL);
    }
    
    public static void submitTask(String name, String currentVersion, String categoryID, String updateJSONUrl, String updateURL) {
    	if(!isEnabled()) return;
    	if(!categories.containsKey(categoryID)) {
    		LOGGER.warn("Tried to register a non-existent category for mod " + name + ": " + categoryID);
    	}
    	futures.add(CompletableFuture.supplyAsync(new UpdateCheckTask(name, currentVersion, categories.get(categoryID), updateJSONUrl, updateURL), executor));
    }
    
    public static void registerCategory(String id, String version, String displayName) {
    	if(!isEnabled()) return;
    	categories.put(id, new UpdateCategory(id, version, displayName));
    }
	
}
