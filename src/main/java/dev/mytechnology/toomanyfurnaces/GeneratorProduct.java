package dev.mytechnology.toomanyfurnaces;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Blocks the cobblestone generator can be set to produce. The generation times follow the original
 * Better Furnaces Reforged values: cobblestone is the quickest, obsidian takes a good while.
 */
public enum GeneratorProduct {
	COBBLESTONE(80, Items.COBBLESTONE),
	STONE(100, Items.STONE),
	BLACKSTONE(150, Items.BLACKSTONE),
	COBBLED_DEEPSLATE(160, Items.COBBLED_DEEPSLATE),
	DEEPSLATE(200, Items.DEEPSLATE),
	OBSIDIAN(600, Items.OBSIDIAN);

	private final int ticks;
	private final Item item;

	GeneratorProduct(final int ticks, final Item item) {
		this.ticks = ticks;
		this.item = item;
	}

	/** Ticks needed to produce one item. */
	public int ticks() {
		return this.ticks;
	}

	public Item item() {
		return this.item;
	}

	public ItemStack stack() {
		return new ItemStack(this.item);
	}

	public Component displayName() {
		return this.stack().getHoverName();
	}

	/** Ordinal lookup that survives a saved out-of-range ordinal by falling back to the first entry. */
	public static GeneratorProduct byOrdinal(final int ordinal) {
		final GeneratorProduct[] values = values();
		return values[Math.floorMod(ordinal, values.length)];
	}

	/** Name lookup for saved data; unknown names fall back to the first entry. */
	public static GeneratorProduct byName(final String name) {
		for (final GeneratorProduct product : values()) {
			if (product.name().equals(name)) {
				return product;
			}
		}

		return COBBLESTONE;
	}
}
