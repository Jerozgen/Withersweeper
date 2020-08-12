package io.github.haykam821.withersweeper.game.field;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class NumberField extends Field {
	private static final BlockState[] VALUES_TO_STATES = new BlockState[] {
		Blocks.WHITE_WOOL.getDefaultState(),
		Blocks.BLUE_WOOL.getDefaultState(),
		Blocks.GREEN_WOOL.getDefaultState(),
		Blocks.RED_WOOL.getDefaultState(),
		Blocks.LIGHT_BLUE_WOOL.getDefaultState(),
		Blocks.BROWN_WOOL.getDefaultState(),
		Blocks.CYAN_WOOL.getDefaultState(),
		Blocks.BLACK_WOOL.getDefaultState(),
		Blocks.LIGHT_GRAY_WOOL.getDefaultState()
	};

	private int value;

	public NumberField(int value) {
		if (value < 0 || value >= VALUES_TO_STATES.length) {
			throw new IllegalStateException("Value must be between 0 and 8 (inclusive)");
		}

		this.value = value;
	}

	@Override
	public BlockState getBlockState() {
		return VALUES_TO_STATES[this.value];
	}

	public NumberField increaseValue() {
		return new NumberField(Math.min(this.value + 1, 8));
	}

	@Override
	public String toString() {
		return "NumberField{value=" + this.value + "}";
	}
}