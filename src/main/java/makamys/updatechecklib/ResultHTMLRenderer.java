package makamys.updatechecklib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import static makamys.updatechecklib.UpdateCheckLib.LOGGER;

public class ResultHTMLRenderer {
	
	List<UpdateCheckTask.Result> results;
	
	public ResultHTMLRenderer(List<UpdateCheckTask.Result> results) {
		this.results = results;
	}
	
	public boolean render(File outFile) {
		try (FileOutputStream out = new FileOutputStream(outFile)){
			String template = IOUtils.toString(ResultHTMLRenderer.class.getClassLoader().getResourceAsStream("resources/updates.template.html"));
			IOUtils.write(template, out, "utf8");
			LOGGER.info("Wrote update check results to " + outFile);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
