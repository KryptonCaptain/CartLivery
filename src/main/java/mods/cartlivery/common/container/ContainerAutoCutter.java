package mods.cartlivery.common.container;

import cofh.api.energy.EnergyStorage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.cartlivery.common.block.tileentity.TileEntityAutoCutter;
import mods.cartlivery.common.item.ItemSticker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAutoCutter extends Container {

	public TileEntityAutoCutter tileAutoCutter;
	public static EntityPlayer player;
	private int lastCuttingTime;
	private int lastEnergyStorage;

	public ContainerAutoCutter(InventoryPlayer player, TileEntityAutoCutter tileEntityAutoCutter) {

		ContainerAutoCutter.player = player.player;
		this.tileAutoCutter = tileEntityAutoCutter;

		addSlot(tileEntityAutoCutter, 0, 52, 21);
		addSlot(tileEntityAutoCutter, 1, 80, 51);
		addSlot(tileEntityAutoCutter, 2, 108, 21);

		bindPlayerInventory(player);
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting icrafting)
    {
        super.addCraftingToCrafters(icrafting);
        icrafting.sendProgressBarUpdate(this, 0, this.tileAutoCutter.cuttingTime);
        
        EnergyStorage storage = this.tileAutoCutter.getEnergyStorage();
        if (storage != null)
            icrafting.sendProgressBarUpdate(this, 1, storage.getEnergyStored());
    }
	
	/**
     * Looks for changes made in the container, sends them to every listener.
     */
	@Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        
        EnergyStorage storage = this.tileAutoCutter.getEnergyStorage();
        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.lastCuttingTime != this.tileAutoCutter.cuttingTime)
            {
                icrafting.sendProgressBarUpdate(this, 0, this.tileAutoCutter.cuttingTime);
            }
            if (storage != null)
            {
            	icrafting.sendProgressBarUpdate(this, 1, storage.getEnergyStored());
            }
        }

        this.lastCuttingTime = this.tileAutoCutter.cuttingTime;
        //this.lastEnergyStorage = this.tileAutoCutter.getEnergyStorage().getEnergyStored();
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int p_75137_1_, int p_75137_2_)
    {
    	switch(p_75137_1_){
	    	case 0:
	    		this.tileAutoCutter.cuttingTime = p_75137_2_;
	    		break;
	    	case 1:
	    		this.tileAutoCutter.getEnergyStorage().setEnergyStored(p_75137_2_);
	    		break;
    	}
    }

	private void addSlot(IInventory inv, final int id, int x, int y) {
		addSlotToContainer(new Slot(inv, id, x, y) {

			@Override
			public boolean isItemValid(ItemStack arg0) {
				return inventory.isItemValidForSlot(id, arg0);
			}
		});
	}

	protected void bindPlayerInventory(InventoryPlayer invPlayer) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlotToContainer(new Slot(invPlayer, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
			}
		}

		for (int slotNumber = 0; slotNumber < 9; slotNumber++) {
			addSlotToContainer(new Slot(invPlayer, slotNumber, 8 + slotNumber * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileAutoCutter.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNumber) {
		ItemStack itemStack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotNumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack1 = slot.getStack();
			itemStack = itemStack1.copy();

			// if we are in the auto Cutter
			if (slotNumber <= 2) {
				if (!this.mergeItemStack(itemStack1, 3, 38, false)) {
					return null;
				}
				// otherwise just put it in one of the machine slots
			} else if (!this.mergeItemStack(itemStack1, 0, 3, false)) {
				return null;
			}

			if (itemStack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			if (itemStack1.stackSize == itemStack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, itemStack1);
		}

		return itemStack;
	}

	/**
	 * Added validation of slot input
	 * 
	 * @author CrazyPants
	 */
	@Override
	protected boolean mergeItemStack(ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {

		boolean result = false;
		int checkIndex = fromIndex;

		if (reversOrder) {
			checkIndex = toIndex - 1;
		}

		Slot slot;
		ItemStack itemstack1;

		if (par1ItemStack.isStackable()) {

			while (par1ItemStack.stackSize > 0 && (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex)) {
				slot = (Slot) this.inventorySlots.get(checkIndex);
				itemstack1 = slot.getStack();

				if (itemstack1 != null && itemstack1.getItem() == par1ItemStack.getItem() && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == itemstack1.getItemDamage())
						&& ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1) && slot.isItemValid(par1ItemStack)) {

					int mergedSize = itemstack1.stackSize + par1ItemStack.stackSize;
					int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getSlotStackLimit());
					if (mergedSize <= maxStackSize) {
						par1ItemStack.stackSize = 0;
						itemstack1.stackSize = mergedSize;
						slot.onSlotChanged();
						result = true;
					} else if (itemstack1.stackSize < maxStackSize) {
						par1ItemStack.stackSize -= maxStackSize - itemstack1.stackSize;
						itemstack1.stackSize = maxStackSize;
						slot.onSlotChanged();
						result = true;
					}
				}

				if (reversOrder) {
					--checkIndex;
				} else {
					++checkIndex;
				}
			}
		}

		if (par1ItemStack.stackSize > 0) {
			if (reversOrder) {
				checkIndex = toIndex - 1;
			} else {
				checkIndex = fromIndex;
			}

			while (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex) {
				slot = (Slot) this.inventorySlots.get(checkIndex);
				itemstack1 = slot.getStack();

				if (itemstack1 == null && slot.isItemValid(par1ItemStack)) {
					ItemStack in = par1ItemStack.copy();
					in.stackSize = Math.min(in.stackSize, slot.getSlotStackLimit());

					slot.putStack(in);
					slot.onSlotChanged();
					if (in.stackSize >= par1ItemStack.stackSize) {
						par1ItemStack.stackSize = 0;
					} else {
						par1ItemStack.stackSize -= in.stackSize;
					}
					result = true;
					break;
				}

				if (reversOrder) {
					--checkIndex;
				} else {
					++checkIndex;
				}
			}
		}

		return result;
	}
}