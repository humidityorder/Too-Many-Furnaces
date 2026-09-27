package dev.workbuddy.betterfurnaces.blockentity;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Server side smelting logic shared by every tiered furnace and forge.
 * <p>
 * Performance contract: all logic runs once per tick on the {@link ServerLevel} ticker only, recipe
 * lookups are cached, and neighbour interaction happens at most once every {@link #TRANSFER_INTERVAL}
 * ticks. Nothing here touches the client and no dynamic renderer is used.
 */
public abstract class AbstractSmeltingBlockEntity extends BlockEntity implements WorldlyContainer {
	public static final int UPGRADE_COUNT = 3;
	public static final int TRANSFER_INTERVAL = 8;
	/** Recipes are re-validated at most this often so a datapack reload never leaves a stale cache. */
	private static final int RECIPE_CACHE_TTL = 100;
	private static final int[] EMPTY_SLOTS = new int[0];

	protected NonNullList<ItemStack> items;
	protected final NonNullList<ItemStack> upgrades = NonNullList.withSize(UPGRADE_COUNT, ItemStack.EMPTY);
	protected int litTime;
	protected int litTotalTime;
	protected int[] cookingProgress;
	protected float storedXp;
	protected int redstoneMode;
	protected int tickCount;

	private final Map<RecipeType<?>, RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe>> quickChecks =
		new Object2ObjectOpenHashMap<>(4);
	private final Map<Item, Optional<RecipeHolder<? extends AbstractCookingRecipe>>> recipeCache = new Object2ObjectOpenHashMap<>(16);
	private RecipeType<?> cachedRecipeType;
	private int recipeCacheAge;

	protected AbstractSmeltingBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state, final int itemCount) {
		super(type, pos, state);
		this.items = NonNullList.withSize(itemCount, ItemStack.EMPTY);
		this.cookingProgress = new int[this.getInputSlots().length];
	}

	// ------------------------------------------------------------------ layout

	public abstract int[] getInputSlots();

	public abstract int getFuelSlot();

	public abstract int[] getOutputSlots();

	public abstract int getCookTicks();

	public abstract net.minecraft.world.inventory.ContainerData getDataAccess();

	// ------------------------------------------------------------------ ticking

	public void serverTick(final ServerLevel level, final BlockPos pos, final BlockState state) {
		this.tickCount++;
		this.recipeCacheAge++;
		if (this.recipeCacheAge >= RECIPE_CACHE_TTL) {
			this.recipeCache.clear();
			this.recipeCacheAge = 0;
		}
		if (this.tickCount % TRANSFER_INTERVAL == 0) {
			this.transferWithNeighbours(level, pos);
		}

		final boolean wasLit = this.litTime > 0;
		if (this.litTime > 0) {
			this.litTime--;
		}

		boolean isLit = this.litTime > 0;
		boolean changed = false;

		if (!this.isDisabledByRedstone(level, pos)) {
			final int cookTicks = Math.max(1, this.getCookTicks());
			final int[] inputs = this.getInputSlots();
			final int[] outputs = this.getOutputSlots();
			for (int i = 0; i < inputs.length; i++) {
				final int inputSlot = inputs[i];
				final int outputSlot = outputs[i];
				final ItemStack input = this.items.get(inputSlot);
				if (input.isEmpty()) {
					this.cookingProgress[i] = 0;
					continue;
				}

				final Optional<RecipeHolder<? extends AbstractCookingRecipe>> recipe = this.findRecipe(level, input);
				if (recipe.isEmpty()) {
					this.cookingProgress[i] = 0;
					continue;
				}

				final ItemStack result = this.assemble(recipe.get(), input);
				if (result.isEmpty() || !this.canAcceptOutput(outputSlot, result)) {
					this.cookingProgress[i] = 0;
					continue;
				}

				if (!isLit && !this.items.get(this.getFuelSlot()).isEmpty()) {
					this.litTime = this.consumeFuel(level);
					this.litTotalTime = this.litTime;
					isLit = this.litTime > 0;
					if (isLit) {
						changed = true;
					}
				}

				if (isLit) {
					this.cookingProgress[i]++;
					if (this.cookingProgress[i] >= cookTicks) {
						this.cookingProgress[i] = 0;
						this.produce(inputSlot, outputSlot, result, recipe.get().value().experience());
						changed = true;
					}
				} else {
					this.cookingProgress[i] = 0;
				}
			}
		}

		if (wasLit != isLit) {
			changed = true;
			final BlockState litState = state.hasProperty(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT)
				? state.setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, isLit)
				: state;
			if (litState != state) {
				level.setBlock(pos, litState, 3);
			}
		}

		if (changed) {
			this.setChanged(level, pos, state);
		}
	}

	private boolean isDisabledByRedstone(final Level level, final BlockPos pos) {
		if (!this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.REDSTONE) || this.redstoneMode == 0) {
			return false;
		}

		final boolean powered = level.hasNeighborSignal(pos);
		return this.redstoneMode == 1 ? powered : !powered;
	}

	// ------------------------------------------------------------------ recipes

	private Optional<RecipeHolder<? extends AbstractCookingRecipe>> findRecipe(final ServerLevel level, final ItemStack stack) {
		final RecipeType<?> type = this.recipeType();
		if (type != this.cachedRecipeType) {
			this.recipeCache.clear();
			this.cachedRecipeType = type;
		}

		final Item item = stack.getItem();
		Optional<RecipeHolder<? extends AbstractCookingRecipe>> cached = this.recipeCache.get(item);
		if (cached == null) {
			cached = this.queryRecipe(level, type, stack);
			this.recipeCache.put(item, cached);
		}

		return cached;
	}

	@SuppressWarnings("unchecked")
	private Optional<RecipeHolder<? extends AbstractCookingRecipe>> queryRecipe(
		final ServerLevel level, final RecipeType<?> type, final ItemStack stack
	) {
		final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> check =
			this.quickChecks.computeIfAbsent(type, t -> RecipeManager.createCheck((RecipeType<AbstractCookingRecipe>)t));
		return (Optional<RecipeHolder<? extends AbstractCookingRecipe>>)(Optional<?>)check.getRecipeFor(new SingleRecipeInput(stack), level);
	}

	public boolean hasRecipe(final ServerLevel level, final ItemStack stack) {
		return !stack.isEmpty() && this.findRecipe(level, stack).isPresent();
	}

	private ItemStack assemble(final RecipeHolder<? extends AbstractCookingRecipe> recipe, final ItemStack input) {
		final ItemStack result = recipe.value().assemble(new SingleRecipeInput(input));
		if (result.isEmpty()) {
			return ItemStack.EMPTY;
		}

		final int multiplier = this.oreMultiplier(input);
		if (multiplier > 1) {
			result.setCount(Math.min(99, result.getCount() * multiplier));
		}

		return result;
	}

	private RecipeType<?> recipeType() {
		if (this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.BLASTING)) {
			return RecipeType.BLASTING;
		}

		if (this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.SMOKING)) {
			return RecipeType.SMOKING;
		}

		return RecipeType.SMELTING;
	}

	// ------------------------------------------------------------------ fuel

	private int consumeFuel(final ServerLevel level) {
		final int fuelSlot = this.getFuelSlot();
		final ItemStack fuel = this.items.get(fuelSlot);
		if (fuel.isEmpty()) {
			return 0;
		}

		final int base = level.fuelValues().burnDuration(fuel);
		if (base <= 0) {
			return 0;
		}

		final int duration = base * this.fuelEfficiencyMultiplier();
		final Item fuelItem = fuel.getItem();
		fuel.shrink(1);
		if (fuel.isEmpty()) {
			// Liquid fuel: always hand the empty bucket back (see upstream issues #191 / #210).
			final ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
			this.items.set(fuelSlot, remainder != null ? remainder.create() : ItemStack.EMPTY);
		}

		this.setChanged();
		return duration;
	}

	private int fuelEfficiencyMultiplier() {
		int multiplier = 1;
		if (this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.FUEL_EFFICIENCY)) {
			multiplier *= 2;
		}

		if (this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.ADVANCED_FUEL_EFFICIENCY)) {
			multiplier *= 2;
		}

		return multiplier;
	}

	private int oreMultiplier(final ItemStack input) {
		final Identifier path = BuiltInRegistries.ITEM.getKey(input.getItem());
		final String name = path == null ? "" : path.getPath();
		final boolean raw = name.startsWith("raw_");
		final boolean ore = name.endsWith("_ore") || name.endsWith("ore") || name.contains("_ore_");
		if (!raw && !ore) {
			return 1;
		}

		int multiplier = 1;
		if (ore && this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.ORE_PROCESSING)) {
			multiplier = Math.max(multiplier, 2);
		}

		if (raw && this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.RAW_ORE_PROCESSING)) {
			multiplier = Math.max(multiplier, 2);
		}

		if (ore && this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.ADVANCED_ORE_PROCESSING)) {
			multiplier = Math.max(multiplier, 4);
		}

		if (this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.ULTIMATE_ORE_PROCESSING)) {
			multiplier = Math.max(multiplier, 4);
		}

		return multiplier;
	}

	// ------------------------------------------------------------------ output

	private boolean canAcceptOutput(final int outputSlot, final ItemStack result) {
		final ItemStack current = this.items.get(outputSlot);
		if (current.isEmpty()) {
			return true;
		}

		if (!ItemStack.isSameItemSameComponents(current, result)) {
			return false;
		}

		return current.getCount() + result.getCount() <= this.getSlotCapacity(result);
	}

	private void produce(final int inputSlot, final int outputSlot, final ItemStack result, final float experience) {
		final ItemStack current = this.items.get(outputSlot);
		if (current.isEmpty()) {
			this.items.set(outputSlot, result.copy());
		} else {
			current.grow(Math.min(result.getCount(), this.getSlotCapacity(result) - current.getCount()));
		}

		this.storedXp += experience;
		this.items.get(inputSlot).shrink(1);
		this.setChanged();
	}

	public void awardStoredExperience(final Player player) {
		if (!(player instanceof ServerPlayer serverPlayer) || this.storedXp <= 0.0F) {
			return;
		}

		int xp = Mth.floor(this.storedXp);
		final float fraction = Mth.frac(this.storedXp);
		this.storedXp -= xp;
		if (fraction != 0.0F && serverPlayer.level().getRandom().nextFloat() < fraction) {
			xp++;
			this.storedXp = 0.0F;
		}

		if (xp > 0) {
			serverPlayer.giveExperiencePoints(xp);
		}
	}

	// ------------------------------------------------------------------ upgrades

	public Container getUpgradeContainer() {
		return new UpgradeContainer();
	}

	public boolean hasUpgrade(final dev.workbuddy.betterfurnaces.UpgradeKind kind) {
		for (ItemStack stack : this.upgrades) {
			if (stack.getItem() instanceof dev.workbuddy.betterfurnaces.item.UpgradeItem upgrade && upgrade.getKind() == kind) {
				return true;
			}
		}

		return false;
	}

	public int getSlotCapacity(final ItemStack stack) {
		final int max = stack.getMaxStackSize();
		if (max <= 1) {
			return 1;
		}

		return this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.STORAGE) ? Math.min(99, max * 2) : max;
	}

	public int getRedstoneMode() {
		return this.redstoneMode;
	}

	public void setRedstoneMode(final int mode) {
		this.redstoneMode = Mth.clamp(mode, 0, 2);
		this.setChanged();
	}

	// ------------------------------------------------------------------ automation

	private void transferWithNeighbours(final ServerLevel level, final BlockPos pos) {
		final boolean input = this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.AUTO_INPUT)
			|| this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.FACTORY);
		final boolean output = this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.AUTO_OUTPUT)
			|| this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.FACTORY);
		if (!input && !output) {
			return;
		}

		for (Direction direction : Direction.values()) {
			final BlockPos neighbourPos = pos.relative(direction);
			final BlockEntity neighbour = level.getBlockEntity(neighbourPos);
			if (!(neighbour instanceof Container container) || container == this) {
				continue;
			}

			if (output) {
				this.pushOutputs(container, direction);
			}

			if (input) {
				this.pullInputs(level, container, direction);
			}
		}
	}

	private static int[] accessibleSlots(final Container container, final Direction direction) {
		if (container instanceof WorldlyContainer worldly) {
			return worldly.getSlotsForFace(direction.getOpposite());
		}

		final int size = container.getContainerSize();
		final int[] slots = new int[size];
		for (int i = 0; i < size; i++) {
			slots[i] = i;
		}

		return slots;
	}

	private void pushOutputs(final Container target, final Direction direction) {
		final int[] targetSlots = accessibleSlots(target, direction);
		for (int outputSlot : this.getOutputSlots()) {
			final ItemStack stack = this.items.get(outputSlot);
			if (stack.isEmpty()) {
				continue;
			}

			for (int targetSlot : targetSlots) {
				if (targetSlot < 0 || targetSlot >= target.getContainerSize()) {
					continue;
				}

				if (target instanceof WorldlyContainer worldly
					&& !worldly.canPlaceItemThroughFace(targetSlot, stack, direction.getOpposite())) {
					continue;
				}

				final ItemStack existing = target.getItem(targetSlot);
				if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
					continue;
				}

				final int limit = Math.min(target.getMaxStackSize(stack), Math.min(this.getSlotCapacity(stack), 99));
				final int room = existing.isEmpty() ? limit : limit - existing.getCount();
				if (room <= 0) {
					continue;
				}

				final int moved = Math.min(room, stack.getCount());
				if (existing.isEmpty()) {
					final ItemStack copy = stack.copyWithCount(moved);
					target.setItem(targetSlot, copy);
				} else {
					existing.grow(moved);
				}

				stack.shrink(moved);
				target.setChanged();
				this.setChanged();
				if (stack.isEmpty()) {
					break;
				}
			}
		}
	}

	private void pullInputs(final ServerLevel level, final Container source, final Direction direction) {
		final int[] sourceSlots = accessibleSlots(source, direction);
		for (int sourceSlot : sourceSlots) {
			if (sourceSlot < 0 || sourceSlot >= source.getContainerSize()) {
				continue;
			}

			final ItemStack stack = source.getItem(sourceSlot);
			if (stack.isEmpty()) {
				continue;
			}

			if (source instanceof WorldlyContainer worldly
				&& !worldly.canTakeItemThroughFace(sourceSlot, stack, direction.getOpposite())) {
				continue;
			}

			final boolean fuel = level.fuelValues().isFuel(stack);
			if (fuel) {
				this.pullInto(this.getFuelSlot(), source, sourceSlot, stack);
			} else if (this.hasRecipe(level, stack)) {
				for (int inputSlot : this.getInputSlots()) {
					if (this.pullInto(inputSlot, source, sourceSlot, stack)) {
						break;
					}
				}
			}
		}
	}

	private boolean pullInto(final int slot, final Container source, final int sourceSlot, final ItemStack stack) {
		if (!this.canPlaceItem(slot, stack)) {
			return false;
		}

		final ItemStack current = this.items.get(slot);
		if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
			return false;
		}

		final int limit = Math.min(this.getSlotCapacity(stack), 99);
		final int room = current.isEmpty() ? limit : limit - current.getCount();
		if (room <= 0) {
			return false;
		}

		final int moved = Math.min(room, stack.getCount());
		if (current.isEmpty()) {
			this.items.set(slot, stack.copyWithCount(moved));
		} else {
			current.grow(moved);
		}

		stack.shrink(moved);
		if (stack.isEmpty()) {
			source.setItem(sourceSlot, ItemStack.EMPTY);
		}

		source.setChanged();
		this.setChanged();
		return true;
	}

	// ------------------------------------------------------------------ container

	@Override
	public int getContainerSize() {
		return this.items.size();
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
		return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
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
		if (slot < 0 || slot >= this.items.size()) {
			return;
		}

		final boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(this.items.get(slot), stack);
		this.items.set(slot, stack);
		stack.limitSize(this.getSlotCapacity(stack));
		if (!same) {
			final int[] inputs = this.getInputSlots();
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] == slot) {
					this.cookingProgress[i] = 0;
				}
			}

			this.setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(final int slot, final ItemStack stack) {
		if (stack.isEmpty() || slot < 0 || slot >= this.items.size()) {
			return false;
		}

		for (int outputSlot : this.getOutputSlots()) {
			if (outputSlot == slot) {
				return false;
			}
		}

		if (slot == this.getFuelSlot()) {
			return this.isFuel(stack);
		}

		return true;
	}

	private boolean isFuel(final ItemStack stack) {
		if (this.level == null) {
			return false;
		}

		if (!this.level.fuelValues().isFuel(stack)) {
			return false;
		}

		if (stack.is(Items.LAVA_BUCKET) && !this.hasUpgrade(dev.workbuddy.betterfurnaces.UpgradeKind.LIQUID_FUEL)) {
			// Buckets are only accepted once the liquid fuel upgrade is installed, so the empty
			// bucket can never clog the fuel slot (upstream issue #191).
			return false;
		}

		if (stack.is(Items.LAVA_BUCKET) && !this.items.get(this.getFuelSlot()).isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.upgrades.clear();
	}

	@Override
	public boolean stillValid(final Player player) {
		return this.level != null
			&& this.level.getBlockEntity(this.worldPosition) == this
			&& player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= 64.0;
	}

	@Override
	public int[] getSlotsForFace(final Direction direction) {
		if (direction == Direction.DOWN) {
			final int[] outputs = this.getOutputSlots();
			final int[] slots = new int[outputs.length + 1];
			System.arraycopy(outputs, 0, slots, 0, outputs.length);
			slots[outputs.length] = this.getFuelSlot();
			return slots;
		}

		if (direction == Direction.UP) {
			return this.getInputSlots();
		}

		return new int[]{this.getFuelSlot()};
	}

	@Override
	public boolean canPlaceItemThroughFace(final int slot, final ItemStack stack, final @Nullable Direction direction) {
		return this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(final int slot, final ItemStack stack, final Direction direction) {
		for (int outputSlot : this.getOutputSlots()) {
			if (outputSlot == slot) {
				return true;
			}
		}

		return slot == this.getFuelSlot() && (stack.is(Items.BUCKET) || stack.is(Items.WATER_BUCKET));
	}

	// ------------------------------------------------------------------ persistence

	@Override
	protected void loadAdditional(final ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		ContainerHelper.loadAllItems(input.childOrEmpty("Upgrades"), this.upgrades);
		this.litTime = input.getIntOr("lit_time", 0);
		this.litTotalTime = input.getIntOr("lit_total_time", 0);
		this.storedXp = input.getFloatOr("stored_xp", 0.0F);
		this.redstoneMode = Mth.clamp(input.getIntOr("redstone_mode", 0), 0, 2);
		final int[] progress = input.getIntArray("cooking_progress").orElse(EMPTY_SLOTS);
		for (int i = 0; i < this.cookingProgress.length; i++) {
			this.cookingProgress[i] = i < progress.length ? progress[i] : 0;
		}
	}

	@Override
	protected void saveAdditional(final ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		ContainerHelper.saveAllItems(output.child("Upgrades"), this.upgrades);
		output.putInt("lit_time", this.litTime);
		output.putInt("lit_total_time", this.litTotalTime);
		output.putFloat("stored_xp", this.storedXp);
		output.putInt("redstone_mode", this.redstoneMode);
		output.putIntArray("cooking_progress", this.cookingProgress);
	}

	@Override
	public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (this.level != null) {
			Containers.dropContents(this.level, pos, this.upgrades);
		}
	}

	/** Hidden upgrade inventory exposed to the menu but never to hoppers. */
	private final class UpgradeContainer implements Container {
		@Override
		public int getContainerSize() {
			return UPGRADE_COUNT;
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack stack : AbstractSmeltingBlockEntity.this.upgrades) {
				if (!stack.isEmpty()) {
					return false;
				}
			}

			return true;
		}

		@Override
		public ItemStack getItem(final int slot) {
			return AbstractSmeltingBlockEntity.this.upgrades.get(slot);
		}

		@Override
		public ItemStack removeItem(final int slot, final int amount) {
			final ItemStack result = ContainerHelper.removeItem(AbstractSmeltingBlockEntity.this.upgrades, slot, amount);
			if (!result.isEmpty()) {
				AbstractSmeltingBlockEntity.this.invalidateUpgradeCache();
			}

			return result;
		}

		@Override
		public ItemStack removeItemNoUpdate(final int slot) {
			return ContainerHelper.takeItem(AbstractSmeltingBlockEntity.this.upgrades, slot);
		}

		@Override
		public void setItem(final int slot, final ItemStack stack) {
			AbstractSmeltingBlockEntity.this.upgrades.set(slot, stack);
			AbstractSmeltingBlockEntity.this.invalidateUpgradeCache();
		}

		@Override
		public void setChanged() {
			AbstractSmeltingBlockEntity.this.invalidateUpgradeCache();
			AbstractSmeltingBlockEntity.this.setChanged();
		}

		@Override
		public boolean stillValid(final Player player) {
			return AbstractSmeltingBlockEntity.this.stillValid(player);
		}

		@Override
		public void clearContent() {
			AbstractSmeltingBlockEntity.this.upgrades.clear();
			AbstractSmeltingBlockEntity.this.invalidateUpgradeCache();
		}

		@Override
		public boolean canPlaceItem(final int slot, final ItemStack stack) {
			return stack.getItem() instanceof dev.workbuddy.betterfurnaces.item.UpgradeItem;
		}
	}

	protected void invalidateUpgradeCache() {
		// nothing cached beyond the container contents today; kept as a single hook for subclasses
	}
}
