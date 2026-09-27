package dev.workbuddy.betterfurnaces.client;

import dev.workbuddy.betterfurnaces.BFR;
import dev.workbuddy.betterfurnaces.menu.BFRFurnaceMenu;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

/**
 * Reuses the vanilla furnace screen (and its recipe book) with the modded furnace GUI texture.
 */
public class BFRFurnaceScreen extends AbstractFurnaceScreen<BFRFurnaceMenu> {
	private static final Identifier TEXTURE = BFR.id("textures/container/furnace_gui.png");
	private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
	private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smeltable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
		new RecipeBookComponent.TabInfo(net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory.FURNACE),
		new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.FURNACE_FOOD),
		new RecipeBookComponent.TabInfo(Items.STONE, RecipeBookCategories.FURNACE_BLOCKS),
		new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.EMERALD, RecipeBookCategories.FURNACE_MISC)
	);

	public BFRFurnaceScreen(final BFRFurnaceMenu menu, final Inventory inventory, final Component title) {
		super(menu, inventory, title, FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
	}

	/**
	 * The vanilla position (left edge, mid height) would cover the upgrade slot column, so the button
	 * is moved to the empty right hand side of the GUI.
	 */
	@Override
	protected net.minecraft.client.gui.navigation.ScreenPosition getRecipeBookButtonPosition() {
		return new net.minecraft.client.gui.navigation.ScreenPosition(this.leftPos + this.imageWidth - 24, this.height / 2 - 49);
	}
}
