package makamys.updatechecklib;

import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.common.versioning.ComparableVersion;
import makamys.updatechecklib.UpdateCheckLib.UpdateCategory;

import static makamys.updatechecklib.UpdateCheckLib.LOGGER;

class UpdateCheckTask implements Supplier<UpdateCheckTask.Result> {
    	
	String name;
	ComparableVersion currentVersion;
	UpdateCategory category;
	String updateJSONUrl;
	String homepage = "";
	
	public static final boolean TEST_MODE = Boolean.parseBoolean(System.getProperty("updateCheckLib.test", "false"));
	private static final String MOCK_PREFIX = "mock://";
	
	public UpdateCheckTask(String name, String currentVersion, UpdateCategory category, String updateJSONUrl) {
		this.name = name;
		this.currentVersion = new ComparableVersion(currentVersion);
		this.category = category;
		this.updateJSONUrl = updateJSONUrl;
	}
	
	@Override
	public UpdateCheckTask.Result get() {
        LOGGER.debug("Checking " + name + " for updates");

        ComparableVersion current = currentVersion;
        ComparableVersion solved = null;
        try {
        	solved = solveVersion();
        } catch(Exception e) {
        	LOGGER.log(getErrorLevel(), "Failed to retrieve update JSON for " + name + ": " + e.getMessage());
        }
        if (solved == null)
            return new UpdateCheckTask.Result(this);
        
        LOGGER.debug("Update version found for " + name + ": " + solved + " (running " + current + ")");

        return new UpdateCheckTask.Result(this, solved);
    }
	
	private Level getErrorLevel() {
		return !ConfigUCL.hideErrored ? Level.ERROR : Level.DEBUG;
	}
	
	private ComparableVersion solveVersion() throws Exception {
		if(category == null) return null;
		if(TEST_MODE && updateJSONUrl.startsWith(MOCK_PREFIX)) return mockSolveVersion();
		
        URL url = new URL(updateJSONUrl);
        InputStream contents = url.openStream();

        String jasonString = IOUtils.toString(contents, "UTF-8");

        JsonObject jason = new JsonParser().parse(jasonString).getAsJsonObject();
        
        JsonElement homepageElem = jason.get("homepage");
        if(homepageElem instanceof JsonPrimitive) {
        	homepage = homepageElem.getAsString();
        } else {
        	LOGGER.log(getErrorLevel(), "Failed to locate 'homepage' element in " + updateJSONUrl);
        }
        
        String channel = ConfigUCL.promoChannel;
        String promoKey = category.version + "-" + channel;
        
        JsonElement promos = jason.get("promos");
        if(promos instanceof JsonObject) {
        	JsonElement promoVersion = ((JsonObject)promos).get(promoKey);
        	
        	if(promoVersion != null) {
        		return new ComparableVersion(promoVersion.getAsString());
        	}
        }
        LOGGER.log(getErrorLevel(), "Failed to locate promos -> " + promoKey + " in " + updateJSONUrl);
        return null;
    }
	
	private ComparableVersion mockSolveVersion() {
		return new ComparableVersion(updateJSONUrl.substring(MOCK_PREFIX.length()));
	}
	
	public static class Result {
		UpdateCheckTask task;
		public ComparableVersion newVersion;
		
		public Result(UpdateCheckTask task, ComparableVersion newVersion) {
			this.task = task;
			this.newVersion = newVersion;
		}
		
		public Result(UpdateCheckTask task) {
			this(task, null);
		}
		
		public boolean foundUpdate() {
			return newVersion != null && newVersion.compareTo(task.currentVersion) > 0;
		}
		
		public boolean isInteresting() {
			return (!ConfigUCL.hideErrored && newVersion == null) || foundUpdate();
		}
	}
}
