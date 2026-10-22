package dev.mytechnology.toomanyfurnaces.client;

import dev.mytechnology.toomanyfurnaces.TMF;
import dev.mytechnology.toomanyfurnaces.menu.CobblestoneGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CobblestoneGeneratorScreen extends AbstractContainerScreen<CobblestoneGeneratorMenu> {
	private static final Identifier BACKGROUND = TMF.id("textures/container/cobblestone_generator_gui.png");

	// Progress meter drawn between the two fluid slots, above the output slot.
	private static final int PROGRESS_BAR_X = 74;
	private static final int PROGRESS_BAR_Y = 36;
	private static final int PROGRESS_BAR_WIDTH = 28;
	private static final int PROGRESS_BAR_HEIGHT = 3;
	private static final int PROGRESS_BAR_EMPTY = 0xFF555555;
	private static final int PROGRESS_BAR_FULL = 0xFFD8D8D8;

	private Button productButton;
	private Button runningButton;

	public CobblestoneGeneratorScreen(final CobblestoneGeneratorMenu menu, final Inventory inventory, final Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		this.productButton = this.addRenderableWidget(Button.builder(this.menu.getProduct().displayName(), button ->
				this.sendMenuButtonClick(CobblestoneGeneratorMenu.BUTTON_CYCLE_PRODUCT))
			.bounds(this.leftPos + 28, this.topPos + 64, 70, 16)
			.tooltip(Tooltip.create(this.label("product.tooltip")))
			.build());
		this.runningButton = this.addRenderableWidget(Button.builder(this.runningLabel(), button ->
				this.sendMenuButtonClick(CobblestoneGeneratorMenu.BUTTON_TOGGLE_RUNNING))
			.bounds(this.leftPos + 102, this.topPos + 64, 46, 16)
			.tooltip(Tooltip.create(this.label("toggle.tooltip")))
			.build());
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.productButton.setMessage(this.menu.getProduct().displayName());
		this.runningButton.setMessage(this.runningLabel());
	}

	private Component runningLabel() {
		return this.label(this.menu.isRunning() ? "running" : "paused");
	}

	private Component label(final String key) {
		return Component.translatable("gui." + TMF.MOD_ID + ".cobblestone_generator." + key);
	}

	private void sendMenuButtonClick(final int id) {
		if (this.minecraft != null && this.minecraft.gameMode != null) {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
		}
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		final int total = this.menu.getProduct().ticks();
		final int progress = Math.min(this.menu.getProgress(), total);
		final int x = this.leftPos + PROGRESS_BAR_X;
		final int y = this.topPos + PROGRESS_BAR_Y;
		graphics.fill(x, y, x + PROGRESS_BAR_WIDTH, y + PROGRESS_BAR_HEIGHT, PROGRESS_BAR_EMPTY);
		graphics.fill(x, y, x + PROGRESS_BAR_WIDTH * progress / total, y + PROGRESS_BAR_HEIGHT, PROGRESS_BAR_FULL);
	}
}
