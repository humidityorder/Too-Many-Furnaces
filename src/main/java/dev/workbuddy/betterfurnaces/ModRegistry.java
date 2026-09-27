package dev.workbuddy.betterfurnaces;

import dev.workbuddy.betterfurnaces.block.CobblestoneGeneratorBlock;
import dev.workbuddy.betterfurnaces.block.ConductorBlock;
import dev.workbuddy.betterfurnaces.block.FuelVerifierBlock;
import dev.workbuddy.betterfurnaces.block.TieredForgeBlock;
import dev.workbuddy.betterfurnaces.block.TieredFurnaceBlock;
import dev.workbuddy.betterfurnaces.blockentity.CobblestoneGeneratorBlockEntity;
import dev.workbuddy.betterfurnaces.blockentity.ForgeBlockEntity;
import dev.workbuddy.betterfurnaces.blockentity.FuelVerifierBlockEntity;
import dev.workbuddy.betterfurnaces.blockentity.FurnaceBlockEntity;
import dev.workbuddy.betterfurnaces.item.TierUpgradeItem;
import dev.workbuddy.betterfurnaces.item.UpgradeItem;
import dev.workbuddy.betterfurnaces.menu.BFRFurnaceMenu;
import dev.workbuddy.betterfurnaces.menu.CobblestoneGeneratorMenu;
import dev.workbuddy.betterfurnaces.menu.ForgeMenu;
import dev.workbuddy.betterfurnaces.menu.FuelVerifierMenu;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.SimpleContainer;

