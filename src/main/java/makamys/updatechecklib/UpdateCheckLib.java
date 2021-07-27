package makamys.updatechecklib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;

@Mod(modid = UpdateCheckLib.MODID, version = UpdateCheckLib.VERSION)
public class UpdateCheckLib
{
    public static final String MODID = "UpdateCheckLib";
    public static final String VERSION = "0.0";
    
    public static final Logger LOGGER = LogManager.getLogger("dmod");

    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 4, 60, TimeUnit.SECONDS, workQueue);
    private static List<CompletableFuture<UpdateCheckTask.Result>> futures = new ArrayList<>();
    
    public static String MODS_CATEGORY_ID = "mods";
    static UpdateCategory MODS = new UpdateCategory(MODS_CATEGORY_ID, Loader.MC_VERSION, "Mod");
    static Map<String, UpdateCategory> categories = new HashMap<>();
    
    static {
    	categories.put("mods", MODS);
    }
    
    public static void submitModTask(String modid, String updateJSONUrl, String updateURL) {
    	submitModTask(modid, null, updateJSONUrl, updateURL);
    }
    
    public static void submitModTask(String modid, String currentVersion, String updateJSONUrl, String updateURL) {
    	ModContainer mc = Loader.instance().getIndexedModList().get(modid);
    	if(mc == null) {
    		LOGGER.warn("Tried to register update check for non-existent modid: " + modid);
    		return;
    	}
    	submitTask(mc.getName(), currentVersion != null ? currentVersion : mc.getVersion(), MODS_CATEGORY_ID, updateJSONUrl, updateURL);
    }
    
    public static void submitTask(String name, String currentVersion, String categoryID, String updateJSONUrl, String updateURL) {
    	if(!categories.containsKey(categoryID)) {
    		LOGGER.warn("Tried to register a non-existent category for mod " + name + ": " + categoryID);
    	}
    	futures.add(CompletableFuture.supplyAsync(new UpdateCheckTask(name, currentVersion, categories.get(categoryID), updateJSONUrl, updateURL), executor));
    }
    
    public static void registerCategory(String id, String version, String displayName) {
    	categories.put(id, new UpdateCategory(id, version, displayName));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).thenRun(new Runnable() {	
			@Override
			public void run() {
				List<UpdateCheckTask.Result> results = futures.stream().map(f -> {
					try {
						return f.get();
					} catch(Exception e) {
						LOGGER.error("Failed to get retrieve update check result: " + e.getMessage());
						return null;
					}
				})
				.collect(Collectors.toList());
				
				for(UpdateCheckTask.Result result : results) {
					result.task.category.results.add(result);
				}
				
				onFinished();
				if(event.getSide() == Side.CLIENT) {
					onFinishedClient();
				}
			}
		});
    }
    
    private void onFinished() {
    	new ResultHTMLRenderer().render(new File(Minecraft.getMinecraft().mcDataDir + "/updates.html"));
    }
    
    @SideOnly(Side.CLIENT)
    private void onFinishedClient() {
    	
    }
    
    static class UpdateCategory implements Comparable<UpdateCategory> {
    	public String id;
    	public String displayName;
    	public String version;
    	public List<UpdateCheckTask.Result> results = new ArrayList<>();
    	
    	public UpdateCategory(String id, String version, String displayName) {
    		this.id = id;
    		this.version = version;
    		this.displayName = displayName;
    	}

		@Override
		public int compareTo(UpdateCategory o) {
			return this.id.equals(UpdateCheckLib.MODS_CATEGORY_ID) ? -1 : displayName.compareTo(o.displayName);
		}
    }
}
