package mods.cartlivery.common.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.cartlivery.common.block.BlockAutoCutter;
import mods.cartlivery.common.item.ItemSticker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

public class TileEntityAutoCutter extends TileEntity implements ISidedInventory {

	private ItemStack[] inventory = new ItemStack[3];
	private String name = "Livery Cutting Press";
	
	/** The number of ticks that the current sticker has been cutting for */
    public int cuttingTime;
	
	/**
     * autoCutter is cutting
     */
    public boolean isCutting()
    {
        return this.cuttingTime > 0;
    }
    
    /**
     * Returns an integer between 0 and the passed value representing how close the current sticker is to being completely
     * cut
     */
    @SideOnly(Side.CLIENT)
    public int getCutProgressScaled(int p_145953_1_)
    {
        return this.cuttingTime * p_145953_1_ / 200;
    }

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		if (inventory[slot] != null) {
			ItemStack is;
			if (inventory[slot].stackSize <= size) {
				is = inventory[slot];
				inventory[slot] = null;
				return is;
			} else {
				is = inventory[slot].splitStack(size);
				if (inventory[slot].stackSize == 0)
					inventory[slot] = null;
				return is;
			}
		} else
			return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList tags = nbt.getTagList("Items", 10);
		inventory = new ItemStack[getSizeInventory()];

		this.cuttingTime = nbt.getShort("CutTime");
		for (int i = 0; i < tags.tagCount(); i++) {
			NBTTagCompound data = tags.getCompoundTagAt(i);
			int j = data.getByte("Slot") & 255;

			if (j >= 0 && j < inventory.length) {
				inventory[j] = ItemStack.loadItemStackFromNBT(data);
			}
		}

		if (nbt.hasKey("CustomName", 8)) {
			this.name = nbt.getString("CustomName");
		}
	}

	@Override
	public void updateEntity() {
		ItemStack base = inventory[0], template = inventory[1], output = inventory[2];
		if (!worldObj.isRemote /*&& worldObj.getTotalWorldTime() % 40 == 0*/) {
			if (base != null && template != null) {
				if (canBeMadeFrom(base, template)) {
					// the max possible for this craft
					int canBeMade = template.getMaxStackSize();

					// if there are items in the output, they count towards the max we can make
					if (output != null) {
						canBeMade -= output.stackSize;
					}

					// can't make more than we have
					canBeMade = Math.min(base.stackSize, canBeMade);

					// if we can't make any, forget it
					if (canBeMade <= 0) {
						return;
					}

					// result will always be a copy of the template, of course
					ItemStack Cut = ItemSticker.create(template.getTagCompound().getString("pattern"));
					
					// if we can place the result in the output
					if (canMerge(Cut)/* && this.isCutting()*/) {
						this.cuttingTime++;
			            if (this.cuttingTime == 196)
			            {
			            	BlockAutoCutter.updateBlockState(true, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			            	this.worldObj.playSoundEffect((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D, "mob.sheep.shear", 1.0F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			            }
			            if (this.cuttingTime == 200)
			            {
			            	BlockAutoCutter.updateBlockState(false, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			            	//this.worldObj.playSoundEffect((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D, "mob.sheep.shear", 1.0F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			                this.cuttingTime = 0;
			                
			                // if our output is empty, just use the current result
							if (output == null) {
								inventory[2] = Cut;
							} else {
								// otherwise just add our result to the existing stack
								inventory[2].stackSize += Cut.stackSize;
							}
							// remove what we made from the stack
							base.stackSize -= Cut.stackSize;
							if (base.stackSize <= 0) {
								inventory[0] = null; // clear out base size itemstacks
							}
							// we changed something, so we need to tell the chunk to save
							markDirty();
			            }
					}
					else
					{
						this.cuttingTime = 0;
					}
				}
				else
				{
					this.cuttingTime = 0;
				}
			}
			else
			{
				this.cuttingTime = 0;
			}
		}
	}

	// lets make sure the user isn't trying to make something from a block that doesn't have this as a valid template
	private boolean canBeMadeFrom(ItemStack from, ItemStack to) {
		return from.getItem() == Items.paper && to.getItem() instanceof ItemSticker;
	}

	private boolean canMerge(ItemStack toMerge) {
		// if the output slot is empty we can merge without checking
		if (inventory[2] == null) {
			return true;
		}
		// need to check NBT as well as item
		if (toMerge.isItemEqual(inventory[2]) && ItemStack.areItemStackTagsEqual(toMerge, inventory[2])) {
			// we only care about metadata if the item has subtypes
			return toMerge.getHasSubtypes() && toMerge.getItemDamage() == inventory[2].getItemDamage();
		}

		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setShort("CutTime", (short)this.cuttingTime);
		NBTTagList tags = new NBTTagList();

		for (int i = 0; i < inventory.length; i++) {
			if (inventory[i] != null) {
				NBTTagCompound data = new NBTTagCompound();
				data.setByte("Slot", (byte) i);
				inventory[i].writeToNBT(data);
				tags.appendTag(data);
			}
		}

		nbt.setTag("Items", tags);

		if (this.hasCustomInventoryName()) {
			nbt.setString("CustomName", this.name);
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory[slot];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (inventory[slot] != null) {
			ItemStack is = inventory[slot];
			inventory[slot] = null;
			return is;
		} else
			return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public String getInventoryName() {
		return name;
	}

	@Override
	public final boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 2 };
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}

		switch (slot) {
		case 0:
			return itemStack.getItem() == Items.paper;
		case 1:
			return itemStack.getItem() instanceof ItemSticker;
		case 2:
			return false;
		default:
			return false;
		}
	}

	@Override
	public void openInventory() {
		;
	}

	@Override
	public void closeInventory() {
		;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack item, int side) {
		switch (slot) {
		case 0:
			return item.getItem() == Items.paper;
		case 1:
			return item.getItem() instanceof ItemSticker;
		case 2:
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack item, int side) {
		switch (slot) {
		case 0:
			return false;
		case 1:
			return false;
		case 2:
			return true;
		default:
			return false;
		}
	}
}