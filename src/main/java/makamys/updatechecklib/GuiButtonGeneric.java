package makamys.updatechecklib;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

/* Adapted from tconstruct's AbstractTab */
public class GuiButtonGeneric extends GuiButton {
	
	private Runnable clickListener;
	
    public GuiButtonGeneric(int id, int posX, int posY, int width, int height, String text) {
        super(id, posX, posY, width, height, text);
    }

    @Override
    public boolean mousePressed (Minecraft mc, int mouseX, int mouseY) {
        boolean inWindow = this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        
        if (inWindow) {
            this.onClicked();
        }

        return inWindow;
    }

    public void onClicked() {
    	if(clickListener != null) {
    		clickListener.run();
    	}
    }
    
    @Override
    public void drawButton(Minecraft p_146112_1_, int mouseX, int mouseY) {
    	super.drawButton(p_146112_1_, mouseX, mouseY);
    	
    	boolean inWindow = this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    	
    	if(inWindow) {
    		Util.drawSimpleTooltip(mouseX, mouseY, (int)this.zLevel, Arrays.asList(
	        		EnumChatFormatting.GREEN + "4" + EnumChatFormatting.RESET + " mod updates found.",
	        		"Click to open list in browser.",
	        		EnumChatFormatting.GRAY + "(Shift click to copy URL.)"));
    	}
    }
    
    public GuiButtonGeneric setClickListener(Runnable clickListener) {
    	this.clickListener = clickListener;
    	return this;
    }
}
