package dev.mytechnology.toomanyfurnaces.item;

import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.UpgradeKind;
import dev.mytechnology.toomanyfurnaces.blockentity.AbstractSmeltingBlockEntity;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Functional upgrade. Right clicking a furnace/forge inserts the upgrade into a free hidden upgrade
 * slot; the redstone upgrade instead cycles the redstone control mode.
 * <p>
 * The three automation upgrades additionally bind to a container: right click a chest, trapped
 * chest, barrel or hopper to bind, right click air to unbind, and right click one of our furnaces
 * to install the (still bound) upgrade into the machine.
 */
public class UpgradeItem extends Item {
	private final UpgradeKind kind;

	public UpgradeItem(final Item.Properties properties, final UpgradeKind kind) {
		super(properties);
		this.kind = kind;
	}

	public UpgradeKind getKind() {
		return this.kind;
	}

	@Override
	public InteractionResult useOn(final UseOnContext context) {
		final Level level = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final ItemStack stack = context.getItemInHand();
		final Player player = context.getPlayer();
		if (this.kind.isContainerBound()) {
			final InteractionResult bound = ContainerBinding.onUseBlock(player, level, context.getHand(), pos, stack);
			if (bound != InteractionResult.PASS) {
				return bound;
			}
		}

		return this.applyTo(level, pos, stack, player);
	}

	/** Right clicking air unbinds an automation upgrade. */
	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		final InteractionResult result = ContainerBinding.onUseAir(player, level, hand);
		return result == InteractionResult.PASS ? super.use(level, player, hand) : result;
	}

	/** Detects a destroyed/replaced bound container while the upgrade is being held. */
	@Override
	public void inventoryTick(final ItemStack stack, final ServerLevel level, final Entity entity, final EquipmentSlot slot) {
		super.inventoryTick(stack, level, entity, slot);
		ContainerBinding.validateHeld(stack, level, entity);
	}

	/**
	 * Shared entry point for both the item interaction and the block interaction: the vanilla furnace
	 * block handles {@code useWithoutItem} first, so the block forwards held upgrades here instead of
	 * opening the menu.
	 */
	public InteractionResult applyTo(final Level level, final BlockPos pos, final ItemStack stack, final @Nullable Player player) {
		if (!(level.getBlockEntity(pos) instanceof AbstractSmeltingBlockEntity smelter)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (this.kind == UpgradeKind.REDSTONE) {
			// The first use installs the upgrade and enables control; later uses only cycle the mode,
			// so the item is never consumed twice.
			if (!smelter.hasUpgrade(UpgradeKind.REDSTONE)) {
				if (!this.insert(smelter, stack, player)) {
					return InteractionResult.PASS;
				}

				smelter.setRedstoneMode(1);
				this.notifyMode(player, 1);
				return InteractionResult.SUCCESS;
			}

			final int mode = (smelter.getRedstoneMode() + 1) % 3;
			smelter.setRedstoneMode(mode);
			this.notifyMode(player, mode);
			return InteractionResult.SUCCESS;
		}

		return this.insert(smelter, stack, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	private boolean insert(final AbstractSmeltingBlockEntity smelter, final ItemStack stack, final @Nullable Player player) {
		final Container upgrades = smelter.getUpgradeContainer();
		for (int slot = 0; slot < upgrades.getContainerSize(); slot++) {
			if (upgrades.getItem(slot).isEmpty()) {
				upgrades.setItem(slot, stack.copyWithCount(1));
				if (player != null && !player.isCreative()) {
					stack.shrink(1);
				}

				smelter.setChanged();
				return true;
			}
		}

		return false;
	}

	private void notifyMode(final @Nullable Player player, final int mode) {
		if (player != null) {
			player.sendSystemMessage(Component.translatable("tooltip." + TMF.MOD_ID + ".redstone_mode." + mode));
		}
	}

	/** Applies the held upgrade, if any, and reports whether the interaction was consumed. */
	public static InteractionResult applyHeld(final ItemStack stack, final Level level, final BlockPos pos, final @Nullable Player player) {
		if (stack.getItem() instanceof UpgradeItem upgrade) {
			return upgrade.applyTo(level, pos, stack, player);
		}

		if (stack.getItem() instanceof TierUpgradeItem tier) {
			return tier.applyTo(level, pos, stack, player);
		}

		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(
		final ItemStack stack, final Item.TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder, final TooltipFlag flag
	) {
		builder.accept(Component.translatable(this.descriptionKey()).withStyle(ChatFormatting.GRAY));
		if (this.kind.isContainerBound()) {
			final Optional<ContainerBinding.Binding> bound = ContainerBinding.get(stack);
			if (bound.isPresent()) {
				final BlockPos pos = bound.get().pos();
				builder.accept(Component.translatable("tooltip." + TMF.MOD_ID + ".upgrade.bound",
					pos.getX(), pos.getY(), pos.getZ(), bound.get().dimension()).withStyle(ChatFormatting.GREEN));
			} else {
				builder.accept(Component.translatable("tooltip." + TMF.MOD_ID + ".upgrade.unbound")
					.withStyle(ChatFormatting.DARK_GRAY));
			}

			builder.accept(Component.translatable("tooltip." + TMF.MOD_ID + ".upgrade.bind_hint")
				.withStyle(ChatFormatting.DARK_GRAY));
		}

		builder.accept(Component.translatable("tooltip." + TMF.MOD_ID + ".upgrade_right_click").withStyle(ChatFormatting.DARK_GRAY));
	}

	private String descriptionKey() {
		return "tooltip." + TMF.MOD_ID + ".upgrade." + switch (this.kind) {
			case FUEL_EFFICIENCY -> "fuel";
			case ADVANCED_FUEL_EFFICIENCY -> "advanced_fuel";
			case ORE_PROCESSING -> "ores";
			case RAW_ORE_PROCESSING -> "raw_ores";
			case ADVANCED_ORE_PROCESSING -> "advanced_ores";
			case ULTIMATE_ORE_PROCESSING -> "ultimate_ores";
			case BLASTING -> "blasting";
			case SMOKING -> "smoking";
			case STORAGE -> "storage";
			case AUTO_INPUT -> "input";
			case AUTO_OUTPUT -> "output";
			case AUTO_IO -> "auto_io";
			case REDSTONE -> "redstone";
			case LIQUID_FUEL -> "liquid";
		};
	}
}
