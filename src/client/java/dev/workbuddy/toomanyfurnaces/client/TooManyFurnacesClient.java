package dev.workbuddy.toomanyfurnaces.client;

import dev.workbuddy.toomanyfurnaces.ModRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class TooManyFurnacesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModRegistry.FURNACE_MENU_TYPE, TMFurnaceScreen::new);
		MenuScreens.register(ModRegistry.FORGE_MENU_TYPE, ForgeScreen::new);
		MenuScreens.register(ModRegistry.COBBLESTONE_GENERATOR_MENU_TYPE, CobblestoneGeneratorScreen::new);
		MenuScreens.register(ModRegistry.FUEL_VERIFIER_MENU_TYPE, FuelVerifierScreen::new);
	}
}
