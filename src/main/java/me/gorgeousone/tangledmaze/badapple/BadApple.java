package me.gorgeousone.tangledmaze.badapple;

import me.gorgeousone.tangledmaze.generation.building.BlockPalette;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;

import java.util.Set;

public class BadApple {
	
	public static final int FLOOR_Y = -63;
	public static Set<BlockVec> LAST_BLOCKS;
	public static MazeSettings SETTINGS;
	
	public static void setBuildSettings() {
		BlockPalette black = new BlockPalette();
		black.addBlock(BlockType.get("black_concrete"), 1);
		
		SETTINGS = new MazeSettings();
		SETTINGS.setPalette(MazePart.WALLS, black);
		SETTINGS.setValue(MazeProperty.CURLINESS, 1);
		SETTINGS.setValue(MazeProperty.SEED, 1);
	}
}
