package dev.workbuddy.toomanyfurnaces.menu;

import dev.workbuddy.toomanyfurnaces.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FuelVerifierMenu extends AbstractContainerMenu {
	private final ContainerData data;

	public FuelVerifierMenu(final int containerId, final Inventory inventory, final Container container, final ContainerData data) {
		super(ModRegistry.FUEL_VERIFIER_MENU_TYPE, containerId);
		checkContainerSize(container, 1);
		checkContainerDataCount(data, 2);
		this.data = data;
		this.addSlot(new FuelSlot(container, 0, 80, 48));
		this.addStandardInventorySlots(inventory, 8, 84);
		this.addDataSlots(data);
	}

	public int getBurnTime() {
		return this.data.get(0);
	}

	@Override
	public boolean stillValid(final Player player) {
		return this.slots.get(0).container.stillValid(player);
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
		if (slotIndex == 0) {
			if (!this.moveItemStackTo(stack, 1, 37, true)) {
				return ItemStack.EMPTY;
			}

			slot.onQuickCraft(stack, clicked);
		} else if (!this.moveItemStackTo(stack, 0, 1, false)) {
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
