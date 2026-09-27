package dev.mytechnology.toomanyfurnaces.neoforge;

import dev.mytechnology.toomanyfurnaces.ModRegistry;
import dev.mytechnology.toomanyfurnaces.TMF;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge entry point.
 * <p>
 * {@link RegisterEvent} is fired once per registry, so every phase is handed to {@link ModRegistry}
 * through the matching helper. {@link RegisterEvent#register(ResourceKey, java.util.function.Consumer)}
 * silently skips consumers whose registry key does not match, which keeps the five phases in their
 * required order (blocks, items, block entity types, menus, creative tab) without manual dispatch.
 */
@Mod(TMF.MOD_ID)
public final class TooManyFurnacesNeoForge {
	public static final Logger LOGGER = LoggerFactory.getLogger(TMF.MOD_ID);

	/**
	 * Block items are created together with their blocks, i.e. while only the block registry is open.
	 * They are queued here and flushed as soon as the item registry event fires.
	 */
	private final Map<ResourceKey<Item>, Item> pendingBlockItems = new LinkedHashMap<>();

	public TooManyFurnacesNeoForge(final IEventBus modBus) {
		modBus.addListener(this::onRegister);
	}

	private void onRegister(final RegisterEvent event) {
		event.register(Registries.BLOCK, helper -> ModRegistry.registerBlocks(helper::register, pendingBlockItems::put));

		event.register(Registries.ITEM, helper -> {
			pendingBlockItems.forEach(helper::register);
			pendingBlockItems.clear();
			ModRegistry.registerItems(helper::register);
		});

		event.register(Registries.BLOCK_ENTITY_TYPE, helper -> ModRegistry.registerBlockEntities(helper::register));

		event.register(Registries.MENU, helper -> ModRegistry.registerMenus(helper::register));

		event.register(Registries.CREATIVE_MODE_TAB, helper -> {
			ModRegistry.registerCreativeTab(helper::register);
			LOGGER.info("Too Many Furnaces: registered {} furnaces, {} forges and {} upgrades",
				ModRegistry.FURNACES.size(), ModRegistry.FORGES.size(), ModRegistry.UPGRADES.size() + ModRegistry.TIER_UPGRADES.size());
		});
	}
}
