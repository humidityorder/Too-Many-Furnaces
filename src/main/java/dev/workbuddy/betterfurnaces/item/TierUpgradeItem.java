package dev.workbuddy.betterfurnaces.item;

import dev.workbuddy.betterfurnaces.BFR;
import dev.workbuddy.betterfurnaces.FurnaceTier;
import dev.workbuddy.betterfurnaces.ModRegistry;
import dev.workbuddy.betterfurnaces.block.TieredForgeBlock;
import dev.workbuddy.betterfurnaces.block.TieredFurnaceBlock;
import dev.workbuddy.betterfurnaces.blockentity.AbstractSmeltingBlockEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

/**
 * Upgrades a furnace or forge in place. The block entity (inventory, upgrades and NBT) is kept
 * because every tier of a kind shares one block entity type and the blocks report
 * {@code shouldChangedStateKeepBlockEntity}.
 */
public class TierUpgradeItem extends Item {
	private final @Nullable FurnaceTier from;
	private final FurnaceTier to;

	public TierUpgradeItem(final Item.Properties properties, final @Nullable FurnaceTier from, final FurnaceTier to) {
		super(properties);
		this.from = from;
		this.to = to;
	}

	public @Nullable FurnaceTier getFrom() {
		return this.from;
	}

	public FurnaceTier getTo() {
		return this.to;
	}

	@Override
	public InteractionResult useOn(final UseOnContext context) {
		return this.applyTo(context.getLevel(), context.getClickedPos(), context.getItemInHand(), context.getPlayer());
	}

	/** Shared with the block interaction hook so the menu does not swallow the upgrade. */
	public InteractionResult applyTo(final Level level, final BlockPos pos, final ItemStack stack, final @Nullable Player player) {
		final BlockState state = level.getBlockState(pos);
		final Block current = state.getBlock();
		final Block target = this.resolveTarget(current);
		if (target == null || target == current) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		// A vanilla furnace loses its block entity on the swap, so its input/fuel/output are carried
		// over by hand. Block entity side effects are skipped, otherwise the contents would be dropped
		// and then exist twice. Our own furnaces and forges keep their block entity and need no copy.
		final ItemStack[] carried = captureMachineContents(level.getBlockEntity(pos));
		level.setBlock(
			pos, copyProperties(state, target.defaultBlockState()),
			Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS
		);
		if (carried != null && level.getBlockEntity(pos) instanceof Container machine) {
			for (int slot = 0; slot < carried.length && slot < machine.getContainerSize(); slot++) {
				if (!carried[slot].isEmpty()) {
					machine.setItem(slot, carried[slot]);
				}
			}
		}

		if (player != null && !player.isCreative()) {
			stack.shrink(1);
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * Copies the first three slots of a foreign machine block entity (a vanilla furnace) so nothing is
	 * lost when the block is replaced. Returns {@code null} when the block entity is kept as is.
	 */
	private static @Nullable ItemStack[] captureMachineContents(final @Nullable BlockEntity entity) {
		if (!(entity instanceof Container container) || entity instanceof AbstractSmeltingBlockEntity) {
			return null;
		}

		final int size = Math.min(3, container.getContainerSize());
		final ItemStack[] carried = new ItemStack[size];
		for (int slot = 0; slot < size; slot++) {
			carried[slot] = container.getItem(slot).copy();
		}

		return carried;
	}

	private @Nullable Block resolveTarget(final Block current) {
		if (this.from == null) {
			return current == Blocks.FURNACE ? ModRegistry.FURNACES.get(this.to) : null;
		}

		if (current instanceof TieredFurnaceBlock furnace) {
			return furnace.getTier() == this.from ? ModRegistry.FURNACES.get(this.to) : null;
		}

		if (current instanceof TieredForgeBlock forge) {
			return forge.getTier() == this.from ? ModRegistry.FORGES.get(this.to) : null;
		}

		return null;
	}

	private static <T extends Comparable<T>> BlockState copyValue(final BlockState from, final BlockState to, final Property<T> property) {
		return to.setValue(property, from.getValue(property));
	}

	private static BlockState copyProperties(final BlockState from, BlockState to) {
		for (Property<?> property : from.getProperties()) {
			to = copyValue(from, to, property);
		}

		return to;
	}

	@Override
	public void appendHoverText(
		final ItemStack stack, final Item.TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder, final TooltipFlag flag
	) {
		final String fromName = this.from == null
			? Blocks.FURNACE.getName().getString()
			: Component.translatable("block." + BFR.MOD_ID + "." + this.from.furnaceId()).getString();
		final String toName = Component.translatable("block." + BFR.MOD_ID + "." + this.to.furnaceId()).getString();
		builder.accept(Component.translatable("tooltip." + BFR.MOD_ID + ".upgrade.tier", fromName, toName).withStyle(ChatFormatting.GRAY));
		builder.accept(Component.translatable("tooltip." + BFR.MOD_ID + ".upgrade.tier.keep").withStyle(ChatFormatting.DARK_GRAY));
	}
}
