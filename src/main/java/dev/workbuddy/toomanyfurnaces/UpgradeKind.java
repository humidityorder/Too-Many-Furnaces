package dev.workbuddy.toomanyfurnaces;

/**
 * Functional kind of an upgrade item. Placeholder kinds are registered and craftable but their
 * effect is explicitly documented as "not implemented yet" in their tooltip.
 */
public enum UpgradeKind {
	FUEL_EFFICIENCY(false),
	ADVANCED_FUEL_EFFICIENCY(false),
	ORE_PROCESSING(false),
	RAW_ORE_PROCESSING(false),
	ADVANCED_ORE_PROCESSING(false),
	ULTIMATE_ORE_PROCESSING(false),
	BLASTING(false),
	SMOKING(false),
	STORAGE(false),
	AUTO_INPUT(false),
	AUTO_OUTPUT(false),
	FACTORY(false),
	REDSTONE(false),
	LIQUID_FUEL(false),
	ENERGY(true),
	XP(true),
	GENERATOR(true),
	COLOR(true),
	PIPING(true);

	private final boolean placeholder;

	UpgradeKind(final boolean placeholder) {
		this.placeholder = placeholder;
	}

	/**
	 * Placeholder upgrades are kept for recipe compatibility only; their tooltip must state that
	 * the behaviour is planned for a later 26.2 release.
	 */
	public boolean isPlaceholder() {
		return this.placeholder;
	}
}
