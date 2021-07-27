package makamys.updatechecklib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

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
    
    public GuiButtonGeneric setClickListener(Runnable clickListener) {
    	this.clickListener = clickListener;
    	return this;
    }
}
