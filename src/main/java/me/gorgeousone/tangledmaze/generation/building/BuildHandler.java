package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.event.MazeBuildEvent;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.generator.FloorGen;
import me.gorgeousone.tangledmaze.generation.generator.RoofGen;
import me.gorgeousone.tangledmaze.generation.generator.WallGen;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class BuildHandler {
	
	private final JavaPlugin plugin;
	private final SessionHandler sessionHandler;
	
	public BuildHandler(JavaPlugin plugin, SessionHandler sessionHandler) {
		this.plugin = plugin;
		this.sessionHandler = sessionHandler;
	}
	
	public void buildMaze(UUID playerId, Clip maze, MazeSettings settings, MazePart mazePart) throws TextException {
		if (mazePart != MazePart.WALLS && !sessionHandler.hasBackup(maze)) {
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
			boolean isFirstBuild = backup.hasBlocks(mazePart);
			backup.setBlocksIfAbsent(mazePart, backupBlocks);
			
			if (mazePart == MazePart.WALLS && isFirstBuild) {
				Bukkit.getPluginManager().callEvent(new MazeBuildEvent(maze, playerId));
			}
		}).runTaskTimer(plugin, 0, 1);
	}
	
	private void createBlockSegments(MazeBackup backup, MazePart mazePart, MazeMap mazeMap, MazeSettings settings) {
		switch (mazePart) {
			case WALLS:
				backup.computeSegmentsIfAbsent(MazePart.WALLS, walls -> WallGen.genWalls(mazeMap));
				break;
			case FLOOR:
				backup.computeSegmentsIfAbsent(MazePart.FLOOR, floor -> FloorGen.genFloor(mazeMap));
				break;
			case ROOF:
				backup.computeSegmentsIfAbsent(MazePart.ROOF, roof -> RoofGen.genRoof(mazeMap, settings));
				break;
		}
	}
	
	public void unbuildMaze(Clip maze, MazePart mazePart) throws TextException {
		if (!sessionHandler.hasBackup(maze)) {
			throw new TextException(Message.INFO_MAZE_NOT_BUILT);
		}
		MazeBackup backup = sessionHandler.getBackup(maze);
		
		if (mazePart == MazePart.WALLS) {
			for (MazePart builtPart : backup.getBuiltParts()) {
				unbuildMazePart(backup, builtPart);
			}
			sessionHandler.removeBackup(maze);
			return;
		}
		if (!backup.getBuiltParts().contains(mazePart)) {
			return;
		}
		unbuildMazePart(backup, mazePart);
		backup.removeMazePart(mazePart);
	}
	
	private void unbuildMazePart(MazeBackup backup, MazePart mazePart) {
		new BlockResetter(plugin, backup.getBlocks(mazePart), ConfigSettings.BLOCKS_PLACED_PER_TICK, callback -> {
			if (mazePart == MazePart.WALLS) {
				backup.getMaze().updateHeights();
				backup.getMaze().setActive(true);
			}
		}).runTaskTimer(plugin, 0, 1);
	}
}
