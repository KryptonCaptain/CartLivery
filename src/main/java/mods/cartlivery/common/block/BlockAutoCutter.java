package mods.cartlivery.common.block;

import mods.cartlivery.ModCartLivery;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;

import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockAutoCutter extends BlockContainer
{
	private final boolean cutting;
	private static boolean swap;
	//@SideOnly(Side.CLIENT)
	private IIcon[] icons = new IIcon[2];
	
    public BlockAutoCutter(boolean flag)
    {
        super(Material.anvil);
        setBlockName("autoCutter");
        setHardness(1F);
        cutting=flag;
    }
    
    @Override
    public String getUnlocalizedName()
    {
        return String.format("tile.%s%s", ModCartLivery.MOD_ID.toLowerCase() + ":", "autoCutter");
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
    	if(side<=1)
    		return this.icons[0];
    	return this.icons[1];
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
    	this.icons[0] = reg.registerIcon("cartlivery:autocutter_top");
    	this.icons[1] = reg.registerIcon(this.cutting ? "cartlivery:autocutter_closed" : "cartlivery:autocutter_open");
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
        if(!swap) dropItems(world, x, y, z);
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
    
    public static void updateBlockState(boolean flag, World world, int x, int y, int z)
    {
        int l = world.getBlockMetadata(x, y, z);
        TileEntity tileentity = world.getTileEntity(x, y, z);
        swap = true;

        if (flag)
        {
            world.setBlock(x, y, z, GameRegistry.findBlock("CartLivery", "autoCutterLive"));
        }
        else
        {
            world.setBlock(x, y, z, GameRegistry.findBlock("CartLivery", "autoCutter"));
        }

        swap = false;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);

        if (tileentity != null)
        {
            tileentity.validate();
            world.setTileEntity(x, y, z, tileentity);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityAutoCutter();
    }
}
