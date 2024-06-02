package me.gorgeousone.badapplemaze.generation.building;

import me.gorgeousone.badapplemaze.BadApplePlugin;
import me.gorgeousone.badapplemaze.generation.BlockCollection;
import me.gorgeousone.badapplemaze.generation.MazeMap;
import me.gorgeousone.badapplemaze.generation.generator.WallBlockGen;
import me.gorgeousone.badapplemaze.maze.MazePart;
import me.gorgeousone.badapplemaze.maze.MazeSettings;
import me.gorgeousone.badapplemaze.util.BlockVec;
import me.gorgeousone.badapplemaze.util.text.TextException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * A class to handle the building and unbuilding of maze walls, floor and roof distributed over time.
 */
public class BuildHandler {

	private final JavaPlugin plugin;

	public BuildHandler(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Starts the building process of maze walls, floor or roof.
	 *
	 * @param callback a runnable that is called after the building process is finished
	 * @throws TextException if the maze is not built yet
	 */
	public void buildMaze(MazeMap newFrame, MazeSettings settings, Runnable callback) throws TextException {
		BlockCollection segments = WallBlockGen.genWalls(newFrame);
		Set<BlockVec> nextBlocks = segments.listBlocks();
		Set<BlockVec> newBlocks = new HashSet<>();
		Set<BlockVec> newAir = new HashSet<>();
		filterBlocks(BadApplePlugin.LAST_BLOCKS, nextBlocks, newBlocks, newAir);
		BadApplePlugin.LAST_BLOCKS = nextBlocks;

		new BlockPlacer(
				newFrame.getWorld(),
				newBlocks,
				newAir,
				settings.getPalette(MazePart.WALLS),
				-1,
				backupBlocks -> {
					callback.run();
				}).runTaskTimer(plugin, 0, 1);
	}

	private void filterBlocks(Set<BlockVec> lastBlocks, Set<BlockVec> nextBlocks, Set<BlockVec> newBlocks, Set<BlockVec> newAir) {
		newBlocks.addAll(nextBlocks);

		if (lastBlocks != null) {
			newBlocks.removeAll(lastBlocks);
			newAir.addAll(lastBlocks);
			newAir.removeAll(nextBlocks);
		}
	}
}