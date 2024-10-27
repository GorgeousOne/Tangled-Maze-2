package me.gorgeousone.tangledmaze.maze;

import java.util.Arrays;

/**
 * An enum to distinguish between different properties of a maze.
 */
public enum MazeProperty {
	WALL_HEIGHT(2, 128),
	WALL_WIDTH(1, 64),
	PATH_WIDTH(1, 64),
	CURLINESS(2, 10),
	ROOF_WIDTH(1, 64),
	ROOM_COUNT(0, 100, 0),
	ROOM_SIZE(15, 65, 3);
	
	private final int maxVal;
	private final int minVal;
	private final int defaultVal;

	MazeProperty(int defaultVal, int maxVal) {
		this(defaultVal, maxVal, 1);
	}

	MazeProperty(int defaultVal, int maxVal, int minVal) {
		this.maxVal = maxVal;
		this.defaultVal = defaultVal;
		this.minVal = minVal;
	}

	public int getDefault() {
		return defaultVal;
	}

	public int getMax() {
		return maxVal;
	}

	public int getMin() {
		return minVal;
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
