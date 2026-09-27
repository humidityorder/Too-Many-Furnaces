package dev.mytechnology.toomanyfurnaces.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Plain crafting component block used by the tier upgrade recipes.
 */
public class ConductorBlock extends Block {
	private final MapCodec<ConductorBlock> codec;

	public ConductorBlock(final BlockBehaviour.Properties properties) {
		super(properties);
		this.codec = BlockBehaviour.simpleCodec(ConductorBlock::new);
	}

	public static BlockBehaviour.Properties defaultProperties() {
		return BlockBehaviour.Properties.of().strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL);
	}

	@Override
	public MapCodec<ConductorBlock> codec() {
		return this.codec;
	}
}
