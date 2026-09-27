package dev.workbuddy.betterfurnaces.menu;

import dev.workbuddy.betterfurnaces.ModRegistry;
import dev.workbuddy.betterfurnaces.blockentity.AbstractSmeltingBlockEntity;
import dev.workbuddy.betterfurnaces.blockentity.ForgeBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Forge menu: three independent inputs (0-2) sharing one fuel slot (3), three outputs (4-6) and
 * three hidden upgrade slots (7-9).
 */
public class ForgeMenu extends AbstractContainerMenu {
	public static final int INPUT_SLOTS = 3;
	public static final int FUEL_SLOT = 3;
	public static final int OUTPUT_START = 4;
	public static final int OUTPUT_END = 7;
	public static final int UPGRADE_START = 7;
	public static final int UPGRADE_END = 10;
	public static final int INV_START = 10;
	public static final int INV_END = 46;

	private final ContainerData data;

	public ForgeMenu(final int containerId, final Inventory inventory, final Container container, final Container upgrades, final ContainerData data) {
		super(ModRegistry.FORGE_MENU_TYPE, containerId);
		checkContainerSize(container, 7);
		checkContainerDataCount(data, 6);
		this.data = data;

		for (int i = 0; i < INPUT_SLOTS; i++) {
			this.addSlot(new Slot(container, i, 27 + i * 18, 62));
		}

		this.addSlot(new FuelSlot(container, FUEL_SLOT, 8, 100));
		final AbstractSmeltingBlockEntity smelter = container instanceof AbstractSmeltingBlockEntity found ? found : null;
		for (int i = 0; i < OUTPUT_END - OUTPUT_START; i++) {
			this.addSlot(new SmeltingResultSlot(inventory.player, container, OUTPUT_START + i, 108 + i * 18, 80, smelter));
		}

		for (int i = 0; i < UPGRADE_END - UPGRADE_START; i++) {
			this.addSlot(new UpgradeSlot(upgrades, i, 7 + i * 18, 5));
		}

		this.addStandardInventorySlots(inventory, 8, 126);
		this.addDataSlots(data);
	}

	@Nullable
	public ForgeBlockEntity getForge() {
		return this.slots.get(0).container instanceof ForgeBlockEntity forge ? forge : null;
	}

	/** Reads the synced container data so the progress bars also work on the client. */
	public float getCookProgress(final int lane) {
		final int total = Math.max(1, this.data.get(2));
		return net.minecraft.util.Mth.clamp((float)this.data.get(3 + lane) / total, 0.0F, 1.0F);
	}

	public float getLitProgress() {
		final int total = this.data.get(1);
		return total == 0 ? 0.0F : net.minecraft.util.Mth.clamp((float)this.data.get(0) / total, 0.0F, 1.0F);
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
		if (slotIndex >= OUTPUT_START && slotIndex < OUTPUT_END) {
			if (!this.moveItemStackTo(stack, INV_START, INV_END, true)) {
				return ItemStack.EMPTY;
			}

			slot.onQuickCraft(stack, clicked);
		} else if (slotIndex >= UPGRADE_START && slotIndex < UPGRADE_END) {
			if (!this.moveItemStackTo(stack, INV_START, INV_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (slotIndex < OUTPUT_START) {
			if (!this.moveItemStackTo(stack, INV_START, INV_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (this.isFuel(stack, player)) {
			if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.getItem() instanceof dev.workbuddy.betterfurnaces.item.UpgradeItem) {
			if (!this.moveItemStackTo(stack, UPGRADE_START, UPGRADE_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (this.isSmeltable(stack, player)) {
			if (!this.moveItemStackTo(stack, 0, INPUT_SLOTS, false)) {
				return ItemStack.EMPTY;
			}
		} else if (slotIndex >= INV_START && slotIndex < INV_START + 27) {
			if (!this.moveItemStackTo(stack, INV_START + 27, INV_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (slotIndex >= INV_START + 27 && slotIndex < INV_END && !this.moveItemStackTo(stack, INV_START, INV_START + 27, false)) {
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

	private boolean isFuel(final ItemStack stack, final Player player) {
		return !stack.isEmpty() && player.level().fuelValues().isFuel(stack);
	}

	private boolean isSmeltable(final ItemStack stack, final Player player) {
		if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
			return false;
		}

		final ForgeBlockEntity forge = this.getForge();
		return forge != null && forge.hasRecipe(serverLevel, stack);
	}
}
