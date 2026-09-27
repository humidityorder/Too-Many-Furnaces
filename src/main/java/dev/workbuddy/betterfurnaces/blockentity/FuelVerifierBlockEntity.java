package dev.workbuddy.betterfurnaces.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Small utility block that reports how many items a single piece of fuel can smelt.
 */
public class FuelVerifierBlockEntity extends BlockEntity implements Container, MenuProvider {
	private static final int BURN_TIME = 0;
	private static final int TOTAL_TIME = 1;

	private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(final int id) {
			return switch (id) {
				case BURN_TIME -> FuelVerifierBlockEntity.this.getBurnTime();
				case TOTAL_TIME -> 200;
				default -> 0;
			};
		}

		@Override
		public void set(final int id, final int value) {
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public FuelVerifierBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public int getBurnTime() {
		final ItemStack stack = this.items.get(0);
		return stack.isEmpty() || this.level == null ? 0 : this.level.fuelValues().burnDuration(stack);
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.items.get(0).isEmpty();
	}

	@Override
	public ItemStack getItem(final int slot) {
		return this.items.get(0);
	}

	@Override
	public ItemStack removeItem(final int slot, final int amount) {
		final ItemStack result = ContainerHelper.removeItem(this.items, 0, amount);
		if (!result.isEmpty()) {
			this.setChanged();
		}

		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(final int slot) {
		return ContainerHelper.takeItem(this.items, 0);
	}

	@Override
	public void setItem(final int slot, final ItemStack stack) {
		this.items.set(0, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		this.setChanged();
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
		return new dev.workbuddy.betterfurnaces.menu.FuelVerifierMenu(containerId, inventory, this, this.dataAccess);
	}
}
