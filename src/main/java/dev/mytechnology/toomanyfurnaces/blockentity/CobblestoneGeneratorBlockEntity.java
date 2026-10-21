package dev.mytechnology.toomanyfurnaces.blockentity;

import dev.mytechnology.toomanyfurnaces.GeneratorProduct;
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
import net.minecraft.world.inventory.ContainerData;
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
 * Turns a lava source and a water source into the selected {@link GeneratorProduct}. The buckets
 * themselves are never consumed, they only prove that both fluids are supplied - either bucket may
 * sit in either fluid slot. Generation can be paused from the GUI and the product switched there too.
 */
public class CobblestoneGeneratorBlockEntity extends BlockEntity implements Container, MenuProvider {
	public static final int FLUID_SLOT_LEFT = 0;
	public static final int FLUID_SLOT_RIGHT = 1;
	public static final int OUTPUT_SLOT = 2;
	public static final int SIZE = 3;

	// Indices into the ContainerData synced to the menu, shared with CobblestoneGeneratorMenu.
	public static final int DATA_RUNNING = 0;
	public static final int DATA_PRODUCT = 1;
	public static final int DATA_PROGRESS = 2;
	public static final int DATA_COUNT = 3;

	private static final int MAX_BUFFER = 64;

	private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private boolean running = true;
	private GeneratorProduct product = GeneratorProduct.COBBLESTONE;
	private int progress;

	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(final int id) {
			return switch (id) {
				case DATA_RUNNING -> CobblestoneGeneratorBlockEntity.this.running ? 1 : 0;
				case DATA_PRODUCT -> CobblestoneGeneratorBlockEntity.this.product.ordinal();
				case DATA_PROGRESS -> CobblestoneGeneratorBlockEntity.this.progress;
				default -> 0;
			};
		}

		@Override
		public void set(final int id, final int value) {
			switch (id) {
				case DATA_RUNNING -> CobblestoneGeneratorBlockEntity.this.setRunning(value != 0);
				case DATA_PRODUCT -> CobblestoneGeneratorBlockEntity.this.setProduct(GeneratorProduct.byOrdinal(value));
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	public CobblestoneGeneratorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public static void tick(final net.minecraft.world.level.Level level, final BlockPos pos, final BlockState state, final CobblestoneGeneratorBlockEntity entity) {
		if (!(level instanceof ServerLevel)) {
			return;
		}

		if (!entity.running || !entity.hasBothFluids()) {
			return;
		}

		final ItemStack output = entity.items.get(OUTPUT_SLOT);
		if (!output.isEmpty() && (!output.is(entity.product.item()) || output.getCount() >= MAX_BUFFER)) {
			return;
		}

		entity.progress++;
		if (entity.progress >= entity.product.ticks()) {
			entity.progress = 0;
			if (output.isEmpty()) {
				entity.items.set(OUTPUT_SLOT, entity.product.stack());
			} else {
				output.grow(1);
			}

			entity.setChanged();
		}
	}

	private boolean hasBothFluids() {
		boolean lava = false;
		boolean water = false;
		for (final int slot : new int[]{FLUID_SLOT_LEFT, FLUID_SLOT_RIGHT}) {
			if (this.items.get(slot).is(Items.LAVA_BUCKET)) {
				lava = true;
			} else if (this.items.get(slot).is(Items.WATER_BUCKET)) {
				water = true;
			}
		}

		return lava && water;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void setRunning(final boolean running) {
		this.running = running;
		this.setChanged();
	}

	public GeneratorProduct getProduct() {
		return this.product;
	}

	public void setProduct(final GeneratorProduct product) {
		this.product = product;
		this.progress = 0;
		this.setChanged();
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
			// Either bucket fits either fluid slot; only a second bucket of the same fluid is refused.
			case FLUID_SLOT_LEFT, FLUID_SLOT_RIGHT -> (stack.is(Items.LAVA_BUCKET) || stack.is(Items.WATER_BUCKET))
				&& !stack.is(this.items.get(slot == FLUID_SLOT_LEFT ? FLUID_SLOT_RIGHT : FLUID_SLOT_LEFT).getItem());
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
		this.running = input.getBooleanOr("Running", true);
		this.product = GeneratorProduct.byName(input.getStringOr("Product", GeneratorProduct.COBBLESTONE.name()));
		this.progress = 0;
	}

	@Override
	protected void saveAdditional(final ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putBoolean("Running", this.running);
		output.putString("Product", this.product.name());
	}

	@Override
	public Component getDisplayName() {
		return this.getBlockState().getBlock().getName();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(final int containerId, final Inventory inventory, final Player player) {
		return new dev.mytechnology.toomanyfurnaces.menu.CobblestoneGeneratorMenu(containerId, inventory, this, this.dataAccess);
	}
}
