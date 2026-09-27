package dev.mytechnology.toomanyfurnaces.neoforge;

import dev.mytechnology.toomanyfurnaces.ModRegistry;
import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.client.CobblestoneGeneratorScreen;
import dev.mytechnology.toomanyfurnaces.client.ForgeScreen;
import dev.mytechnology.toomanyfurnaces.client.FuelVerifierScreen;
import dev.mytechnology.toomanyfurnaces.client.TMFurnaceScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client only wiring: connects the shared menu types to their shared screens.
 * <p>
 * NeoForge patches {@code MenuScreens#register} to private, hence the dedicated mod bus event which
 * writes directly into the vanilla screen map.
 */
@EventBusSubscriber(modid = TMF.MOD_ID, value = Dist.CLIENT)
public final class TooManyFurnacesNeoForgeClient {
	private TooManyFurnacesNeoForgeClient() {
	}

	@SubscribeEvent
	static void onRegisterMenuScreens(final RegisterMenuScreensEvent event) {
		event.register(ModRegistry.FURNACE_MENU_TYPE, TMFurnaceScreen::new);
		event.register(ModRegistry.FORGE_MENU_TYPE, ForgeScreen::new);
		event.register(ModRegistry.COBBLESTONE_GENERATOR_MENU_TYPE, CobblestoneGeneratorScreen::new);
		event.register(ModRegistry.FUEL_VERIFIER_MENU_TYPE, FuelVerifierScreen::new);
	}
}
