package me.gorgeousone.tangledmaze.maze;

import me.gorgeousone.tangledmaze.generation.building.BlockPalette;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A class to store build settings of a maze, like the dimensions of the maze parts and the block palettes for each maze part.
 */
public class MazeSettings {
	
	private final HashMap<MazeProperty, Integer> properties;
	private final HashMap<MazePart, BlockPalette> blockPalettes;
	
	public MazeSettings() {
		properties = new HashMap<>();
		Arrays.stream(MazeProperty.values()).forEach(property -> properties.put(property, property.getDefault()));
		blockPalettes = new HashMap<>();
		blockPalettes.put(MazePart.WALLS, BlockPalette.getDefault());
	}
	
	public int getValue(MazeProperty property) {
		return properties.get(property);
	}
	
	public int setValue(MazeProperty property, int newValue) {
		newValue = Math.max(1, Math.min(property.getMax(), newValue));
		properties.put(property, newValue);
		return newValue;
	}
	
	public void setPalette(MazePart mazePart, BlockPalette palette) {
		blockPalettes.put(mazePart, palette);
	}
	
	public BlockPalette getPalette(MazePart mazePart) {
		return blockPalettes.get(mazePart);
	}
	
	public void computePaletteIfAbsent(MazePart mazePart) {
		if (!blockPalettes.containsKey(mazePart)) {
			blockPalettes.put(mazePart, blockPalettes.get(MazePart.WALLS));
		}
	}
}
