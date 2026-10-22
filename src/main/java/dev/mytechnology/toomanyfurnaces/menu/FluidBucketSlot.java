package dev.mytechnology.toomanyfurnaces.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Fluid slot of the cobblestone generator. The left slot only accepts lava buckets and the right
 * slot only water buckets, so the two inputs are no longer interchangeable. Acceptance is fully
 * delegated to the block entity, mirroring {@link FuelSlot}.
 */
public class FluidBucketSlot extends Slot {
	private final Item acceptedItem;

	public FluidBucketSlot(final Container container, final int slot, final int x, final int y, final Item acceptedItem) {
		super(container, slot, x, y);
		this.acceptedItem = acceptedItem;
	}

	@Override
	public boolean mayPlace(final ItemStack stack) {
		return stack.is(this.acceptedItem) && this.container.canPlaceItem(this.getContainerSlot(), stack);
	}
}
