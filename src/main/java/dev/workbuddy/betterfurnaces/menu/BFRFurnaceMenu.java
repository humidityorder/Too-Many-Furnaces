package dev.workbuddy.betterfurnaces.menu;

import dev.workbuddy.betterfurnaces.ModRegistry;
import dev.workbuddy.betterfurnaces.blockentity.AbstractSmeltingBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import org.jspecify.annotations.Nullable;

/**
 * Reuses the vanilla {@link AbstractFurnaceMenu} logic (and therefore the vanilla furnace screen)
 * and appends the three hidden upgrade slots on the left column.
 */
public class BFRFurnaceMenu extends AbstractFurnaceMenu {
	public static final int UPGRADE_SLOT_START = 39;
	public static final int UPGRADE_SLOT_END = 42;

	public BFRFurnaceMenu(final int containerId, final Inventory inventory, final Container container, final ContainerData data, final Container upgrades) {
		super(ModRegistry.FURNACE_MENU_TYPE, RecipePropertySet.FURNACE_INPUT, RecipeBookType.FURNACE, containerId, inventory, container, data);
		final AbstractSmeltingBlockEntity smelter = container instanceof AbstractSmeltingBlockEntity found ? found : null;
		// Re-align the vanilla slots with the Better Furnaces GUI texture (frames at 53/17 and 53/53).
		final Slot inputSlot = new Slot(container, 0, 54, 18);
		inputSlot.index = 0;
		this.slots.set(0, inputSlot);
		final FuelSlot fuelSlot = new FuelSlot(container, 1, 54, 54);
		fuelSlot.index = 1;
		this.slots.set(1, fuelSlot);
		final SmeltingResultSlot resultSlot = new SmeltingResultSlot(inventory.player, container, 2, 116, 35, smelter);
		resultSlot.index = 2;
		this.slots.set(2, resultSlot);
		for (int i = 0; i < AbstractSmeltingBlockEntity.UPGRADE_COUNT; i++) {
			this.addSlot(new UpgradeSlot(upgrades, i, 8, 18 + i * 18));
		}
	}

	@Nullable
	public AbstractSmeltingBlockEntity getSmelter() {
		return this.slots.get(0).container instanceof AbstractSmeltingBlockEntity smelter ? smelter : null;
	}

	@Override
	public ItemStack quickMoveStack(final Player player, final int slotIndex) {
		ItemStack clicked = ItemStack.EMPTY;
		final Slot slot = this.slots.get(slotIndex);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = slot.getItem();
		clicked = stack.copy();
		if (slotIndex == 2) {
			if (!this.moveItemStackTo(stack, 3, 39, true)) {
				return ItemStack.EMPTY;
			}

			slot.onQuickCraft(stack, clicked);
		} else if (slotIndex >= UPGRADE_SLOT_START && slotIndex < UPGRADE_SLOT_END) {
			if (!this.moveItemStackTo(stack, 3, 39, false)) {
				return ItemStack.EMPTY;
			}
		} else if (slotIndex != 1 && slotIndex != 0) {
			if (this.canSmelt(stack)) {
				if (!this.moveItemStackTo(stack, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.isFuel(stack)) {
				if (!this.moveItemStackTo(stack, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (stack.getItem() instanceof dev.workbuddy.betterfurnaces.item.UpgradeItem) {
				if (!this.moveItemStackTo(stack, UPGRADE_SLOT_START, UPGRADE_SLOT_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotIndex >= 3 && slotIndex < 30) {
				if (!this.moveItemStackTo(stack, 30, 39, false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotIndex >= 30 && slotIndex < 39 && !this.moveItemStackTo(stack, 3, 30, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, 3, 39, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (stack.getCount() == clicked.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, stack);
		return clicked;
	}
}
