package makamys.updatechecklib;

import java.net.URI;
import java.net.URISyntaxException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

/* Adapted from GuiConfirmOpenLink */

@SideOnly(Side.CLIENT)
public class GuiUpdateNotification extends GuiYesNo
{
    /** Text to warn players from opening unsafe links. */
    private final String openLinkWarning;
    /** Label for the Copy to Clipboard button. */
    private final String copyLinkButtonText;
    private final String url;
    private boolean field_146360_u = true;
    private static final String __OBFID = "CL_00000683";

    private GuiScreen parent;
    
    public GuiUpdateNotification(GuiScreen parent, String url, boolean trusted)
    {
        super(null, "", "", -1);
        this.parent = parent;
        this.confirmButtonText = I18n.format(trusted ? "chat.link.open" : "gui.yes", new Object[0]);
        this.cancelButtonText = I18n.format(trusted ? "gui.cancel" : "gui.no", new Object[0]);
        this.copyLinkButtonText = I18n.format("chat.copy", new Object[0]);
        this.openLinkWarning = I18n.format("chat.link.warning", new Object[0]);
        this.url = url;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.add(new GuiButton(0, this.width / 3 - 83 + 0, this.height / 6 + 96, 100, 20, this.confirmButtonText));
        this.buttonList.add(new GuiButton(2, this.width / 3 - 83 + 105, this.height / 6 + 96, 100, 20, this.copyLinkButtonText));
        this.buttonList.add(new GuiButton(1, this.width / 3 - 83 + 210, this.height / 6 + 96, 100, 20, this.cancelButtonText));
    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
    	switch(p_146284_1_.id) {
    	case 0:
    		openURLInBrowser(url);
    		break;
    	case 1:
    		Minecraft.getMinecraft().displayGuiScreen(parent);
    		break;
    	case 2:
            this.copyLinkToClipboard();
            break;
        }

        //this.parentScreen.confirmClicked(p_146284_1_.id == 0, this.field_146357_i);
    }
    
    private void openURLInBrowser(String url) {
    	try {
    		openURIInBrowser(new URI(url));
    	} catch(URISyntaxException e) {
    		UpdateCheckLib.LOGGER.error("Invalid URI: " + url);
    	}
    }
    
	private void openURIInBrowser(URI p_146407_1_) {
		try {
			Class oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
			oclass.getMethod("browse", new Class[] { URI.class }).invoke(object, new Object[] { p_146407_1_ });
		} catch (Throwable throwable) {
			UpdateCheckLib.LOGGER.error("Couldn\'t open link", throwable);
		}
	}

    /**
     * Copies the link to the system clipboard.
     */
    public void copyLinkToClipboard()
    {
        setClipboardString(this.url);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        
        this.drawCenteredString(this.fontRendererObj, "Updates were found for " + EnumChatFormatting.GREEN + "4" + EnumChatFormatting.RESET + " mods.", this.width / 2, 70, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "The list has been written to " + EnumChatFormatting.YELLOW + "updates.html" + EnumChatFormatting.RESET + " in your Minecraft directory.", this.width / 2, 90, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Do you want to open it now?", this.width / 2, 110, 0xFFFFFF);
    }

    public void func_146358_g()
    {
        this.field_146360_u = false;
    }
}
