package dev.workbuddy.betterfurnaces.client;

import dev.workbuddy.betterfurnaces.ModRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class BetterFurnacesRewovenClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModRegistry.FURNACE_MENU_TYPE, BFRFurnaceScreen::new);
		MenuScreens.register(ModRegistry.FORGE_MENU_TYPE, ForgeScreen::new);
		MenuScreens.register(ModRegistry.COBBLESTONE_GENERATOR_MENU_TYPE, CobblestoneGeneratorScreen::new);
		MenuScreens.register(ModRegistry.FUEL_VERIFIER_MENU_TYPE, FuelVerifierScreen::new);
	}
}
