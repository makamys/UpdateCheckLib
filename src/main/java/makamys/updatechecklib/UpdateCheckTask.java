package makamys.updatechecklib;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.versioning.ComparableVersion;
import makamys.updatechecklib.UpdateCheckLib.UpdateCategory;

import static makamys.updatechecklib.UpdateCheckLib.LOGGER;

class UpdateCheckTask implements Supplier<UpdateCheckTask.Result> {
    	
	String name;
	ComparableVersion currentVersion;
	UpdateCategory category;
	String updateJSONUrl;
	String updateUrl;
	
	public static final boolean TEST_MODE = Boolean.parseBoolean(System.getProperty("updateCheckLib.test", "false"));
	private static final String MOCK_PREFIX = "mock://";
	
	public UpdateCheckTask(String name, String currentVersion, UpdateCategory category, String updateJSONUrl, String updateUrl) {
		this.name = name;
		this.currentVersion = new ComparableVersion(currentVersion);
		this.category = category;
		this.updateJSONUrl = updateJSONUrl;
		this.updateUrl = updateUrl;
	}
	
	@Override
	public UpdateCheckTask.Result get() {
        LOGGER.debug("Checking " + name + " for updates");

        ComparableVersion current = currentVersion;
        ComparableVersion solved = null;
        try {
        	solved = solveVersion();
        } catch(Exception e) {
        	LOGGER.log(!ConfigUCL.hideErrored ? Level.ERROR : Level.DEBUG, "Failed to retrieve update JSON for " + name + ": " + e.getMessage());
        }
        if (solved == null)
            return new UpdateCheckTask.Result(this);
        
        LOGGER.debug("Update version found for " + name + ": " + solved + " (running " + current + ")");

        return new UpdateCheckTask.Result(this, solved);
    }
	
	private ComparableVersion solveVersion() throws Exception {
		if(category == null) return null;
		if(TEST_MODE && updateJSONUrl.startsWith(MOCK_PREFIX)) return mockSolveVersion();
		
        URL url = new URL(updateJSONUrl);
        InputStream contents = url.openStream();

        String jasonString = IOUtils.toString(contents, "UTF-8");

        JsonObject jason = new JsonParser().parse(jasonString).getAsJsonObject();
        
        JsonElement versions = jason.get(category.version);
        if(versions != null) {
            List<ComparableVersion> availableVersions = ((JsonObject)versions).entrySet().stream()
                    .map(name -> new ComparableVersion(name.getKey())).collect(Collectors.toList());
            ComparableVersion latest = Collections.max(availableVersions);

            return latest;            	
        }
        LOGGER.error("Update json " + updateJSONUrl + " contains no " + category.version + " element.");
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
