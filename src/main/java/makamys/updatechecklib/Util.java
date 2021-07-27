package makamys.updatechecklib;

import java.net.URI;

public class Util {
	
	public static void openURLInBrowser(String url) {
		try {
			Class oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
			oclass.getMethod("browse", new Class[] { URI.class }).invoke(object, new Object[] { new URI(url) });
		} catch (Throwable throwable) {
			UpdateCheckLib.LOGGER.error("Couldn\'t open link", throwable);
		}
	}
	
}
