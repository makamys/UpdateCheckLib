package makamys.updatechecklib;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.versioning.ComparableVersion;
import makamys.updatechecklib.UpdateCheckLib.UpdateCategory;

import static makamys.updatechecklib.UpdateCheckLib.LOGGER;

class UpdateCheckTask implements Supplier<UpdateCheckTask.Result> {
    	
	private String name;
	private String currentVersion;
	private UpdateCategory category;
	private String updateJSONUrl;
	
	public UpdateCheckTask(String name, String currentVersion, UpdateCategory category, String updateJSONUrl) {
		this.name = name;
		this.currentVersion = currentVersion;
		this.category = category;
		this.updateJSONUrl = updateJSONUrl;
	}
	
	@Override
	public UpdateCheckTask.Result get() {
        LOGGER.debug("Checking " + name + " for updates");

        ComparableVersion current = new ComparableVersion(currentVersion);
        ComparableVersion solved = null;
        try {
        	solved = solveVersion();
        } catch(Exception e) {
        	LOGGER.error("Failed to retrieve update JSON for " + name + ": " + e.getMessage());
        }
        if (solved == null)
            return null;
        
        LOGGER.debug("Update version found for " + name + ": " + solved + " (running " + current + ")");

        return new UpdateCheckTask.Result(solved);
    }
	
	private ComparableVersion solveVersion() throws Exception {
		if(category == null) return null;
		
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
	
	public static class Result {
		public ComparableVersion newVersion;
		
		public Result(ComparableVersion newVersion) {
			this.newVersion = newVersion;
		}
	}
}
