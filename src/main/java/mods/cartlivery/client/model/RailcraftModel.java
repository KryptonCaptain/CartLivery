package mods.cartlivery.client.model;

import mods.cartlivery.CartConfig;
import mods.cartlivery.client.LiveryTextureRegistry;
import mods.railcraft.client.emblems.EmblemToolsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RailcraftModel extends ModelCartLivery{
	public void render(int baseColor, int patternColor, String pattern, String emblem, float par2, float par3, float par4, float par5, float par6, float par7) {
		super.render(baseColor, patternColor, pattern, emblem, par2, par3, par4, par5, par6, par7);
		if (CartConfig.ENABLE_EMBLEMS && emblem != null && !emblem.isEmpty())
	    {
			ResourceLocation emblemTexture = null;
		    if ((emblem != null) && (!emblem.equals("")) && (EmblemToolsClient.packageManager != null)) {
		    	emblemTexture = EmblemToolsClient.packageManager.getEmblemTextureLocation(emblem);
		    }
		    GL11.glPushMatrix();
	    	Minecraft.getMinecraft().renderEngine.bindTexture(emblemTexture);
	    	Tessellator tess = Tessellator.instance;
	    	
	    	tess.startDrawingQuads();
	    	tess.addVertexWithUV(this.emblemOffsetX - this.emblemSize, this.emblemOffsetY - this.emblemSize, this.emblemOffsetZ, 0.0D, 0.0D);
	    	tess.addVertexWithUV(this.emblemOffsetX - this.emblemSize, this.emblemOffsetY + this.emblemSize, this.emblemOffsetZ, 0.0D, 1.0D);
	    	tess.addVertexWithUV(this.emblemOffsetX + this.emblemSize, this.emblemOffsetY + this.emblemSize, this.emblemOffsetZ, 1.0D, 1.0D);
	    	tess.addVertexWithUV(this.emblemOffsetX + this.emblemSize, this.emblemOffsetY + -this.emblemSize, this.emblemOffsetZ, 1.0D, 0.0D);
	    	
	    	tess.addVertexWithUV(this.emblemOffsetX + this.emblemSize, this.emblemOffsetY + -this.emblemSize, -this.emblemOffsetZ, 0.0D, 0.0D);
	    	tess.addVertexWithUV(this.emblemOffsetX + this.emblemSize, this.emblemOffsetY + this.emblemSize, -this.emblemOffsetZ, 0.0D, 1.0D);
	    	tess.addVertexWithUV(this.emblemOffsetX - this.emblemSize, this.emblemOffsetY + this.emblemSize, -this.emblemOffsetZ, 1.0D, 1.0D);
	    	tess.addVertexWithUV(this.emblemOffsetX - this.emblemSize, this.emblemOffsetY - this.emblemSize, -this.emblemOffsetZ, 1.0D, 0.0D);
	    	tess.draw();
	    	GL11.glPopMatrix();
	    }
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
