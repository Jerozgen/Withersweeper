package io.github.haykam821.withersweeper.game.board;

import io.github.haykam821.withersweeper.game.field.Field;
import io.github.haykam821.withersweeper.game.field.FieldVisibility;
import io.github.haykam821.withersweeper.game.field.MineField;
import io.github.haykam821.withersweeper.game.field.NumberField;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Random;

public class Board {
	private final BoardConfig config;
	private final Field[][] fields;
	private boolean placedMines = false;

	public Board(BoardConfig config) {
		this.config = config;

		this.fields = new Field[this.config.x][this.config.z];
		for (int x = 0; x < this.config.x; x++) {
			for (int z = 0; z < this.config.z; z++) {
				this.setField(x, z, new NumberField(0));
			}
		}

		// Mines can never be placed in the first uncovered field and the 8 fields neighboring it
		if (this.config.mines > (this.config.x * this.config.z) - 9) {
			throw new IllegalStateException("Cannot have more mines than candidate fields for placing mines");
		}
	}

	private boolean isOnTopOfOrAround(int x, int z, int onTopX, int onTopZ) {
		if (x < onTopX - 1 || z < onTopZ - 1) return false;
		if (x > onTopX + 1 || z > onTopZ + 1) return false;
		return true;
	}

	public boolean placeMines(int avoidX, int avoidZ, Random random) {
		if (this.placedMines) {
			return false;
		}

		for (int index = 0; index < this.config.mines; index++) {
			int x = random.nextInt(this.config.x);
			int z = random.nextInt(this.config.z);

			// Prevent mines from generating on top of or around the avoidance position
			if (this.isOnTopOfOrAround(x, z, avoidX, avoidZ)) {
				index--;
				continue;
			}

			// Prevent two mines from generating on each other
			Field field = this.getField(x, z);
			if (field instanceof MineField) {
				index--;
				continue;
			}

			this.setField(x, z, new MineField());

			// Increase the value of neighboring fields
			this.increaseField(x - 1, z - 1);
			this.increaseField(x, z - 1);
			this.increaseField(x + 1, z - 1);

			this.increaseField(x - 1, z);
			this.increaseField(x + 1, z);

			this.increaseField(x - 1, z + 1);
			this.increaseField(x, z + 1);
			this.increaseField(x + 1, z + 1);
		}

		this.placedMines = true;
		return true;
	}
	
	public void setField(int x, int z, Field field) {
		this.fields[z][x] = field;
	}

	public Field getField(int x, int z) {
		if (!this.isValidPos(x, z)) return null;
		return this.fields[z][x];
	}

	public void increaseField(int x, int z) {
		Field field = this.getField(x, z);
		if (field instanceof NumberField) {
			this.setField(x, z, ((NumberField) field).increaseValue());
		}
	}

	public boolean isValidPos(int x, int z) {
		return x >= 0 && z >= 0 && x < this.config.x && z < this.config.z;
	}

	public int getRemainingFlags() {
		int remainingFlags = this.config.mines;
		for (int x = 0; x < this.config.x; x++) {
			for (int z = 0; z < this.config.z; z++) {
				Field field = this.getField(x, z);
				if (field.getVisibility() == FieldVisibility.FLAGGED || (field.getVisibility() == FieldVisibility.UNCOVERED && field instanceof MineField)) {
					remainingFlags -= 1;
				}
			}
		}

		return remainingFlags;
	}

	public int getMinesCount() {
		return this.config.mines;
	}

	public boolean isCompleted() {
		for (int x = 0; x < this.config.x; x++) {
			for (int z = 0; z < this.config.z; z++) {
				Field field = this.getField(x, z);
				if (!field.isCompleted()) {
					return false;
				}
			}
		}

		return true;
	}

	public void build(MapTemplate template) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int x = 0; x < this.config.x; x++) {
			for (int z = 0; z < this.config.z; z++) {
				pos.set(x, 0, z);

				Field field = this.getField(x, z);
				if (field != null) {
					template.setBlockState(pos, field.getCoveredBlockState());
				}
			}
		}
	}

	public void build(WorldAccess world) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int x = 0; x < this.config.x; x++) {
			for (int z = 0; z < this.config.z; z++) {
				pos.set(x, 0, z);

				Field field = this.getField(x, z);
				if (field != null) {
					world.setBlockState(pos, field.getCoveredBlockState(), 2);
				}
			}
		}
	}

	public MapTemplate buildFromTemplate() {
		MapTemplate template = MapTemplate.createEmpty();
		this.build(template);
		return template;
	}
}