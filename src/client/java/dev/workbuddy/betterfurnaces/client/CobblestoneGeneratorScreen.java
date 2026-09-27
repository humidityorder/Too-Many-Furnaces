package dev.workbuddy.betterfurnaces.client;

import dev.workbuddy.betterfurnaces.BFR;
import dev.workbuddy.betterfurnaces.menu.CobblestoneGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CobblestoneGeneratorScreen extends AbstractContainerScreen<CobblestoneGeneratorMenu> {
	private static final Identifier BACKGROUND = BFR.id("textures/container/cobblestone_generator_gui.png");

	public CobblestoneGeneratorScreen(final CobblestoneGeneratorMenu menu, final Inventory inventory, final Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
	}
}
