package dev.workbuddy.betterfurnaces.menu;

import dev.workbuddy.betterfurnaces.item.UpgradeItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Hidden upgrade slot. Only upgrade items are accepted and the slot is never exposed to hoppers.
 */
public class UpgradeSlot extends Slot {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("container/slot");

	public UpgradeSlot(final Container container, final int slot, final int x, final int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(final ItemStack stack) {
		return stack.getItem() instanceof UpgradeItem && this.container.canPlaceItem(this.getContainerSlot(), stack);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public int getMaxStackSize(final ItemStack stack) {
		return 1;
	}

	@Override
	public Identifier getNoItemIcon() {
		return BACKGROUND;
	}
}
