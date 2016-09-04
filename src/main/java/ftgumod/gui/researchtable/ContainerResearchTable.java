package ftgumod.gui.researchtable;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import ftgumod.CapabilityTechnology;
import ftgumod.CapabilityTechnology.ITechnology;
import ftgumod.Decipher;
import ftgumod.Decipher.DecipherGroup;
import ftgumod.FTGUAPI;
import ftgumod.ResearchRecipe;
import ftgumod.Technology;
import ftgumod.TechnologyHandler;
import ftgumod.TechnologyUtil;
import ftgumod.gui.SlotSpecial;
import ftgumod.gui.TileEntityInventory;
import ftgumod.item.ItemLookingGlass;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;

public class ContainerResearchTable extends Container {

	public final TileEntityInventory invInput;
	public final IInventory invResult = new InventoryCraftResult();
	public final InventoryPlayer invPlayer;

	public final int sizeInventory;

	public ResearchRecipe recipe = null;
	public boolean possible;

	public int feather;
	public int parchment;
	public int combine;
	public int output;
	public int glass;

	public ContainerResearchTable(TileEntityInventory tileEntity, InventoryPlayer invPlayer) {
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

		addSlotToContainer(new SlotSpecial(this, tileEntity, c, 8, 46, 1, new ItemStack(Items.FEATHER)));
		feather = c;
		c++;

		addSlotToContainer(new SlotSpecial(this, tileEntity, c, 8, 24, 1, new ItemStack(FTGUAPI.i_parchmentIdea)));
		parchment = c;
		c++;

		combine = c;
		for (int sloty = 0; sloty < 3; sloty++) {
			for (int slotx = 0; slotx < 3; slotx++) {
				addSlotToContainer(new SlotSpecial(this, tileEntity, c, 30 + slotx * 18, 17 + sloty * 18, 1));
				c++;
			}
		}

		addSlotToContainer(new SlotSpecial(this, tileEntity, c, 150, 35, 1, new ItemStack(FTGUAPI.i_lookingGlass)));
		glass = c;
		c++;

		addSlotToContainer(new SlotResearchTable(invPlayer.player, invResult, c, 124, 35, tileEntity));
		output = c;
		c++;

		return c;
	}

	public ResearchRecipe hasRecipe() {
		for (ResearchRecipe i : TechnologyHandler.researches) {
			boolean recipe[] = new boolean[9];
			for (int j = 0; j < 9; j++) {
				if (TechnologyUtil.isEqual(i.recipe[j], ((Slot) inventorySlots.get(combine + j)).getStack())) {
					recipe[j] = true;
				}
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
			boolean parch = inventorySlots.get(parchment).getHasStack();
			if (parch) {
				NBTTagCompound tag = TechnologyUtil.getItemData(inventorySlots.get(parchment).getStack());
				String s = tag.getString("FTGU");
				recipe = TechnologyHandler.getResearch(s);

				Technology tech = recipe.output;
				EntityPlayer player = invPlayer.player;

				if (tech.researched || tech.isResearched(player) || (tech.prev != null && !tech.prev.isResearched(player))) {
					recipe = null;
				}

				if (recipe != null && !player.worldObj.isRemote && TechnologyHandler.unlock.containsKey(recipe)) {
					ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
					if (!cap.isResearched(TechnologyHandler.UNDECIPHERED_RESEARCH.getUnlocalisedName() + ".unlock")) {
						cap.setResearched(TechnologyHandler.UNDECIPHERED_RESEARCH.getUnlocalisedName() + ".unlock");
						invPlayer.player.addChatMessage(new TextComponentString(I18n.translateToLocal("technology.complete.unlock") + " \"" + TechnologyHandler.UNDECIPHERED_RESEARCH.getLocalisedName() + "\"!"));
						player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
						PacketDispatcher.sendTo(new TechnologyMessage(player), (EntityPlayerMP) player);
					}
				}
			} else {
				recipe = null;
			}

			possible = inventorySlots.get(feather).getHasStack() && parch;
			if (possible) {
				ResearchRecipe recipe = hasRecipe();

				if (recipe != null && recipe == this.recipe) {
					Technology tech = recipe.output;
					EntityPlayer player = invPlayer.player;
					if (!tech.researched && !tech.isResearched(player) && (tech.prev == null || tech.prev.isResearched(player))) {
						if (TechnologyHandler.unlock.containsKey(recipe)) {
							if (!inventorySlots.get(glass).getHasStack()) {
								inventorySlots.get(output).putStack(null);
								return;
							}

							Decipher d = TechnologyHandler.unlock.get(recipe);
							List<String> items = ItemLookingGlass.getItems(inventorySlots.get(glass).getStack());
							for (DecipherGroup g : d.list) {
								boolean perms = false;
								for (ItemStack s : g.unlock)
									for (String t : items)
										if ((s.getItem() == null && t.equals("tile.null")) || (s.getItem() != null && s.getItem().getUnlocalizedName(s).equals(t)))
											perms = true;
								if (!perms) {
									inventorySlots.get(output).putStack(null);
									return;
								}
							}
						}

						ItemStack result = new ItemStack(FTGUAPI.i_parchmentResearch);

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

			for (int i = 0; i < 9; i++) {
				if (inventorySlots.get(combine + i).getStack() != null) {
					Item t = inventorySlots.get(combine + i).getStack().getItem();
					if (t.getContainerItem() != null)
						inventorySlots.get(combine + i).putStack(new ItemStack(t.getContainerItem()));
					else
						inventorySlots.get(combine + i).putStack(null);
				}
			}
			recipe = null;
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