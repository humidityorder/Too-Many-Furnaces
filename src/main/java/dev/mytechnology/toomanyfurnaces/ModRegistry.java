package dev.mytechnology.toomanyfurnaces;

import dev.mytechnology.toomanyfurnaces.block.CobblestoneGeneratorBlock;
import dev.mytechnology.toomanyfurnaces.block.ConductorBlock;
import dev.mytechnology.toomanyfurnaces.block.FuelVerifierBlock;
import dev.mytechnology.toomanyfurnaces.block.TieredForgeBlock;
import dev.mytechnology.toomanyfurnaces.block.TieredFurnaceBlock;
import dev.mytechnology.toomanyfurnaces.blockentity.CobblestoneGeneratorBlockEntity;
import dev.mytechnology.toomanyfurnaces.blockentity.ForgeBlockEntity;
import dev.mytechnology.toomanyfurnaces.blockentity.FuelVerifierBlockEntity;
import dev.mytechnology.toomanyfurnaces.blockentity.FurnaceBlockEntity;
import dev.mytechnology.toomanyfurnaces.item.TierUpgradeItem;
import dev.mytechnology.toomanyfurnaces.item.UpgradeItem;
import dev.mytechnology.toomanyfurnaces.menu.TMFurnaceMenu;
import dev.mytechnology.toomanyfurnaces.menu.CobblestoneGeneratorMenu;
import dev.mytechnology.toomanyfurnaces.menu.ForgeMenu;
import dev.mytechnology.toomanyfurnaces.menu.FuelVerifierMenu;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Every registry object of the mod, shared by all loaders.
 * <p>
 * The registration calls are injected by each loader: Fabric registers straight into the built-in
 * registries, while Forge and NeoForge hand the entries to their own registration event. Ordering
 * matters - blocks first, then items, block entity types, menus and finally the creative tab.
 */
public final class ModRegistry {
	public static final Map<FurnaceTier, TieredFurnaceBlock> FURNACES = new EnumMap<>(FurnaceTier.class);
	public static final Map<FurnaceTier, TieredForgeBlock> FORGES = new EnumMap<>(FurnaceTier.class);
	public static final Map<FurnaceTier, BlockItem> FURNACE_ITEMS = new EnumMap<>(FurnaceTier.class);
	public static final Map<FurnaceTier, BlockItem> FORGE_ITEMS = new EnumMap<>(FurnaceTier.class);

	public static BlockEntityType<FurnaceBlockEntity> FURNACE_BLOCK_ENTITY;
	public static BlockEntityType<ForgeBlockEntity> FORGE_BLOCK_ENTITY;
	public static BlockEntityType<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_BLOCK_ENTITY;
	public static BlockEntityType<FuelVerifierBlockEntity> FUEL_VERIFIER_BLOCK_ENTITY;

	public static MenuType<TMFurnaceMenu> FURNACE_MENU_TYPE;
	public static MenuType<ForgeMenu> FORGE_MENU_TYPE;
	public static MenuType<CobblestoneGeneratorMenu> COBBLESTONE_GENERATOR_MENU_TYPE;
	public static MenuType<FuelVerifierMenu> FUEL_VERIFIER_MENU_TYPE;

	public static Block IRON_CONDUCTOR_BLOCK;
	public static Block GOLD_CONDUCTOR_BLOCK;
	public static Block NETHERHOT_CONDUCTOR_BLOCK;
	public static Block COBBLESTONE_GENERATOR;
	public static Block FUEL_VERIFIER;

	public static BlockItem IRON_CONDUCTOR_BLOCK_ITEM;
	public static BlockItem GOLD_CONDUCTOR_BLOCK_ITEM;
	public static BlockItem NETHERHOT_CONDUCTOR_BLOCK_ITEM;
	public static BlockItem COBBLESTONE_GENERATOR_ITEM;
	public static BlockItem FUEL_VERIFIER_ITEM;

	public static final Map<String, UpgradeItem> UPGRADES = new LinkedHashMap<>();
	public static final Map<String, TierUpgradeItem> TIER_UPGRADES = new LinkedHashMap<>();

	public static CreativeModeTab CREATIVE_MODE_TAB;

	private ModRegistry() {
	}

	// ------------------------------------------------------------------ helpers

	private static <T extends Block> T registerBlock(
		final BiConsumer<ResourceKey<Block>, Block> blocks, final String path,
		final Function<BlockBehaviour.Properties, T> factory, final BlockBehaviour.Properties properties
	) {
		final ResourceKey<Block> key = TMF.blockKey(path);
		final T block = factory.apply(properties.setId(key));
		blocks.accept(key, block);
		return block;
	}

