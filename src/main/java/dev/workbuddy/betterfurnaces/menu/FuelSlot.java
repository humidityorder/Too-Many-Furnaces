package dev.workbuddy.betterfurnaces.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Fuel slot; acceptance is fully delegated to the block entity so liquid fuel rules stay in one place.
 */
public class FuelSlot extends Slot {
	public FuelSlot(final Container container, final int slot, final int x, final int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(final ItemStack stack) {
		return this.container.canPlaceItem(this.getContainerSlot(), stack);
	}

	@Override
	public int getMaxStackSize(final ItemStack stack) {
		return this.container instanceof dev.workbuddy.betterfurnaces.blockentity.AbstractSmeltingBlockEntity smelter
			? Math.min(99, smelter.getSlotCapacity(stack))
			: super.getMaxStackSize(stack);
	}
}
