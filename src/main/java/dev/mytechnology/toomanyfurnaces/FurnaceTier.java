package dev.mytechnology.toomanyfurnaces;

/**
 * Tier list shared by furnaces and forges. {@code cookTicks} is the number of ticks a single
 * smelting operation needs, regardless of the vanilla recipe time.
 */
public enum FurnaceTier {
	COPPER("copper", 175),
	IRON("iron", 150),
	STEEL("steel", 125),
	GOLD("gold", 100),
	AMETHYST("amethyst", 75),
	DIAMOND("diamond", 50),
	PLATINUM("platinum", 25),
	NETHERHOT("netherhot", 8),
	EXTREME("extreme", 4),
	ULTIMATE("ultimate", 1);

	private final String name;
	private final int cookTicks;

	FurnaceTier(final String name, final int cookTicks) {
		this.name = name;
		this.cookTicks = cookTicks;
	}

	public String getName() {
		return this.name;
	}

	public int getCookTicks() {
		return this.cookTicks;
	}

	public String furnaceId() {
		return this.name + "_furnace";
	}

	public String forgeId() {
		return this.name + "_forge";
	}

	public static FurnaceTier byName(final String name) {
		for (FurnaceTier tier : values()) {
			if (tier.name.equals(name)) {
				return tier;
			}
		}

		return null;
	}
}
