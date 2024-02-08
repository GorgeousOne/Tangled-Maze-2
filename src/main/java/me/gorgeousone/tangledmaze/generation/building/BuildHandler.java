package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.event.MazeBuildEvent;
import me.gorgeousone.tangledmaze.event.MazeUnbuildEvent;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.generator.FloorGen;
import me.gorgeousone.tangledmaze.generation.generator.RoofBlockGen;
import me.gorgeousone.tangledmaze.generation.generator.WallBlockGen;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildHandler {
	
	private final JavaPlugin plugin;
	private final SessionHandler sessionHandler;
	
	public BuildHandler(JavaPlugin plugin, SessionHandler sessionHandler) {
		this.plugin = plugin;
		this.sessionHandler = sessionHandler;
	}
	
	public void buildMaze(Clip maze, MazeSettings settings, MazePart mazePart, Runnable callback) throws TextException {
		if (mazePart != MazePart.WALLS && !sessionHandler.isBuilt(maze)) {
			throw new TextException(Message.INFO_MAZE_NOT_BUILT);
		}
		sessionHandler.backupMaze(maze, settings);
		MazeBackup backup = sessionHandler.getBackup(maze);
		backup.createMazeMapIfAbsent(settings);
		MazeMap mazeMap = backup.getMazeMap();
		
		createBlockSegments(backup, mazePart, mazeMap, settings);
		BlockCollection segments = backup.getPartBlockLocs(mazePart);
		settings.computePaletteIfAbsent(mazePart);

		new BlockPlacer(mazeMap.getWorld(), segments.listBlocks(), settings.getPalette(mazePart), ConfigSettings.BLOCKS_PLACED_PER_TICK, backupBlocks -> {
			backup.setBlocksIfAbsent(mazePart, backupBlocks);
			Bukkit.getPluginManager().callEvent(new MazeBuildEvent(maze));
			callback.run();
		}).runTaskTimer(plugin, 0, 1);
	}
	
	private void createBlockSegments(MazeBackup backup, MazePart mazePart, MazeMap mazeMap, MazeSettings settings) {
		switch (mazePart) {
			case WALLS:
				backup.computeSegmentsIfAbsent(MazePart.WALLS, walls -> WallBlockGen.genWalls(mazeMap));
				break;
			case FLOOR:
				backup.computeSegmentsIfAbsent(MazePart.FLOOR, floor -> FloorGen.genFloor(mazeMap));
				break;
			case ROOF:
				backup.computeSegmentsIfAbsent(MazePart.ROOF, roof -> RoofBlockGen.genRoof(mazeMap, settings));
				break;
		}
	}
	
	public void unbuildMaze(Clip maze, MazePart mazePart, Runnable callback) throws TextException {
		if (!sessionHandler.isBuilt(maze)) {
			throw new TextException(Message.INFO_MAZE_NOT_BUILT);
		}
		MazeBackup backup = sessionHandler.getBackup(maze);
		
		//unbuild the whole maze
		if (mazePart == MazePart.WALLS) {
			new BlockResetter(plugin, backup.getAllBlocks(), ConfigSettings.BLOCKS_PLACED_PER_TICK, () -> {
				backup.removeAllMazeParts();
				backup.getMaze().updateHeights();
				backup.getMaze().setEditable(true);
				Bukkit.getPluginManager().callEvent(new MazeUnbuildEvent(maze));
				callback.run();
			}).runTaskTimer(plugin, 0, 1);
			return;
		}
		if (!backup.getBuiltParts().contains(mazePart)) {
			callback.run();
			return;
		}
		new BlockResetter(plugin, backup.getBlocks(mazePart), ConfigSettings.BLOCKS_PLACED_PER_TICK, () -> {
			backup.removeMazePart(mazePart);
			Bukkit.getPluginManager().callEvent(new MazeUnbuildEvent(maze));
			callback.run();
		}).runTaskTimer(plugin, 0, 1);
	}
}
