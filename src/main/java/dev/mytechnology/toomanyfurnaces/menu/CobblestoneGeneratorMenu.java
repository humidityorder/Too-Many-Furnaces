package dev.mytechnology.toomanyfurnaces.menu;

import dev.mytechnology.toomanyfurnaces.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Cobblestone generator: a lava source, a water source and the resulting cobblestone, laid out on the
 * slot frames baked into the original GUI texture.
 */
public class CobblestoneGeneratorMenu extends AbstractContainerMenu {
	public static final int LAVA_SLOT = 0;
	public static final int WATER_SLOT = 1;
	public static final int OUTPUT_SLOT = 2;
	public static final int SLOT_COUNT = 3;

	public CobblestoneGeneratorMenu(final int containerId, final Inventory inventory, final Container container) {
		super(ModRegistry.COBBLESTONE_GENERATOR_MENU_TYPE, containerId);
		checkContainerSize(container, SLOT_COUNT);
		this.addSlot(new Slot(container, LAVA_SLOT, 53, 27));
		this.addSlot(new Slot(container, WATER_SLOT, 108, 27));
		this.addSlot(new Slot(container, OUTPUT_SLOT, 80, 45));
		this.addStandardInventorySlots(inventory, 8, 84);
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
		if (slotIndex < SLOT_COUNT) {
			if (!this.moveItemStackTo(stack, SLOT_COUNT, SLOT_COUNT + 36, true)) {
				return ItemStack.EMPTY;
			}

			slot.onQuickCraft(stack, clicked);
		} else if (!this.moveItemStackTo(stack, 0, SLOT_COUNT, false)) {
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
