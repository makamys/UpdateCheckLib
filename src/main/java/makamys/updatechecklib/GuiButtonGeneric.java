package makamys.updatechecklib;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
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
    	
    	GuiScreen current = Minecraft.getMinecraft().currentScreen;
    	
    	boolean inWindow = this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    	
    	if(current != null && inWindow) {
    		int startY = 40;
    		int startX = 30;
    		int width = 150;
    		int textOff = 3;
    		
    		startX = mouseX - 70;
    		startY = mouseY - 30;
    		
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        Tessellator tessellator = Tessellator.instance;
	        tessellator.startDrawingQuads();
	        tessellator.setColorRGBA_F(0, 0, 0, 0.7f);
	        int p_73733_3_ = startX;
	        int p_73733_1_ = startX + width;
	        int p_73733_4_ = startY;
	        int p_73733_2_ = startY+30+5;
	        tessellator.addVertex((double)p_73733_3_, (double)p_73733_2_, (double)this.zLevel);
	        tessellator.addVertex((double)p_73733_1_, (double)p_73733_2_, (double)this.zLevel);
	        //tessellator.setColorRGBA_F(f5, f6, f7, f4);
	        tessellator.addVertex((double)p_73733_1_, (double)p_73733_4_, (double)this.zLevel);
	        tessellator.addVertex((double)p_73733_3_, (double)p_73733_4_, (double)this.zLevel);
	        tessellator.draw();
	        GL11.glShadeModel(GL11.GL_FLAT);
	        GL11.glDisable(GL11.GL_BLEND);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        
	        current.drawString(Minecraft.getMinecraft().fontRenderer, EnumChatFormatting.GREEN + "4" + EnumChatFormatting.RESET + " mod updates found.", startX + textOff, startY + textOff, 0xFFFFFF);
	        current.drawString(Minecraft.getMinecraft().fontRenderer, "Click to open list in browser.", startX + textOff, startY + textOff + 10, 0xFFFFFF);
	        current.drawString(Minecraft.getMinecraft().fontRenderer, "(Shift click to copy URL.)", startX + textOff, startY + textOff + 20, 0xBBBBBB);
    	}
    }
    
    public GuiButtonGeneric setClickListener(Runnable clickListener) {
    	this.clickListener = clickListener;
    	return this;
    }
}
