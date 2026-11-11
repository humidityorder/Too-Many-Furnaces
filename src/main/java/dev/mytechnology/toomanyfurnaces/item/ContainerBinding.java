package dev.mytechnology.toomanyfurnaces.item;

import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.blockentity.AbstractSmeltingBlockEntity;
import dev.mytechnology.toomanyfurnaces.blockentity.CobblestoneGeneratorBlockEntity;
import dev.mytechnology.toomanyfurnaces.blockentity.FuelVerifierBlockEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Chest binding shared by the three automation upgrades (auto input, auto output and auto
 * input/output).
 * <p>
 * The binding is stored in the vanilla {@link DataComponents#CUSTOM_DATA} component, so it survives
 * being inserted into a furnace's upgrade slot, being dropped, being traded and world reloads, and
 * is automatically synchronised to the client for the tooltip. No custom registry entry or
 * networking is required, which keeps the shared sources loader independent.
 * <p>
 * Supported blocks: chests, trapped chests, barrels, hoppers and any other block entity that is a
 * {@link Container} (ender chests and this mod's own machines are excluded).
 */
public final class ContainerBinding {
	private static final String TAG_POS = "bound_pos";
	private static final String TAG_DIM = "bound_dim";
	private static final String TAG_BLOCK = "bound_block";

	/** Immutable description of a bound container. */
	public record Binding(BlockPos pos, String dimension, String block) {
	}

	private ContainerBinding() {
	}

	// ------------------------------------------------------------------ storage

	public static Optional<Binding> get(final ItemStack stack) {
		final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return Optional.empty();
		}

		final CompoundTag tag = data.copyTag();
		if (!tag.contains(TAG_POS) || !tag.contains(TAG_DIM)) {
			return Optional.empty();
		}

		return Optional.of(new Binding(
			BlockPos.of(tag.getLongOr(TAG_POS, 0L)),
			tag.getStringOr(TAG_DIM, ""),
			tag.getStringOr(TAG_BLOCK, "")
		));
	}

	public static boolean isBound(final ItemStack stack) {
		final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().contains(TAG_POS);
	}

	public static void bind(final ItemStack stack, final Level level, final BlockPos pos) {
		final String dimension = dimensionId(level);
		final String block = blockId(level, pos);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putLong(TAG_POS, pos.asLong());
			tag.putString(TAG_DIM, dimension);
			tag.putString(TAG_BLOCK, block);
		});
	}

	public static void unbind(final ItemStack stack) {
		if (!isBound(stack)) {
			return;
		}

		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.remove(TAG_POS);
			tag.remove(TAG_DIM);
			tag.remove(TAG_BLOCK);
		});
	}

	public static boolean isBindableItem(final @Nullable ItemStack stack) {
		return stack != null
			&& stack.getItem() instanceof UpgradeItem upgrade
			&& upgrade.getKind().isContainerBound();
	}

	// ------------------------------------------------------------------ blocks

	public static String dimensionId(final Level level) {
		return level.dimension().identifier().toString();
	}

	private static String blockId(final Level level, final BlockPos pos) {
		return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
	}

	/**
	 * A block can be bound when it owns a container block entity. Our own machines are handled by
	 * the upgrade-insertion path instead, and ender chests hold a per-player inventory.
	 */
	public static boolean isBindableBlock(final Level level, final BlockPos pos) {
		final BlockState state = level.getBlockState(pos);
		if (state.getBlock() == Blocks.ENDER_CHEST || state.isAir()) {
			return false;
		}

		final BlockEntity entity = level.getBlockEntity(pos);
		if (!(entity instanceof Container)) {
			return false;
		}

		return !(entity instanceof AbstractSmeltingBlockEntity)
			&& !(entity instanceof CobblestoneGeneratorBlockEntity)
			&& !(entity instanceof FuelVerifierBlockEntity);
	}

	/** Returns the bound container, or {@code null} when the binding is missing/invalid. */
	public static @Nullable Container containerAt(final Level level, final BlockPos pos) {
		final BlockState state = level.getBlockState(pos);
		// For double chests this returns the combined 54 slot container instead of a single half.
		if (state.getBlock() instanceof ChestBlock chest) {
			final Container combined = ChestBlock.getContainer(chest, state, level, pos, true);
			if (combined != null) {
				return combined;
			}
		}

		final BlockEntity entity = level.getBlockEntity(pos);
		return entity instanceof Container container ? container : null;
	}

	/**
	 * Clears a binding whose block was destroyed, replaced or moved. Called both while the item is
	 * held ({@code inventoryTick}) and while it is installed in a furnace (furnace tick).
	 */
	public static @Nullable Container resolve(final ServerLevel level, final ItemStack upgrade) {
		final Optional<Binding> bound = get(upgrade);
		if (bound.isEmpty()) {
			return null;
		}

		final Binding binding = bound.get();
		if (!binding.dimension().equals(dimensionId(level)) || !level.isLoaded(binding.pos())) {
			// Another dimension or an unloaded chunk: keep the binding intact and simply wait.
			return null;
		}

		if (!isBindableBlock(level, binding.pos()) || !binding.block().equals(blockId(level, binding.pos()))) {
			unbind(upgrade);
			return null;
		}

		return containerAt(level, binding.pos());
	}

	// ------------------------------------------------------------------ interaction

	/**
	 * Right click on a block. Returns {@code SUCCESS} (and therefore suppresses the chest GUI) when
	 * a bindable upgrade was just bound to a container.
	 * <p>
	 * This is called from the item's {@code useOn} and from each loader's right-click-block hook,
	 * because vanilla opens a chest's menu before the item's {@code useOn} gets a chance to run.
	 */
	public static InteractionResult onUseBlock(
		final @Nullable Player player, final Level level, final InteractionHand hand, final BlockPos pos, final ItemStack stack
	) {
		if (player == null || !isBindableItem(stack)) {
			return InteractionResult.PASS;
		}

		// Right clicking one of our furnaces/forges still installs the upgrade.
		if (level.getBlockEntity(pos) instanceof AbstractSmeltingBlockEntity) {
			return InteractionResult.PASS;
		}

		if (!isBindableBlock(level, pos)) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			bind(stack, level, pos);
			player.sendSystemMessage(Component.translatable(
				"message." + TMF.MOD_ID + ".bound", pos.getX(), pos.getY(), pos.getZ()));
		}

		return InteractionResult.SUCCESS;
	}

	/** Right click on air: unbind the held automation upgrade. */
	public static InteractionResult onUseAir(final Player player, final Level level, final InteractionHand hand) {
		final ItemStack stack = player.getItemInHand(hand);
		if (!isBindableItem(stack) || !isBound(stack)) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			unbind(stack);
			player.sendSystemMessage(Component.translatable("message." + TMF.MOD_ID + ".unbound"));
		}

		return InteractionResult.SUCCESS;
	}

	/** Server-side validation while the upgrade sits in a player's inventory. */
	public static void validateHeld(final ItemStack stack, final ServerLevel level, final Entity holder) {
		if (!isBindableItem(stack)) {
			return;
		}

		final Optional<Binding> bound = get(stack);
		if (bound.isEmpty() || !bound.get().dimension().equals(dimensionId(level))) {
			return;
		}

		final BlockPos pos = bound.get().pos();
		if (!level.isLoaded(pos) || (isBindableBlock(level, pos) && bound.get().block().equals(blockId(level, pos)))) {
			return;
		}

		unbind(stack);
		if (holder instanceof Player player) {
			player.sendSystemMessage(Component.translatable("message." + TMF.MOD_ID + ".unbound_broken"));
		}
	}
}
