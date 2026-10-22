package dev.mytechnology.toomanyfurnaces.menu;

import dev.mytechnology.toomanyfurnaces.GeneratorProduct;
import dev.mytechnology.toomanyfurnaces.ModRegistry;
import dev.mytechnology.toomanyfurnaces.blockentity.CobblestoneGeneratorBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Cobblestone generator: two fluid slots (a lava bucket on the left, a water bucket on the right)
 * and the resulting block, laid out on the slot frames baked into the original GUI texture. The GUI
 * buttons pause/resume the generator and cycle the produced block via {@link #clickMenuButton}.
 */
public class CobblestoneGeneratorMenu extends AbstractContainerMenu {
	public static final int FLUID_SLOT_LEFT = CobblestoneGeneratorBlockEntity.FLUID_SLOT_LEFT;
	public static final int FLUID_SLOT_RIGHT = CobblestoneGeneratorBlockEntity.FLUID_SLOT_RIGHT;
	public static final int OUTPUT_SLOT = CobblestoneGeneratorBlockEntity.OUTPUT_SLOT;
	public static final int SLOT_COUNT = 3;

	// Button ids sent through ServerboundContainerButtonClickPacket.
	public static final int BUTTON_TOGGLE_RUNNING = 0;
	public static final int BUTTON_CYCLE_PRODUCT = 1;

	private final ContainerData data;

	public CobblestoneGeneratorMenu(final int containerId, final Inventory inventory, final Container container, final ContainerData data) {
		super(ModRegistry.COBBLESTONE_GENERATOR_MENU_TYPE, containerId);
		checkContainerSize(container, SLOT_COUNT);
		checkContainerDataCount(data, CobblestoneGeneratorBlockEntity.DATA_COUNT);
		this.data = data;
		this.addSlot(new FluidBucketSlot(container, FLUID_SLOT_LEFT, 53, 27, Items.LAVA_BUCKET));
		this.addSlot(new FluidBucketSlot(container, FLUID_SLOT_RIGHT, 108, 27, Items.WATER_BUCKET));
		this.addSlot(new Slot(container, OUTPUT_SLOT, 80, 45));
		this.addStandardInventorySlots(inventory, 8, 84);
		this.addDataSlots(data);
	}

	@Override
	public boolean clickMenuButton(final Player player, final int id) {
		switch (id) {
			case BUTTON_TOGGLE_RUNNING -> this.data.set(CobblestoneGeneratorBlockEntity.DATA_RUNNING,
				this.data.get(CobblestoneGeneratorBlockEntity.DATA_RUNNING) == 0 ? 1 : 0);
			case BUTTON_CYCLE_PRODUCT -> this.data.set(CobblestoneGeneratorBlockEntity.DATA_PRODUCT,
				(this.data.get(CobblestoneGeneratorBlockEntity.DATA_PRODUCT) + 1) % GeneratorProduct.values().length);
			default -> {
				return false;
			}
		}

		return true;
	}

	public boolean isRunning() {
		return this.data.get(CobblestoneGeneratorBlockEntity.DATA_RUNNING) != 0;
	}

	public GeneratorProduct getProduct() {
		return GeneratorProduct.byOrdinal(this.data.get(CobblestoneGeneratorBlockEntity.DATA_PRODUCT));
	}

	public int getProgress() {
		return this.data.get(CobblestoneGeneratorBlockEntity.DATA_PROGRESS);
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
		} else if (stack.is(Items.LAVA_BUCKET)) {
			// Shift-clicking a lava bucket drops it into the left slot, shift-clicking a water
			// bucket into the right one.
			if (!this.moveItemStackTo(stack, FLUID_SLOT_LEFT, FLUID_SLOT_LEFT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.is(Items.WATER_BUCKET)) {
			if (!this.moveItemStackTo(stack, FLUID_SLOT_RIGHT, FLUID_SLOT_RIGHT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, OUTPUT_SLOT, SLOT_COUNT, false)) {
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
