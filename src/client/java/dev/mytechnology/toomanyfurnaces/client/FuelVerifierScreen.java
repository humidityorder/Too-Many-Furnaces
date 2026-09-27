package dev.mytechnology.toomanyfurnaces.client;

import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.menu.FuelVerifierMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FuelVerifierScreen extends AbstractContainerScreen<FuelVerifierMenu> {
	private static final Identifier BACKGROUND = TMF.id("textures/container/fuel_verifier_gui.png");

	public FuelVerifierScreen(final FuelVerifierMenu menu, final Inventory inventory, final Component title) {
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

		final int burnTime = this.getMenu().getBurnTime();
		final Component text = burnTime > 0
			? Component.translatable("gui." + TMF.MOD_ID + ".fuel.melts")
				.append(Component.literal(String.valueOf(burnTime / 200)))
				.append(Component.translatable("gui." + TMF.MOD_ID + ".fuel.items"))
			: Component.translatable("gui." + TMF.MOD_ID + ".fuel.empty");
		final int x = this.leftPos + (this.imageWidth - this.font.width(text)) / 2;
		graphics.text(this.font, text, x, this.topPos + 72, 0x404040, false);
	}
}
