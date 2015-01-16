package mods.cartlivery;

import mods.cartlivery.client.gui.GuiAutoCutter;
import mods.cartlivery.client.gui.GuiCutter;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;
import mods.cartlivery.common.container.ContainerAutoCutter;
import mods.cartlivery.common.container.ContainerCutter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID){
	        case 0:
	        	return new ContainerCutter(player);
	        case 1:
	        	TileEntity tileentity = world.getTileEntity(x, y, z);
	        	if (tileentity instanceof TileEntityAutoCutter)
	        		return new ContainerAutoCutter(player.inventory, (TileEntityAutoCutter) tileentity);
	        default:
	            return null;
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID){
			case 0:
				return new GuiCutter(player);
	        case 1:
	        	TileEntity tileentity = world.getTileEntity(x, y, z);
	        	if (tileentity instanceof TileEntityAutoCutter)
	        		return new GuiAutoCutter(player.inventory, (TileEntityAutoCutter) tileentity);
	        default:
	            return null;
	    }
	}
}
