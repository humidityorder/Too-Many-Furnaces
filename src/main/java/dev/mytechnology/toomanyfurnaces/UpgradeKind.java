package dev.mytechnology.toomanyfurnaces;

/**
 * Functional kind of an upgrade item.
 * <p>
 * The five placeholder upgrades (energy, generator, colour, piping and XP tank) were removed from
 * the mod; every remaining kind has a real, documented effect.
 */
public enum UpgradeKind {
	FUEL_EFFICIENCY,
	ADVANCED_FUEL_EFFICIENCY,
	ORE_PROCESSING,
	RAW_ORE_PROCESSING,
	ADVANCED_ORE_PROCESSING,
	ULTIMATE_ORE_PROCESSING,
	BLASTING,
	SMOKING,
	STORAGE,
	AUTO_INPUT,
	AUTO_OUTPUT,
	AUTO_IO,
	REDSTONE,
	LIQUID_FUEL;

	/** Upgrades whose behaviour depends on a chest/container binding stored on the item stack. */
	public boolean isContainerBound() {
		return this == AUTO_INPUT || this == AUTO_OUTPUT || this == AUTO_IO;
	}
}
