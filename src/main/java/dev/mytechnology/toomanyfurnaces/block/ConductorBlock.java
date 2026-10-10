package dev.mytechnology.toomanyfurnaces.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Plain crafting component block used by the tier upgrade recipes.
 */
public class ConductorBlock extends Block {
	public ConductorBlock(final BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static BlockBehaviour.Properties defaultProperties() {
		return BlockBehaviour.Properties.of().strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL);
	}
}
