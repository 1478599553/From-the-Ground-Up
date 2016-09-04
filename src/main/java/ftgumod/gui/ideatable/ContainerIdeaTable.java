package ftgumod.gui.ideatable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ftgumod.FTGUAPI;
import ftgumod.IdeaRecipe;
import ftgumod.Technology;
import ftgumod.TechnologyHandler;
import ftgumod.TechnologyUtil;
import ftgumod.gui.SlotSpecial;
import ftgumod.gui.TileEntityInventory;

public class ContainerIdeaTable extends Container {

	public final TileEntityInventory invInput;
	public final IInventory invResult = new InventoryCraftResult();
	public final InventoryPlayer invPlayer;

	public final int sizeInventory;

	public boolean possible;

	public int feather;
	public int parchment;
	public int combine;
	public int output;

	public ContainerIdeaTable(TileEntityInventory tileEntity, InventoryPlayer invPlayer) {
		this.invInput = tileEntity;
		this.invPlayer = invPlayer;

		sizeInventory = addSlots(tileEntity);

		for (int slotx = 0; slotx < 3; slotx++) {
			for (int sloty = 0; sloty < 9; sloty++) {
				addSlotToContainer(new Slot(invPlayer, sloty + slotx * 9 + 9, 8 + sloty * 18, 84 + slotx * 18));
			}
		}

		for (int slot = 0; slot < 9; slot++) {
			addSlotToContainer(new Slot(invPlayer, slot, 8 + slot * 18, 142));
		}

		onCraftMatrixChanged(tileEntity);
	}

	protected int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlotToContainer(new SlotSpecial(this, tileEntity, c, 37, 23, 1, new ItemStack(Items.FEATHER)));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(this, tileEntity, c, 59, 23, 64, new ItemStack(FTGUAPI.i_parchmentEmpty)));
		parchment = c;
		c++;

		combine = c;
		for (int slot = 0; slot < 3; slot++) {
			addSlotToContainer(new SlotSpecial(this, tileEntity, c, 30 + slot * 18, 45, 1));
			c++;
		}

		addSlotToContainer(new SlotIdeaTable(invPlayer.player, invResult, c, 124, 35, tileEntity));
		output = c;
		c++;

		return c;
	}

	public IdeaRecipe hasRecipe() {
		for (IdeaRecipe i : TechnologyHandler.ideas) {
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (int j = 0; j < 3; j++) {
				ItemStack s = inventorySlots.get(combine + j).getStack();
				if (s != null) {
					items.add(s);
				}
			}
			boolean recipe[] = new boolean[i.recipe.length];
			for (int j = 0; j < i.recipe.length; j++) {
				for (int x = 0; x < items.size(); x++) {
					if (TechnologyUtil.isEqual(i.recipe[j], items.get(x))) {
						items.remove(x);
						recipe[j] = true;
						break;
					}
				}
			}
			if (items.size() > 0) {
				continue;
			}
			boolean r = true;
			for (boolean b : recipe) {
				if (!b) {
					r = false;
				}
			}
			if (r) {
				return i;
			}
		}
		return null;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		if (inv == invInput) {
			possible = inventorySlots.get(feather).getHasStack() && inventorySlots.get(parchment).getHasStack();
			if (possible) {
				IdeaRecipe recipe = hasRecipe();

				if (recipe != null) {
					Technology tech = recipe.output;
					EntityPlayer player = invPlayer.player;
					if (!tech.researched && !tech.isResearched(player) && (tech.prev == null || tech.prev.isResearched(player))) {
						ItemStack result = new ItemStack(FTGUAPI.i_parchmentIdea);

						TechnologyUtil.getItemData(result).setString("FTGU", tech.getUnlocalisedName());

						inventorySlots.get(output).putStack(result);
						return;
					}
				}
			}
			inventorySlots.get(output).putStack(null);
		}
	}

	@Override
	public ItemStack slotClick(int index, int mouse, ClickType mode, EntityPlayer player) {
		ItemStack clickItemStack = super.slotClick(index, mouse, mode, player);

		onCraftMatrixChanged(invInput);
		if (index == output && inventorySlots.get(output).getHasStack()) {
			inventorySlots.get(parchment).decrStackSize(1);
			inventorySlots.get(output).putStack(null);

			for (int i = 0; i < 3; i++) {
				if (inventorySlots.get(combine + i).getStack() != null) {
					Item t = inventorySlots.get(combine + i).getStack().getItem();
					if (t.getContainerItem() != null)
						inventorySlots.get(combine + i).putStack(new ItemStack(t.getContainerItem()));
					else
						inventorySlots.get(combine + i).putStack(null);
				}
			}
		}

		return clickItemStack;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
		ItemStack itemStack1 = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack2 = slot.getStack();
			itemStack1 = itemStack2.copy();

			if (slotIndex == output) {
				if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, true)) {
					return null;
				}

				slot.onSlotChange(itemStack2, itemStack1);
			} else if (!(slotIndex < output)) {
				return null;
			} else if (!mergeItemStack(itemStack2, sizeInventory, sizeInventory + 36, false)) {
				return null;
			}

			if (itemStack2.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemStack2.stackSize == itemStack1.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(playerIn, itemStack2);
		}

		return itemStack1;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}