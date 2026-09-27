package dev.workbuddy.betterfurnaces.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Single input furnace. Slot layout: 0 input, 1 fuel, 2 output, plus three hidden upgrade slots.
 */
public class FurnaceBlockEntity extends AbstractSmeltingBlockEntity implements MenuProvider {
	private static final int[] INPUTS = new int[]{0};
	private static final int[] OUTPUTS = new int[]{2};

	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(final int id) {
			switch (id) {
				case 0 -> {
					return FurnaceBlockEntity.this.litTime;
				}
				case 1 -> {
					return FurnaceBlockEntity.this.litTotalTime;
				}
				case 2 -> {
					return FurnaceBlockEntity.this.cookingProgress[0];
				}
				case 3 -> {
					return FurnaceBlockEntity.this.getCookTicks();
				}
				default -> {
					return 0;
				}
			}
		}

		@Override
		public void set(final int id, final int value) {
			switch (id) {
				case 0 -> FurnaceBlockEntity.this.litTime = value;
				case 1 -> FurnaceBlockEntity.this.litTotalTime = value;
				case 2 -> FurnaceBlockEntity.this.cookingProgress[0] = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	public FurnaceBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state, 3);
	}

	@Override
	public int[] getInputSlots() {
		return INPUTS;
	}

	@Override
	public int getFuelSlot() {
		return 1;
	}

	@Override
	public int[] getOutputSlots() {
		return OUTPUTS;
	}

	@Override
	public int getCookTicks() {
		return this.getBlockState().getBlock() instanceof dev.workbuddy.betterfurnaces.block.TieredFurnaceBlock furnace
			? furnace.getTier().getCookTicks()
			: 200;
	}

	@Override
	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	@Override
	public Component getDisplayName() {
		return this.getBlockState().getBlock().getName();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(final int containerId, final Inventory inventory, final Player player) {
		return new dev.workbuddy.betterfurnaces.menu.BFRFurnaceMenu(containerId, inventory, this, this.dataAccess, this.getUpgradeContainer());
	}

	public static void tick(final Level level, final BlockPos pos, final BlockState state, final FurnaceBlockEntity entity) {
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			entity.serverTick(serverLevel, pos, state);
		}
	}

	public float getLitProgress() {
		if (this.litTotalTime == 0) {
			return 0.0F;
		}

		return net.minecraft.util.Mth.clamp((float)this.litTime / this.litTotalTime, 0.0F, 1.0F);
	}

	public float getCookProgress() {
		return net.minecraft.util.Mth.clamp((float)this.cookingProgress[0] / Math.max(1, this.getCookTicks()), 0.0F, 1.0F);
	}
}
