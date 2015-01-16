package mods.cartlivery.common.block;

import mods.cartlivery.ModCartLivery;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAutoCutter extends BlockContainer
{
    public BlockAutoCutter()
    {
        super(Material.anvil);
        setBlockName("autoCutter");
        setHardness(1F);
        setBlockTextureName("cartlivery:autocutter");
        setCreativeTab(CreativeTabs.tabDecorations);
    }
    
    @Override
    public String getUnlocalizedName()
    {
        return String.format("tile.%s%s", ModCartLivery.MOD_ID.toLowerCase() + ":", "autoCutter");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float x1, float y1, float z1){
        TileEntity tile = world.getTileEntity(x, y, z);
        if(world.isRemote)
            return true;
         if(tile !=null && tile instanceof TileEntityAutoCutter)
        	 player.openGui(ModCartLivery.instance, 1, world, x, y, z);
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6){
        dropItems(world, x, y, z);
        super.breakBlock(world, x, y, z, block, par6);
    }

    private void dropItems(World world, int x, int y, int z){
        Random random = new Random();
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if(!(tileEntity instanceof IInventory)){
            return;
        }

        IInventory inventory = (IInventory) tileEntity;

        for(int c = 0; c < inventory.getSizeInventory(); c++){
            ItemStack stack = inventory.getStackInSlot(c);

            if(stack != null && stack.stackSize > 0){
                float rx = random.nextFloat() * 0.8F + 0.1F;
                float ry = random.nextFloat() * 0.8F + 0.1F;
                float rz = random.nextFloat() * 0.8F + 0.1F;
                EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, stack);

                if(stack.hasTagCompound()){
                    entityItem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                }

                float factor = 0.05F;
                entityItem.motionX = random.nextGaussian() * factor;
                entityItem.motionY = random.nextGaussian() * factor + 0.2F;
                entityItem.motionZ = random.nextGaussian() * factor;
                world.spawnEntityInWorld(entityItem);
                stack.stackSize = 0;
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityAutoCutter();
    }
}
