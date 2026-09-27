package dev.workbuddy.toomanyfurnaces.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Forge: three fully independent input slots sharing a single fuel slot, three outputs, plus three
 * hidden upgrade slots. Slot layout: 0-2 inputs, 3 fuel, 4-6 outputs.
 */
public class ForgeBlockEntity extends AbstractSmeltingBlockEntity implements MenuProvider {
	private static final int[] INPUTS = new int[]{0, 1, 2};
	private static final int[] OUTPUTS = new int[]{4, 5, 6};

	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(final int id) {
			return switch (id) {
				case 0 -> ForgeBlockEntity.this.litTime;
				case 1 -> ForgeBlockEntity.this.litTotalTime;
				case 2 -> ForgeBlockEntity.this.getCookTicks();
				case 3 -> ForgeBlockEntity.this.cookingProgress[0];
				case 4 -> ForgeBlockEntity.this.cookingProgress[1];
				case 5 -> ForgeBlockEntity.this.cookingProgress[2];
				default -> 0;
			};
		}

		@Override
		public void set(final int id, final int value) {
			switch (id) {
				case 0 -> ForgeBlockEntity.this.litTime = value;
				case 1 -> ForgeBlockEntity.this.litTotalTime = value;
				case 3 -> ForgeBlockEntity.this.cookingProgress[0] = value;
				case 4 -> ForgeBlockEntity.this.cookingProgress[1] = value;
				case 5 -> ForgeBlockEntity.this.cookingProgress[2] = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return 6;
		}
	};

	public ForgeBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state, 7);
	}

	@Override
	public int[] getInputSlots() {
		return INPUTS;
	}

	@Override
	public int getFuelSlot() {
		return 3;
	}

	@Override
	public int[] getOutputSlots() {
		return OUTPUTS;
	}

	@Override
	public int getCookTicks() {
		return this.getBlockState().getBlock() instanceof dev.workbuddy.toomanyfurnaces.block.TieredForgeBlock forge
			? forge.getTier().getCookTicks()
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
		return new dev.workbuddy.toomanyfurnaces.menu.ForgeMenu(containerId, inventory, this, this.getUpgradeContainer(), this.dataAccess);
	}

	public static void tick(final Level level, final BlockPos pos, final BlockState state, final ForgeBlockEntity entity) {
		if (level instanceof ServerLevel serverLevel) {
			entity.serverTick(serverLevel, pos, state);
		}
	}

	public float getCookProgress(final int index) {
		return net.minecraft.util.Mth.clamp((float)this.cookingProgress[index] / Math.max(1, this.getCookTicks()), 0.0F, 1.0F);
	}

	public float getLitProgress() {
		if (this.litTotalTime == 0) {
			return 0.0F;
		}

		return net.minecraft.util.Mth.clamp((float)this.litTime / this.litTotalTime, 0.0F, 1.0F);
	}
}
