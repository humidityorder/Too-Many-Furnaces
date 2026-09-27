package dev.mytechnology.toomanyfurnaces;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManyFurnaces implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(TMF.MOD_ID);

	@Override
	public void onInitialize() {
		ModRegistry.registerBlocks();
		ModRegistry.registerBlockEntities();
		ModRegistry.registerItems();
		ModRegistry.registerMenus();
		ModRegistry.registerCreativeTab();
		LOGGER.info("Too Many Furnaces: registered {} furnaces, {} forges and {} upgrades",
			ModRegistry.FURNACES.size(), ModRegistry.FORGES.size(), ModRegistry.UPGRADES.size() + ModRegistry.TIER_UPGRADES.size());
	}
}