/**
 * Every registry object of the mod. Registration happens once, on the main thread, during
 * {@link BetterFurnacesRewoven#onInitialize}.
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

	public static MenuType<BFRFurnaceMenu> FURNACE_MENU_TYPE;
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

	private static <T extends Block> T registerBlock(final String path, final java.util.function.Function<BlockBehaviour.Properties, T> factory,
		final BlockBehaviour.Properties properties) {
		final var key = BFR.blockKey(path);
		final T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static BlockItem registerBlockItem(final String path, final Block block) {
		final var key = BFR.itemKey(path);
		final BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
		item.registerBlocks(Item.BY_BLOCK, item);
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	private static <T extends Item> T registerItem(final String path, final java.util.function.Function<Item.Properties, T> factory) {
		final var key = BFR.itemKey(path);
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
	}

	private static <T extends MenuType<?>> T registerMenu(final String path, final T type) {
		return Registry.register(BuiltInRegistries.MENU, BFR.menuKey(path), type);
	}

	public static void registerBlocks() {
		for (FurnaceTier tier : FurnaceTier.values()) {
			final TieredFurnaceBlock furnace = registerBlock(tier.furnaceId(), props -> new TieredFurnaceBlock(tier, props), TieredFurnaceBlock.defaultProperties());
			FURNACES.put(tier, furnace);
			FURNACE_ITEMS.put(tier, registerBlockItem(tier.furnaceId(), furnace));

			final TieredForgeBlock forge = registerBlock(tier.forgeId(), props -> new TieredForgeBlock(tier, props), TieredForgeBlock.defaultProperties());
			FORGES.put(tier, forge);
			FORGE_ITEMS.put(tier, registerBlockItem(tier.forgeId(), forge));
		}

		IRON_CONDUCTOR_BLOCK = registerBlock("iron_conductor_block", ConductorBlock::new, ConductorBlock.defaultProperties());
		GOLD_CONDUCTOR_BLOCK = registerBlock("gold_conductor_block", ConductorBlock::new, ConductorBlock.defaultProperties());
		NETHERHOT_CONDUCTOR_BLOCK = registerBlock("netherhot_conductor_block", ConductorBlock::new, ConductorBlock.defaultProperties());
		COBBLESTONE_GENERATOR = registerBlock("cobblestone_generator", CobblestoneGeneratorBlock::new, CobblestoneGeneratorBlock.defaultProperties());
		FUEL_VERIFIER = registerBlock("fuel_verifier", FuelVerifierBlock::new, FuelVerifierBlock.defaultProperties());

		IRON_CONDUCTOR_BLOCK_ITEM = registerBlockItem("iron_conductor_block", IRON_CONDUCTOR_BLOCK);
		GOLD_CONDUCTOR_BLOCK_ITEM = registerBlockItem("gold_conductor_block", GOLD_CONDUCTOR_BLOCK);
		NETHERHOT_CONDUCTOR_BLOCK_ITEM = registerBlockItem("netherhot_conductor_block", NETHERHOT_CONDUCTOR_BLOCK);
		COBBLESTONE_GENERATOR_ITEM = registerBlockItem("cobblestone_generator", COBBLESTONE_GENERATOR);
		FUEL_VERIFIER_ITEM = registerBlockItem("fuel_verifier", FUEL_VERIFIER);
	}

	public static void registerBlockEntities() {
		FURNACE_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			BFR.blockEntityKey("furnace"),
			new BlockEntityType<>((pos, state) -> new FurnaceBlockEntity(FURNACE_BLOCK_ENTITY, pos, state), new java.util.HashSet<>(FURNACES.values()))
		);
		FORGE_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			BFR.blockEntityKey("forge"),
			new BlockEntityType<>((pos, state) -> new ForgeBlockEntity(FORGE_BLOCK_ENTITY, pos, state), new java.util.HashSet<>(FORGES.values()))
		);
		COBBLESTONE_GENERATOR_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			BFR.blockEntityKey("cobblestone_generator"),
			new BlockEntityType<>((pos, state) -> new CobblestoneGeneratorBlockEntity(COBBLESTONE_GENERATOR_BLOCK_ENTITY, pos, state), java.util.Set.of(COBBLESTONE_GENERATOR))
		);
		FUEL_VERIFIER_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			BFR.blockEntityKey("fuel_verifier"),
			new BlockEntityType<>((pos, state) -> new FuelVerifierBlockEntity(FUEL_VERIFIER_BLOCK_ENTITY, pos, state), java.util.Set.of(FUEL_VERIFIER))
		);
	}

	public static void registerItems() {
		// functional upgrades
		UPGRADES.put("fuel_efficiency_upgrade", registerItem("fuel_efficiency_upgrade", p -> new UpgradeItem(p, UpgradeKind.FUEL_EFFICIENCY)));
		UPGRADES.put("advanced_fuel_efficiency_upgrade",
			registerItem("advanced_fuel_efficiency_upgrade", p -> new UpgradeItem(p, UpgradeKind.ADVANCED_FUEL_EFFICIENCY)));
		UPGRADES.put("ore_processing_upgrade", registerItem("ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.ORE_PROCESSING)));
		UPGRADES.put("raw_ore_processing_upgrade", registerItem("raw_ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.RAW_ORE_PROCESSING)));
		UPGRADES.put("advanced_ore_processing_upgrade",
			registerItem("advanced_ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.ADVANCED_ORE_PROCESSING)));
		UPGRADES.put("ultimate_ore_processing_upgrade",
			registerItem("ultimate_ore_processing_upgrade", p -> new UpgradeItem(p, UpgradeKind.ULTIMATE_ORE_PROCESSING)));
		UPGRADES.put("blasting_upgrade", registerItem("blasting_upgrade", p -> new UpgradeItem(p, UpgradeKind.BLASTING)));
		UPGRADES.put("smoking_upgrade", registerItem("smoking_upgrade", p -> new UpgradeItem(p, UpgradeKind.SMOKING)));
		UPGRADES.put("storage_upgrade", registerItem("storage_upgrade", p -> new UpgradeItem(p, UpgradeKind.STORAGE)));
		UPGRADES.put("autoinput_upgrade", registerItem("autoinput_upgrade", p -> new UpgradeItem(p, UpgradeKind.AUTO_INPUT)));
		UPGRADES.put("autooutput_upgrade", registerItem("autooutput_upgrade", p -> new UpgradeItem(p, UpgradeKind.AUTO_OUTPUT)));
		UPGRADES.put("factory_upgrade", registerItem("factory_upgrade", p -> new UpgradeItem(p, UpgradeKind.FACTORY)));
		UPGRADES.put("redstone_signal_upgrade", registerItem("redstone_signal_upgrade", p -> new UpgradeItem(p, UpgradeKind.REDSTONE)));
		UPGRADES.put("liquid_fuel_upgrade", registerItem("liquid_fuel_upgrade", p -> new UpgradeItem(p, UpgradeKind.LIQUID_FUEL)));

		// compatibility placeholders: registered and craftable, behaviour documented as pending
		UPGRADES.put("energy_upgrade", registerItem("energy_upgrade", p -> new UpgradeItem(p, UpgradeKind.ENERGY)));
		UPGRADES.put("xp_tank_upgrade", registerItem("xp_tank_upgrade", p -> new UpgradeItem(p, UpgradeKind.XP)));
		UPGRADES.put("generator_upgrade", registerItem("generator_upgrade", p -> new UpgradeItem(p, UpgradeKind.GENERATOR)));
		UPGRADES.put("color_upgrade", registerItem("color_upgrade", p -> new UpgradeItem(p, UpgradeKind.COLOR)));
		UPGRADES.put("piping_upgrade", registerItem("piping_upgrade", p -> new UpgradeItem(p, UpgradeKind.PIPING)));

		// Tier upgrades. {@code from == null} means the vanilla furnace; both the vanilla furnace and
		// an intermediate tier can be a valid starting point for the same target tier.
		putTier("copper_upgrade", null, FurnaceTier.COPPER);
		putTier("copper_iron_upgrade", FurnaceTier.COPPER, FurnaceTier.IRON);
		putTier("iron_upgrade", null, FurnaceTier.IRON);
		putTier("steel_upgrade", FurnaceTier.IRON, FurnaceTier.STEEL);
		putTier("steel_gold_upgrade", FurnaceTier.STEEL, FurnaceTier.GOLD);
		putTier("gold_upgrade", FurnaceTier.IRON, FurnaceTier.GOLD);
		putTier("amethyst_upgrade", FurnaceTier.GOLD, FurnaceTier.AMETHYST);
		putTier("amethyst_diamond_upgrade", FurnaceTier.AMETHYST, FurnaceTier.DIAMOND);
		putTier("diamond_upgrade", FurnaceTier.GOLD, FurnaceTier.DIAMOND);
		putTier("platinum_upgrade", FurnaceTier.DIAMOND, FurnaceTier.PLATINUM);
		putTier("platinum_netherhot_upgrade", FurnaceTier.PLATINUM, FurnaceTier.NETHERHOT);
		putTier("netherhot_upgrade", FurnaceTier.DIAMOND, FurnaceTier.NETHERHOT);
		putTier("extreme_upgrade", FurnaceTier.NETHERHOT, FurnaceTier.EXTREME);
		putTier("ultimate_upgrade", FurnaceTier.EXTREME, FurnaceTier.ULTIMATE);
	}

	private static void putTier(final String path, final FurnaceTier from, final FurnaceTier to) {
		TIER_UPGRADES.put(path, registerItem(path, p -> new TierUpgradeItem(p, from, to)));
	}

	public static void registerMenus() {
		FURNACE_MENU_TYPE = registerMenu("furnace", new MenuType<>(
			(id, inventory) -> new BFRFurnaceMenu(id, inventory, new SimpleContainer(3), new SimpleContainerData(4), new SimpleContainer(3)),
			FeatureFlags.VANILLA_SET
		));
		FORGE_MENU_TYPE = registerMenu("forge", new MenuType<>(
			(id, inventory) -> new ForgeMenu(id, inventory, new SimpleContainer(7), new SimpleContainer(3), new SimpleContainerData(6)),
			FeatureFlags.VANILLA_SET
		));
		COBBLESTONE_GENERATOR_MENU_TYPE = registerMenu("cobblestone_generator", new MenuType<>(
			(id, inventory) -> new CobblestoneGeneratorMenu(id, inventory, new SimpleContainer(CobblestoneGeneratorBlockEntity.SIZE)),
			FeatureFlags.VANILLA_SET
		));
		FUEL_VERIFIER_MENU_TYPE = registerMenu("fuel_verifier", new MenuType<>(
			(id, inventory) -> new FuelVerifierMenu(id, inventory, new SimpleContainer(1), new SimpleContainerData(2)),
			FeatureFlags.VANILLA_SET
		));
	}

	public static void registerCreativeTab() {
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

		CREATIVE_MODE_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			BFR.tabKey("main"),
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
				.title(Component.translatable("itemGroup." + BFR.MOD_ID))
				.icon(() -> new ItemStack(FURNACES.get(FurnaceTier.COPPER)))
				.displayItems((parameters, output) -> {
					for (Item item : items) {
						output.accept(new ItemStack(item));
					}
				})
				.build()
		);
	}
}
