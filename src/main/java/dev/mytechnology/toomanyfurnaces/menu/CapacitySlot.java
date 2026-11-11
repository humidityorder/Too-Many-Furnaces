package dev.mytechnology.toomanyfurnaces.menu;

import dev.mytechnology.toomanyfurnaces.blockentity.AbstractSmeltingBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Input slot whose maximum stack size follows the smelter's slot capacity, so the storage upgrade's
 * 128 item limit also applies to items inserted by hand or with shift-click (not just automation).
 */
public class CapacitySlot extends Slot {
	private final @Nullable AbstractSmeltingBlockEntity smelter;

	public CapacitySlot(final Container container, final int slot, final int x, final int y, final @Nullable AbstractSmeltingBlockEntity smelter) {
		super(container, slot, x, y);
		this.smelter = smelter;
	}

	@Override
	public int getMaxStackSize(final ItemStack stack) {
		return this.smelter == null
			? super.getMaxStackSize(stack)
			: this.smelter.getSlotCapacity(this.getContainerSlot(), stack);
	}
}
