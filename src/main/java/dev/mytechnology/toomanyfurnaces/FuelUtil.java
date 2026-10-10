package dev.mytechnology.toomanyfurnaces;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import org.jspecify.annotations.Nullable;

/**
 * Fuel lookups for Minecraft 26.3.
 * <p>
 * 26.3 dropped {@code Level#fuelValues()} - burn times now live in the {@code minecraft:cooking_fuel}
 * data component of the item itself. Vanilla fuels store a plain constant there, which is cheap to
 * read, while datapack driven values are a reference that has to be resolved against a registry.
 */
public final class FuelUtil {
	private FuelUtil() {
	}

	/**
	 * @return the burn duration of {@code stack} in ticks, or {@code 0} when it is not a fuel.
	 */
	public static int burnDuration(final @Nullable Level level, final ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}

		final CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
		if (fuel == null) {
			return 0;
		}

		final ResolvableInt burnTime = fuel.burnTime();
		if (burnTime instanceof ResolvableInt.Constant constant) {
			return constant.value();
		}

		// A reference needs registry access to resolve, so it only works on the logical server.
		return level instanceof ServerLevel serverLevel ? burnTime.get(lootContext(serverLevel), 0) : 0;
	}

	public static boolean isFuel(final @Nullable Level level, final ItemStack stack) {
		return burnDuration(level, stack) > 0;
	}

	private static LootContext lootContext(final ServerLevel level) {
		return new LootContext.Builder(new LootParams.Builder(level).create(LootContextParamSets.EMPTY))
			.create(Optional.empty());
	}
}
