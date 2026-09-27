package dev.mytechnology.toomanyfurnaces.forge;

import dev.mytechnology.toomanyfurnaces.ModRegistry;
import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.client.CobblestoneGeneratorScreen;
import dev.mytechnology.toomanyfurnaces.client.ForgeScreen;
import dev.mytechnology.toomanyfurnaces.client.FuelVerifierScreen;
import dev.mytechnology.toomanyfurnaces.client.TMFurnaceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client only wiring: connects the shared menu types to their shared screens.
 * <p>
 * Unlike NeoForge, Forge leaves {@code MenuScreens#register} public, so the vanilla call is made from
 * {@link FMLClientSetupEvent} - wrapped in {@code enqueueWork} because screen registration has to run
 * on the client thread.
 */
@Mod.EventBusSubscriber(modid = TMF.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TooManyFurnacesForgeClient {
	private TooManyFurnacesForgeClient() {
	}

	@SubscribeEvent
	static void onClientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(ModRegistry.FURNACE_MENU_TYPE, TMFurnaceScreen::new);
			MenuScreens.register(ModRegistry.FORGE_MENU_TYPE, ForgeScreen::new);
			MenuScreens.register(ModRegistry.COBBLESTONE_GENERATOR_MENU_TYPE, CobblestoneGeneratorScreen::new);
			MenuScreens.register(ModRegistry.FUEL_VERIFIER_MENU_TYPE, FuelVerifierScreen::new);
		});
	}
}
