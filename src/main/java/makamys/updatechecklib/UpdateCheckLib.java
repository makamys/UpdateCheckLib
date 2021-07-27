package makamys.updatechecklib;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
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
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = UpdateCheckLib.MODID, version = UpdateCheckLib.VERSION)
public class UpdateCheckLib
{
    public static final String MODID = "UpdateCheckLib";
    public static final String VERSION = "0.0";
    
    public static final Logger LOGGER = LogManager.getLogger("updatechecklib");

    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 4, 60, TimeUnit.SECONDS, workQueue);
    private static List<CompletableFuture<UpdateCheckTask.Result>> futures = new ArrayList<>();
    private static int updateCount = 0;
    private static final File updatesFile = new File(Launch.minecraftHome, "updates.html");
    
    public static String MODS_CATEGORY_ID = "mods";
    static UpdateCategory MODS = new UpdateCategory(MODS_CATEGORY_ID, Loader.MC_VERSION, "Mod");
    static Map<String, UpdateCategory> categories = new HashMap<>();
    
    private static final int UPDATES_BUTTON_ID = 1615486202;
    
    static {
    	categories.put("mods", MODS);
    }
    
    private static boolean isEnabled() {
    	ConfigUCL.loadIfNotAlready();
    	return ConfigUCL.enabled;
    }
    
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
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGui(InitGuiEvent.Post event) {
    	if(event.gui instanceof GuiMainMenu) {
    		ConfigUCL.reload();
    		if(ConfigUCL.showUpdatesButton && updateCount > 0) {
    			String url = null;
    			try {
					url = updatesFile.toURI().toURL().toString();
				} catch (MalformedURLException e) {
					url = "";
					e.printStackTrace();
				}
    			int buttonX = ConfigUCL.updatesButtonX + (ConfigUCL.updatesButtonAbsolutePos ? 0 : event.gui.width / 2);
    			int buttonY = ConfigUCL.updatesButtonY + (ConfigUCL.updatesButtonAbsolutePos ? 0 : event.gui.height / 4);
	    		GuiButton button = new GuiButtonUpdates(UPDATES_BUTTON_ID, buttonX, buttonY, 20, 20, updateCount, url);
	    		event.buttonList.add(button);
    		}
    	}
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	if(!isEnabled()) return;
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
				
				updateCount = 0;
				for(UpdateCheckTask.Result result : results) {
					if(result.newVersion != null) {
						updateCount++;
					}
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
    	new ResultHTMLRenderer().render(updatesFile);
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
