package dev.workbuddy.betterfurnaces.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Turns a lava source and a water source into cobblestone. The buckets themselves are never consumed,
 * they only prove that both fluids are supplied. Runs once per second on the server ticker only.
 */
public class CobblestoneGeneratorBlockEntity extends BlockEntity implements Container, MenuProvider {
	public static final int LAVA_SLOT = 0;
	public static final int WATER_SLOT = 1;
	public static final int OUTPUT_SLOT = 2;
	public static final int SIZE = 3;
	private static final int GENERATE_INTERVAL = 20;
	private static final int MAX_BUFFER = 64;

	private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private int tickCount;

	public CobblestoneGeneratorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public static void tick(final net.minecraft.world.level.Level level, final BlockPos pos, final BlockState state, final CobblestoneGeneratorBlockEntity entity) {
		if (!(level instanceof ServerLevel)) {
			return;
		}

		entity.tickCount++;
		if (entity.tickCount % GENERATE_INTERVAL != 0) {
			return;
		}

		if (!entity.items.get(LAVA_SLOT).is(Items.LAVA_BUCKET) || !entity.items.get(WATER_SLOT).is(Items.WATER_BUCKET)) {
			return;
		}

		final ItemStack output = entity.items.get(OUTPUT_SLOT);
		final ItemStack cobble = new ItemStack(Items.COBBLESTONE);
		if (output.isEmpty()) {
			entity.items.set(OUTPUT_SLOT, cobble);
			entity.setChanged();
		} else if (output.is(Items.COBBLESTONE) && output.getCount() < MAX_BUFFER) {
			output.grow(1);
			entity.setChanged();
		}
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(final int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(final int slot, final int amount) {
		final ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
		if (!result.isEmpty()) {
			this.setChanged();
		}

		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(final int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(final int slot, final ItemStack stack) {
		this.items.set(slot, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		this.setChanged();
	}

	@Override
	public void setChanged() {
		if (this.level != null) {
			super.setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(final int slot, final ItemStack stack) {
		return switch (slot) {
			case LAVA_SLOT -> stack.is(Items.LAVA_BUCKET);
			case WATER_SLOT -> stack.is(Items.WATER_BUCKET);
			default -> false;
		};
	}

	@Override
	public boolean stillValid(final Player player) {
		return this.level != null
			&& this.level.getBlockEntity(this.worldPosition) == this
			&& player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= 64.0;
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	protected void loadAdditional(final ValueInput input) {
		super.loadAdditional(input);
		ContainerHelper.loadAllItems(input, this.items);
	}

	@Override
	protected void saveAdditional(final ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
	}

	@Override
	public Component getDisplayName() {
		return this.getBlockState().getBlock().getName();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(final int containerId, final Inventory inventory, final Player player) {
		return new dev.workbuddy.betterfurnaces.menu.CobblestoneGeneratorMenu(containerId, inventory, this);
	}
}
