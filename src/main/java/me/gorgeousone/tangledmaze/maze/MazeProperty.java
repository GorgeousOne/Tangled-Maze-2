package me.gorgeousone.tangledmaze.maze;

import java.util.Arrays;

/**
 * An enum to distinguish between different properties of a maze.
 */
public enum MazeProperty {
	WALL_HEIGHT(128, 2),
	WALL_WIDTH(64, 1),
	PATH_WIDTH(64, 1),
	CURLINESS(10, 3),
	ROOF_WIDTH(64, 1);
	
	private final int maxVal;
	private final int defaultVal;
	
	MazeProperty(int maxVal, int defaultVal) {
		this.maxVal = maxVal;
		this.defaultVal = defaultVal;
	}
	
	public int getMax() {
		return maxVal;
	}
	
	public int getDefault() {
		return defaultVal;
	}
	
	public String textName() {
		return toString().toLowerCase().replace("_", " ");
	}
	
	public String commandName() {
		return toString().toLowerCase().replace("_", "");
	}
	
	public static String[] commandNames() {
		return Arrays.stream(values()).map(MazeProperty::commandName).toArray(String[]::new);
	}
	
	public static MazeProperty match(String playerInput) {
		for (MazeProperty dimension : MazeProperty.values()) {
			if (dimension.commandName().equalsIgnoreCase(playerInput)) {
				return dimension;
			}
		}
		return null;
	}
}
