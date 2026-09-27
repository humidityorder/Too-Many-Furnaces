package dev.mytechnology.toomanyfurnaces.client;

import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.menu.ForgeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Custom forge GUI drawn from the original Better Furnaces Reforged texture. Everything is a plain
 * static blit plus progress bars, so there is no per-frame dynamic renderer.
 */
public class ForgeScreen extends AbstractContainerScreen<ForgeMenu> {
	private static final Identifier BACKGROUND = TMF.id("textures/container/forge_gui.png");
	private static final Identifier WIDGETS = TMF.id("textures/container/widgets.png");
	private static final int IMAGE_WIDTH = 176;
	private static final int IMAGE_HEIGHT = 206;

	public ForgeScreen(final ForgeMenu menu, final Inventory inventory, final Component title) {
		super(menu, inventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		// centred, like the reference GUI, so the label sits between the fuel slot and the output tray
		this.inventoryLabelX = (this.imageWidth - this.font.width(this.playerInventoryTitle)) / 2;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		final int xo = this.leftPos;
		final int yo = this.topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		final ForgeMenu menu = this.getMenu();
		// slot backgrounds for the three inputs, the fuel slot and the output tray
		for (int i = 0; i < ForgeMenu.INPUT_SLOTS; i++) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, xo + 26 + i * 18, yo + 61, 0.0F, 171.0F, 18, 18, 256, 256);
		}

		graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, xo + 7, yo + 99, 18.0F, 171.0F, 18, 18, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, xo + 103, yo + 75, 0.0F, 229.0F, 62, 26, 256, 256);
		for (int i = 0; i < ForgeMenu.UPGRADE_END - ForgeMenu.UPGRADE_START; i++) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, xo + 6 + i * 18, yo + 4, 0.0F, 171.0F, 18, 18, 256, 256);
		}

		// one flame per input showing that lane's cooking progress
		for (int i = 0; i < ForgeMenu.INPUT_SLOTS; i++) {
			final int flame = Mth.ceil(menu.getCookProgress(i) * 13.0F);
			if (flame > 0) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, xo + 28 + i * 18, yo + 93 - flame, 176.0F, 12.0F - flame, 14, flame + 1, 256, 256);
			}
		}

		// horizontal fuel gauge between the inputs and the outputs
		final int lit = Mth.ceil(menu.getLitProgress() * 24.0F);
		if (lit > 0) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, xo + 80, yo + 80, 176.0F, 14.0F, lit + 1, 16, 256, 256);
		}
	}
}
