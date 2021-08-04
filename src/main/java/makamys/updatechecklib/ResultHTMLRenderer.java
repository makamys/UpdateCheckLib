package makamys.updatechecklib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import makamys.updatechecklib.UpdateCheckLib.UpdateCategory;

import static makamys.updatechecklib.UpdateCheckLib.LOGGER;

public class ResultHTMLRenderer {
	
	private static final String TABLE_TEMPLATE = 
			  "<h1>%s</h1>\n"
			+ "<table>\n"
			+ "	<thead>\n"
			+ "		<th>%s</th>\n"
			+ "		<th>%s</th>\n"
			+ "		<th>%s</th>\n"
			+ "		<th>%s</th>\n"
			+ "	</thead>\n"
			+ "	%s\n"
			+ "</table>\n\n";
	
	private static final String TABLE_ROW_TEMPLATE = 
			"	<tr>\n"
			+ "		<td>%s</td>\n"
			+ "		<td>%s</td>\n"
			+ "		<td>%s</td>\n"
			+ "		<td><a href=\"%s\">%s</a></td>\n"
			+ "	</tr>";
	
	private static final String
	FIELD_NAME = "Name",
	FIELD_CURRENT_VERSION = "Installed version",
	FIELD_NEW_VERSION = "Latest version",
	FIELD_URL = "Update link",
	TABLE_TITLE_TEMPLATE = "%s updates";
	
	public ResultHTMLRenderer() {
		
	}
	
	private boolean hasAnythingToDisplay() {
		return UpdateCheckLib.categories.values().stream().anyMatch(cat -> cat.results.stream().anyMatch(r -> r.isInteresting()));
	}
	
	public boolean render(File outFile) {
		if(!hasAnythingToDisplay()) {
			outFile.delete();
		} else {
			try (FileOutputStream out = new FileOutputStream(outFile)){
				String template = IOUtils.toString(ResultHTMLRenderer.class.getClassLoader().getResourceAsStream("resources/updates.template.html"));
				String html = String.format(template, generateTables());
				IOUtils.write(html, out, "utf8");
				LOGGER.info("Wrote update check results to " + outFile);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private String generateTables() {
		final StringBuffer tables = new StringBuffer();
		
		Stream.concat(Arrays.asList(UpdateCheckLib.MODS).stream(), UpdateCheckLib.categories.values().stream().sorted().filter(c -> c != UpdateCheckLib.MODS)).forEach(cat -> {
			List<UpdateCheckTask.Result> interestingResults = cat.results.stream().filter(r -> r.isInteresting()).collect(Collectors.toList());
			
			if(!interestingResults.isEmpty()) {
				String tableTitle = cat.displayName;
				String rows = "";
				for(UpdateCheckTask.Result result : interestingResults) {
					String newVersionStr = result.newVersion != null ? result.newVersion.toString() : "<b>ERROR</b>";
					rows += String.format(TABLE_ROW_TEMPLATE, result.task.name, result.task.currentVersion, newVersionStr, result.task.homepage, result.task.homepage);
				}
				
				tables.append(String.format(TABLE_TEMPLATE, String.format(TABLE_TITLE_TEMPLATE, tableTitle), FIELD_NAME, FIELD_CURRENT_VERSION, FIELD_NEW_VERSION, FIELD_URL, rows));
			}
		});
		return tables.toString();
	}
	
}
