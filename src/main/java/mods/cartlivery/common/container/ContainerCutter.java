package mods.cartlivery.common.container;

import mods.cartlivery.CartConfig;
import mods.cartlivery.common.item.ItemSticker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

public class ContainerCutter extends Container {

	public String pattern = "";
	public EntityPlayer player;
	public int toolIndex;
	public IInventory inventoryInput;
	public IInventory inventoryOutput;
	
	public ContainerCutter(EntityPlayer player) {
		this.player = player;
		toolIndex = player.inventory.currentItem;
		this.inventoryInput = new InventoryCrafting(this, 1, 1);
		this.inventoryOutput = new InventoryCraftResult();
		
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                addSlotToContainer(new Slot(player.inventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));

        for (int i = 0; i < 9; ++i)
            if (i != toolIndex) {
            	addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
            } else {
            	addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142) {
					@Override
					public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
						return false;
					}
            	});
            }
        
        addSlotToContainer(new Slot(inventoryInput, 0, 106, 36) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == Items.paper;
			}
        });
        addSlotToContainer(new SlotCrafting(player, inventoryInput, inventoryOutput, 0, 144, 36) {
			@Override
			public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
				super.onPickupFromSlot(player, stack);
				damageTool();
				if(CartConfig.PLAY_SOUNDS)
					player.playSound("CartLivery:sticker_cut", 1.0F, 1.0F);
			}
        });
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		ItemStack drop = inventoryInput.getStackInSlotOnClosing(0);
		if (drop != null) player.dropPlayerItemWithRandomChoice(drop, false);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inventoryInput.getStackInSlot(0) == null || pattern.isEmpty()) {
			inventoryOutput.setInventorySlotContents(0, null);
		} else {
			inventoryOutput.setInventorySlotContents(0, ItemSticker.create(pattern));			
		}
	}

	protected void damageTool() {
		ItemStack tool = player.inventory.getStackInSlot(toolIndex);
		tool.setItemDamage(tool.getItemDamage() + 1);
		if (tool.getItemDamage() > tool.getMaxDamage()) {
			player.inventory.setInventorySlotContents(toolIndex, null);
			player.closeScreen();
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNumber) {
		ItemStack itemStack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotNumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack1 = slot.getStack();
			itemStack = itemStack1.copy();

			// if we are in the Cutter
			if (slotNumber == 36 || slotNumber == 37) {
				if (!this.mergeItemStack(itemStack1, 0, 35, false)) {
					return null;
				}
				// otherwise just put it in one of the machine slots
			} else if (!this.mergeItemStack(itemStack1, 36, 37, false)) {
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