	private static BlockItem registerBlockItem(final BiConsumer<ResourceKey<Item>, Item> items, final String path, final Block block) {
		final ResourceKey<Item> key = TMF.itemKey(path);
		final BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
		item.registerBlocks(Item.BY_BLOCK, item);
		items.accept(key, item);
		return item;
	}

	private static <T extends Item> T registerItem(
		final BiConsumer<ResourceKey<Item>, Item> items, final String path, final Function<Item.Properties, T> factory
	) {
		final ResourceKey<Item> key = TMF.itemKey(path);
		final T item = factory.apply(new Item.Properties().setId(key));
		items.accept(key, item);
		return item;
	}

	private static <T extends MenuType<?>> T registerMenu(
		final BiConsumer<ResourceKey<MenuType<?>>, MenuType<?>> menus, final String path, final T type
	) {
		menus.accept(TMF.menuKey(path), type);
		return type;
	}

	// ------------------------------------------------------------------ phases

	public static void registerBlocks(final BiConsumer<ResourceKey<Block>, Block> blocks, final BiConsumer<ResourceKey<Item>, Item> items) {
		for (FurnaceTier tier : FurnaceTier.values()) {
			final TieredFurnaceBlock furnace = registerBlock(blocks, tier.furnaceId(), p -> new TieredFurnaceBlock(tier, p), TieredFurnaceBlock.defaultProperties());
			FURNACES.put(tier, furnace);
			FURNACE_ITEMS.put(tier, registerBlockItem(items, tier.furnaceId(), furnace));

			final TieredForgeBlock forge = registerBlock(blocks, tier.forgeId(), p -> new TieredForgeBlock(tier, p), TieredForgeBlock.defaultProperties());
			FORGES.put(tier, forge);
			FORGE_ITEMS.put(tier, registerBlockItem(items, tier.forgeId(), forge));
		}

		IRON_CONDUCTOR_BLOCK = registerBlock(blocks, "iron_conductor_block", ConductorBlock::new, ConductorBlock.defaultProperties());
		GOLD_CONDUCTOR_BLOCK = registerBlock(blocks, "gold_conductor_block", ConductorBlock::new, ConductorBlock.defaultProperties());
		NETHERHOT_CONDUCTOR_BLOCK = registerBlock(blocks, "netherhot_conductor_block", ConductorBlock::new, ConductorBlock.defaultProperties());
		COBBLESTONE_GENERATOR = registerBlock(blocks, "cobblestone_generator", CobblestoneGeneratorBlock::new, CobblestoneGeneratorBlock.defaultProperties());
		FUEL_VERIFIER = registerBlock(blocks, "fuel_verifier", FuelVerifierBlock::new, FuelVerifierBlock.defaultProperties());

		IRON_CONDUCTOR_BLOCK_ITEM = registerBlockItem(items, "iron_conductor_block", IRON_CONDUCTOR_BLOCK);
		GOLD_CONDUCTOR_BLOCK_ITEM = registerBlockItem(items, "gold_conductor_block", GOLD_CONDUCTOR_BLOCK);
		NETHERHOT_CONDUCTOR_BLOCK_ITEM = registerBlockItem(items, "netherhot_conductor_block", NETHERHOT_CONDUCTOR_BLOCK);
		COBBLESTONE_GENERATOR_ITEM = registerBlockItem(items, "cobblestone_generator", COBBLESTONE_GENERATOR);
		FUEL_VERIFIER_ITEM = registerBlockItem(items, "fuel_verifier", FUEL_VERIFIER);
	}

	public static void registerBlockEntities(final BiConsumer<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> types) {
		FURNACE_BLOCK_ENTITY = new BlockEntityType<>(
			(pos, state) -> new FurnaceBlockEntity(FURNACE_BLOCK_ENTITY, pos, state), new HashSet<>(FURNACES.values()));
		FORGE_BLOCK_ENTITY = new BlockEntityType<>(
			(pos, state) -> new ForgeBlockEntity(FORGE_BLOCK_ENTITY, pos, state), new HashSet<>(FORGES.values()));
		COBBLESTONE_GENERATOR_BLOCK_ENTITY = new BlockEntityType<>(
			(pos, state) -> new CobblestoneGeneratorBlockEntity(COBBLESTONE_GENERATOR_BLOCK_ENTITY, pos, state), Set.of(COBBLESTONE_GENERATOR));
		FUEL_VERIFIER_BLOCK_ENTITY = new BlockEntityType<>(
			(pos, state) -> new FuelVerifierBlockEntity(FUEL_VERIFIER_BLOCK_ENTITY, pos, state), Set.of(FUEL_VERIFIER));

		types.accept(TMF.blockEntityKey("furnace"), FURNACE_BLOCK_ENTITY);
		types.accept(TMF.blockEntityKey("forge"), FORGE_BLOCK_ENTITY);
		types.accept(TMF.blockEntityKey("cobblestone_generator"), COBBLESTONE_GENERATOR_BLOCK_ENTITY);
		types.accept(TMF.blockEntityKey("fuel_verifier"), FUEL_VERIFIER_BLOCK_ENTITY);
	}

