package dev.mytechnology.toomanyfurnaces.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Output slot of the cobblestone generator. Like the vanilla furnace result slot it only displays
 * the generated block: players can take items out but cannot insert anything.
 */
public class GeneratorOutputSlot extends Slot {
	public GeneratorOutputSlot(final Container container, final int slot, final int x, final int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(final ItemStack stack) {
		return false;
	}
}
