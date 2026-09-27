package dev.workbuddy.toomanyfurnaces.menu;

import dev.workbuddy.toomanyfurnaces.blockentity.AbstractSmeltingBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Output slot that hands the accumulated smelting experience to the player, mirroring the vanilla
 * furnace result slot without depending on {@code AbstractFurnaceBlockEntity}.
 */
public class SmeltingResultSlot extends Slot {
	private final Player player;
	private final @Nullable AbstractSmeltingBlockEntity smelter;
	private int removeCount;

	public SmeltingResultSlot(
		final Player player, final Container container, final int slot, final int x, final int y, final @Nullable AbstractSmeltingBlockEntity smelter
	) {
		super(container, slot, x, y);
		this.player = player;
		this.smelter = smelter;
	}

	@Override
	public boolean mayPlace(final ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack remove(final int amount) {
		if (this.hasItem()) {
			this.removeCount += Math.min(amount, this.getItem().getCount());
		}

		return super.remove(amount);
	}

	@Override
	public void onTake(final Player player, final ItemStack carried) {
		this.checkTakeAchievements(carried);
		super.onTake(player, carried);
	}

	@Override
	protected void onQuickCraft(final ItemStack picked, final int count) {
		this.removeCount += count;
		this.checkTakeAchievements(picked);
	}

	@Override
	protected void onSwapCraft(final int count) {
		this.removeCount += count;
	}

	protected void checkTakeAchievements(final ItemStack carried) {
		carried.onCraftedBy(this.player, this.removeCount);
		if (this.smelter != null) {
			this.smelter.awardStoredExperience(this.player);
		}

		this.removeCount = 0;
	}

	@Override
	public int getMaxStackSize(final ItemStack stack) {
		return this.smelter == null ? super.getMaxStackSize(stack) : Math.min(99, this.smelter.getSlotCapacity(stack));
	}
}
