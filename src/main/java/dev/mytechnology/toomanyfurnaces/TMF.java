package dev.mytechnology.toomanyfurnaces;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Shared identifiers and registry key helpers for Too Many Furnaces.
 */
public final class TMF {
	public static final String MOD_ID = "toomanyfurnaces";

	private TMF() {
	}

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static <T> ResourceKey<T> key(final ResourceKey<Registry<T>> registry, final String path) {
		return ResourceKey.create(registry, id(path));
	}

	public static ResourceKey<Block> blockKey(final String path) {
		return key(Registries.BLOCK, path);
	}

	public static ResourceKey<Item> itemKey(final String path) {
		return key(Registries.ITEM, path);
	}

	public static ResourceKey<BlockEntityType<?>> blockEntityKey(final String path) {
		return ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(path));
	}

	public static ResourceKey<MenuType<?>> menuKey(final String path) {
		return ResourceKey.create(Registries.MENU, id(path));
	}

	public static ResourceKey<CreativeModeTab> tabKey(final String path) {
		return key(Registries.CREATIVE_MODE_TAB, path);
	}
}
