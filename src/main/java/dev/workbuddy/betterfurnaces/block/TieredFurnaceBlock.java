package dev.workbuddy.betterfurnaces.block;

import com.mojang.serialization.MapCodec;
import dev.workbuddy.betterfurnaces.FurnaceTier;
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
 * A tiered furnace. Rendering is a plain baked cube model switched by the {@code lit} block state;
 * there is no block entity renderer and no per-frame client work beyond vanilla particles.
 */
public class TieredFurnaceBlock extends AbstractFurnaceBlock {
	private final FurnaceTier tier;
	private final MapCodec<TieredFurnaceBlock> codec;

	public TieredFurnaceBlock(final FurnaceTier tier, final BlockBehaviour.Properties properties) {
		super(properties.lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0));
		this.tier = tier;
		this.codec = BlockBehaviour.simpleCodec(props -> new TieredFurnaceBlock(tier, props));
	}

	public static BlockBehaviour.Properties defaultProperties() {
		return BlockBehaviour.Properties.of().strength(3.5F).requiresCorrectToolForDrops().sound(SoundType.METAL);
	}

	public FurnaceTier getTier() {
		return this.tier;
	}

	@Override
	public MapCodec<TieredFurnaceBlock> codec() {
		return this.codec;
	}

	@Override
	public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
		return new dev.workbuddy.betterfurnaces.blockentity.FurnaceBlockEntity(
			dev.workbuddy.betterfurnaces.ModRegistry.FURNACE_BLOCK_ENTITY, pos, state
		);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
		return level.isClientSide()
			? null
			: createTickerHelper(type, dev.workbuddy.betterfurnaces.ModRegistry.FURNACE_BLOCK_ENTITY, dev.workbuddy.betterfurnaces.blockentity.FurnaceBlockEntity::tick);
	}

	@Override
	protected void openContainer(final Level level, final BlockPos pos, final Player player) {
		final BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof dev.workbuddy.betterfurnaces.blockentity.FurnaceBlockEntity furnace) {
			player.openMenu(furnace);
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
		final InteractionResult applied = dev.workbuddy.betterfurnaces.item.UpgradeItem.applyHeld(stack, level, pos, player);
		return applied.consumesAction() ? applied : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	/**
	 * Keeps the block entity (and therefore the inventory, upgrades and NBT) when a tier upgrade
	 * swaps one tiered furnace for another.
	 */
	@Override
	protected boolean shouldChangedStateKeepBlockEntity(final BlockState oldState) {
		return oldState.getBlock() instanceof TieredFurnaceBlock;
	}

	@Override
	public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}

		final double x = pos.getX() + 0.5;
		final double y = pos.getY();
		final double z = pos.getZ() + 0.5;
		if (random.nextDouble() < 0.1) {
			level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		}

		final Direction direction = state.getValue(FACING);
		final Direction.Axis axis = direction.getAxis();
		final double offset = random.nextDouble() * 0.6 - 0.3;
		final double dx = axis == Direction.Axis.X ? direction.getStepX() * 0.52 : offset;
		final double dy = random.nextDouble() * 6.0 / 16.0;
		final double dz = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52 : offset;
		level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
		level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
	}
}