	public static void registerItems(final BiConsumer<ResourceKey<Item>, Item> items) {
		// functional upgrades
		UPGRADES.put("fuel_efficiency_upgrade", registerItem(items, "fuel_efficiency_upgrade", p -> new UpgradeItem(p, UpgradeKind.FUEL_EFFICIENCY)));
		UPGRADES.put("advanced_fuel_efficiency_upgrade",
			registerItem(items, "advanced_fuel_efficiency_upgrade", p -> new UpgradeItem(p, UpgradeKind.ADVANCED_FUEL_EFFICIENCY)));
		UPGRADES.put("ore_processing_upgrade", registerItem(items, "ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.ORE_PROCESSING)));
		UPGRADES.put("raw_ore_processing_upgrade", registerItem(items, "raw_ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.RAW_ORE_PROCESSING)));
		UPGRADES.put("advanced_ore_processing_upgrade",
			registerItem(items, "advanced_ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.ADVANCED_ORE_PROCESSING)));
		UPGRADES.put("ultimate_ore_processing_upgrade",
			registerItem(items, "ultimate_ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.ULTIMATE_ORE_PROCESSING)));
		UPGRADES.put("blasting_upgrade", registerItem(items, "blasting_upgrade", p -> new UpgradeItem(p, UpgradeKind.BLASTING)));
		UPGRADES.put("smoking_upgrade", registerItem(items, "smoking_upgrade", p -> new UpgradeItem(p, UpgradeKind.SMOKING)));
		UPGRADES.put("storage_upgrade", registerItem(items, "storage_upgrade", p -> new UpgradeItem(p, UpgradeKind.STORAGE)));
		UPGRADES.put("autoinput_upgrade", registerItem(items, "autoinput_upgrade", p -> new UpgradeItem(p, UpgradeKind.AUTO_INPUT)));
		UPGRADES.put("autooutput_upgrade", registerItem(items, "autooutput_upgrade", p -> new UpgradeItem(p, UpgradeKind.AUTO_OUTPUT)));
		UPGRADES.put("factory_upgrade", registerItem(items, "factory_upgrade", p -> new UpgradeItem(p, UpgradeKind.FACTORY)));
		UPGRADES.put("redstone_signal_upgrade", registerItem(items, "redstone_signal_upgrade", p -> new UpgradeItem(p, UpgradeKind.REDSTONE)));
		UPGRADES.put("liquid_fuel_upgrade", registerItem(items, "liquid_fuel_upgrade", p -> new UpgradeItem(p, UpgradeKind.LIQUID_FUEL)));

		// compatibility placeholders: registered and craftable, behaviour documented as pending
		UPGRADES.put("energy_upgrade", registerItem(items, "energy_upgrade", p -> new UpgradeItem(p, UpgradeKind.ENERGY)));
		UPGRADES.put("xp_tank_upgrade", registerItem(items, "xp_tank_upgrade", p -> new UpgradeItem(p, UpgradeKind.XP)));
		UPGRADES.put("generator_upgrade", registerItem(items, "generator_upgrade", p -> new UpgradeItem(p, UpgradeKind.GENERATOR)));
		UPGRADES.put("color_upgrade", registerItem(items, "color_upgrade", p -> new UpgradeItem(p, UpgradeKind.COLOR)));
		UPGRADES.put("piping_upgrade", registerItem(items, "piping_upgrade", p -> new UpgradeItem(p, UpgradeKind.PIPING)));

		// Tier upgrades. A null source means the vanilla furnace; both the vanilla furnace and an
		// intermediate tier can be a valid starting point for the same target tier.
		putTier(items, "copper_upgrade", null, FurnaceTier.COPPER);
		putTier(items, "copper_iron_upgrade", FurnaceTier.COPPER, FurnaceTier.IRON);
		putTier(items, "iron_upgrade", null, FurnaceTier.IRON);
		putTier(items, "steel_upgrade", FurnaceTier.IRON, FurnaceTier.STEEL);
		putTier(items, "steel_gold_upgrade", FurnaceTier.STEEL, FurnaceTier.GOLD);
		putTier(items, "gold_upgrade", FurnaceTier.IRON, FurnaceTier.GOLD);
		putTier(items, "amethyst_upgrade", FurnaceTier.GOLD, FurnaceTier.AMETHYST);
		putTier(items, "amethyst_diamond_upgrade", FurnaceTier.AMETHYST, FurnaceTier.DIAMOND);
		putTier(items, "diamond_upgrade", FurnaceTier.GOLD, FurnaceTier.DIAMOND);
		putTier(items, "platinum_upgrade", FurnaceTier.DIAMOND, FurnaceTier.PLATINUM);
		putTier(items, "platinum_netherhot_upgrade", FurnaceTier.PLATINUM, FurnaceTier.NETHERHOT);
		putTier(items, "netherhot_upgrade", FurnaceTier.DIAMOND, FurnaceTier.NETHERHOT);
		putTier(items, "extreme_upgrade", FurnaceTier.NETHERHOT, FurnaceTier.EXTREME);
		putTier(items, "ultimate_upgrade", FurnaceTier.EXTREME, FurnaceTier.ULTIMATE);
	}

	private static void putTier(final BiConsumer<ResourceKey<Item>, Item> items, final String path, final FurnaceTier from, final FurnaceTier to) {
		TIER_UPGRADES.put(path, registerItem(items, path, p -> new TierUpgradeItem(p, from, to)));
	}

	public static void registerMenus(final BiConsumer<ResourceKey<MenuType<?>>, MenuType<?>> menus) {
		FURNACE_MENU_TYPE = registerMenu(menus, "furnace", new MenuType<>(
			(id, inventory) -> new TMFurnaceMenu(id, inventory, new SimpleContainer(3), new SimpleContainerData(4), new SimpleContainer(3)),
			FeatureFlags.VANILLA_SET
		));
		FORGE_MENU_TYPE = registerMenu(menus, "forge", new MenuType<>(
			(id, inventory) -> new ForgeMenu(id, inventory, new SimpleContainer(7), new SimpleContainer(3), new SimpleContainerData(6)),
			FeatureFlags.VANILLA_SET
		));
		COBBLESTONE_GENERATOR_MENU_TYPE = registerMenu(menus, "cobblestone_generator", new MenuType<>(
			(id, inventory) -> new CobblestoneGeneratorMenu(id, inventory, new SimpleContainer(CobblestoneGeneratorBlockEntity.SIZE)),
			FeatureFlags.VANILLA_SET
		));
		FUEL_VERIFIER_MENU_TYPE = registerMenu(menus, "fuel_verifier", new MenuType<>(
			(id, inventory) -> new FuelVerifierMenu(id, inventory, new SimpleContainer(1), new SimpleContainerData(2)),
			FeatureFlags.VANILLA_SET
		));
	}

	public static void registerCreativeTab(final BiConsumer<ResourceKey<CreativeModeTab>, CreativeModeTab> tabs) {
		final List<Item> items = new ArrayList<>();
		for (FurnaceTier tier : FurnaceTier.values()) {
			items.add(FURNACE_ITEMS.get(tier));
		}

		for (FurnaceTier tier : FurnaceTier.values()) {
			items.add(FORGE_ITEMS.get(tier));
		}

		items.add(IRON_CONDUCTOR_BLOCK_ITEM);
		items.add(GOLD_CONDUCTOR_BLOCK_ITEM);
		items.add(NETHERHOT_CONDUCTOR_BLOCK_ITEM);
		items.add(COBBLESTONE_GENERATOR_ITEM);
		items.add(FUEL_VERIFIER_ITEM);
		UPGRADES.values().forEach(items::add);
		TIER_UPGRADES.values().forEach(items::add);

		CREATIVE_MODE_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
			.title(Component.translatable("itemGroup." + TMF.MOD_ID))
			.icon(() -> new ItemStack(FURNACES.get(FurnaceTier.COPPER)))
			.displayItems((parameters, output) -> {
				for (Item item : items) {
					output.accept(new ItemStack(item));
				}
			})
			.build();

		tabs.accept(TMF.tabKey("main"), CREATIVE_MODE_TAB);
	}
}
