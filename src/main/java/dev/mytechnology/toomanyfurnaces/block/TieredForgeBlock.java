package dev.mytechnology.toomanyfurnaces.block;

import com.mojang.serialization.MapCodec;
import dev.mytechnology.toomanyfurnaces.FurnaceTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A tiered forge: same visual treatment as {@link TieredFurnaceBlock} but backed by the parallel
 * three-input {@link dev.mytechnology.toomanyfurnaces.blockentity.ForgeBlockEntity}.
 */
public class TieredForgeBlock extends AbstractFurnaceBlock {
	private final FurnaceTier tier;
	private final MapCodec<TieredForgeBlock> codec;

	public TieredForgeBlock(final FurnaceTier tier, final BlockBehaviour.Properties properties) {
		super(properties.lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0));
		this.tier = tier;
		this.codec = BlockBehaviour.simpleCodec(props -> new TieredForgeBlock(tier, props));
	}

	public static BlockBehaviour.Properties defaultProperties() {
		return BlockBehaviour.Properties.of().strength(4.0F).requiresCorrectToolForDrops().sound(SoundType.METAL);
	}

	public FurnaceTier getTier() {
		return this.tier;
	}

	@Override
	public MapCodec<TieredForgeBlock> codec() {
		return this.codec;
	}

	@Override
	public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
		return new dev.mytechnology.toomanyfurnaces.blockentity.ForgeBlockEntity(
			dev.mytechnology.toomanyfurnaces.ModRegistry.FORGE_BLOCK_ENTITY, pos, state
		);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, dev.mytechnology.toomanyfurnaces.ModRegistry.FORGE_BLOCK_ENTITY, dev.mytechnology.toomanyfurnaces.blockentity.ForgeBlockEntity::tick);
	}

	@Override
	protected void openContainer(final Level level, final BlockPos pos, final Player player) {
		final BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof dev.mytechnology.toomanyfurnaces.blockentity.ForgeBlockEntity forge) {
			player.openMenu(forge);
		}
	}

	/**
	 * Held upgrade items are applied before the menu opens: the vanilla dispatch calls
	 * {@code useWithoutItem} (and therefore {@link #openContainer}) whenever {@code useItemOn}
	 * returns {@code TRY_WITH_EMPTY_HAND}.
	 */
	@Override
	protected InteractionResult useItemOn(
		final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult
	) {
		final InteractionResult applied = dev.mytechnology.toomanyfurnaces.item.UpgradeItem.applyHeld(stack, level, pos, player);
		return applied.consumesAction() ? applied : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	protected boolean shouldChangedStateKeepBlockEntity(final BlockState oldState) {
		return oldState.getBlock() instanceof TieredForgeBlock;
	}

	@Override
	public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}

		final double x = pos.getX() + 0.5;
		final double y = pos.getY() + 0.5;
		final double z = pos.getZ() + 0.5;
		if (random.nextDouble() < 0.1) {
			level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		}

		final Direction direction = state.getValue(FACING);
		final Direction.Axis axis = direction.getAxis();
		final double offset = random.nextDouble() * 0.6 - 0.3;
		final double dx = axis == Direction.Axis.X ? direction.getStepX() * 0.62 : offset;
		final double dy = random.nextDouble() * 6.0 / 16.0;
		final double dz = axis == Direction.Axis.Z ? direction.getStepZ() * 0.62 : offset;
		level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
		level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
	}
}
