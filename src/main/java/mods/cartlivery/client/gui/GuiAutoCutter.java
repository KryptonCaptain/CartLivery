package mods.cartlivery.client.gui;

import java.util.List;
import java.util.ArrayList;

import mods.cartlivery.CartConfig;
import mods.cartlivery.CommonProxy;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;
import mods.cartlivery.common.container.ContainerAutoCutter;
import mods.cartlivery.common.container.ContainerCutter;
import mods.cartlivery.common.network.LiveryGuiPatternMessage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiAutoCutter extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation("cartlivery", "textures/gui/autocutter.png");
    private ContainerAutoCutter container;
    private TileEntityAutoCutter tileAutoCutter;

    public GuiAutoCutter(InventoryPlayer inventoryPlayer, TileEntityAutoCutter tileEntityAutoCutter){
        super(new ContainerAutoCutter(inventoryPlayer, tileEntityAutoCutter));
        this.container = (ContainerAutoCutter) inventorySlots;
		//CommonProxy.network.sendToServer(new LiveryGuiPatternMessage(pattern));
		this.tileAutoCutter = tileEntityAutoCutter;
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
    	String s = this.tileAutoCutter.getInventoryName();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3){
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        if (this.tileAutoCutter.isCutting())
        {
            int i1 = this.tileAutoCutter.getCutProgressScaled(24);
            this.drawTexturedModalRect(guiLeft + 76, guiTop + 20, 176, 0, i1 + 1, 16);
        }
        if (CartConfig.AUTOCUTTER_USES_RF && this.tileAutoCutter.getEnergyStorage()!=null)
        {
        	this.drawTexturedModalRect(guiLeft + 154, guiTop + 17, 176, 17, 14, 50);
            int i1 = this.tileAutoCutter.getEnergyStorageScaled(50);
            this.drawTexturedModalRect(guiLeft + 154, guiTop + 17 + (50 - i1), 190,  17 + (50 - i1), 14, i1);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float par3)
    {
        super.drawScreen(mouseX, mouseY, par3);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) guiLeft, (float) guiTop, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        
        int k = (this.width - this.xSize) / 2; //X asis on GUI
        int l = (this.height - this.ySize) / 2; //Y asis on GUI
        if (CartConfig.AUTOCUTTER_USES_RF && this.tileAutoCutter.getEnergyStorage()!=null){
	        if (mouseX > guiLeft + 154 && mouseX < guiLeft + 154 + 12) //Basically checking if mouse is in the correct area
	        {
		        if (mouseY > guiTop + 17 && mouseY < guiTop + 17 + 48)
		        {
			        List list = new ArrayList<String>();
			        list.add(this.tileAutoCutter.getEnergyStorage().getEnergyStored() + " RF");
			        this.drawHoveringText(list, (int)mouseX - k, (int)mouseY - l, this.fontRendererObj);
		        }
	        }
	    }
        
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
