package makamys.updatechecklib.gui;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;

import makamys.updatechecklib.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class GuiButtonUpdates extends GuiButtonGeneric {

	int updateCount;
	String url;
	
	public GuiButtonUpdates(int id, int posX, int posY, int width, int height, int updateCount, String url) {
		super(id, posX, posY, width, height, EnumChatFormatting.GREEN + "+" + updateCount);
		this.updateCount = updateCount;
		this.url = url;
	}
	
	@Override
	public List<String> getTooltipStrings() {
		String plural = updateCount != 1 ? "s" : "";
		return Arrays.asList(
        		"" + EnumChatFormatting.GREEN + updateCount + EnumChatFormatting.RESET + " update" + plural + " available.",
        		"Click to open list in browser.",
        		EnumChatFormatting.GRAY + "(Shift click to copy URL.)");
	}
	
	@Override
	public void onClicked() {
		super.onClicked();
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			GuiScreen.setClipboardString(url);
		} else {
			Util.openURLInBrowser(url);
		}
	}

}
