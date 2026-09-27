package dev.mytechnology.toomanyfurnaces;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric entry point. Registers straight into the built in registries; Forge and NeoForge use their
 * own bootstrap classes instead, so this file is excluded from those builds.
 */
public class TooManyFurnaces implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(TMF.MOD_ID);

	@Override
	public void onInitialize() {
		ModRegistry.registerBlocks(
			(key, block) -> Registry.register(BuiltInRegistries.BLOCK, key, block),
			(key, item) -> Registry.register(BuiltInRegistries.ITEM, key, item)
		);
		ModRegistry.registerItems((key, item) -> Registry.register(BuiltInRegistries.ITEM, key, item));
		ModRegistry.registerBlockEntities((key, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type));
		ModRegistry.registerMenus((key, menu) -> Registry.register(BuiltInRegistries.MENU, key, menu));
		ModRegistry.registerCreativeTab((key, tab) -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab));

		LOGGER.info("Too Many Furnaces: registered {} furnaces, {} forges and {} upgrades",
			ModRegistry.FURNACES.size(), ModRegistry.FORGES.size(), ModRegistry.UPGRADES.size() + ModRegistry.TIER_UPGRADES.size());
	}
}
