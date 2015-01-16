package mods.cartlivery.client.gui;

import mods.cartlivery.CommonProxy;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;
import mods.cartlivery.common.container.ContainerAutoCutter;
import mods.cartlivery.common.container.ContainerCutter;
import mods.cartlivery.common.network.LiveryGuiPatternMessage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiAutoCutter extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation("cartlivery", "textures/gui/autocutter.png");
    private ContainerAutoCutter container;
    private TileEntityAutoCutter tileAutoCutter;
    //String pattern = "";

    public GuiAutoCutter(InventoryPlayer inventoryPlayer, TileEntityAutoCutter tileEntityAutoCutter){
        super(new ContainerAutoCutter(inventoryPlayer, tileEntityAutoCutter));
        this.container = (ContainerAutoCutter) inventorySlots;
		//CommonProxy.network.sendToServer(new LiveryGuiPatternMessage(pattern));
		this.tileAutoCutter = tileEntityAutoCutter;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3){
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        //System.out.println(this.tileAutoCutter.cuttingTime);
        if (this.tileAutoCutter.isCutting())
        {
            int i1 = this.tileAutoCutter.getCutProgressScaled(24);
            this.drawTexturedModalRect(guiLeft + 76, guiTop + 14, 176, 0, i1 + 1, 16);
        }
    }
}
