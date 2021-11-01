package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.event.MazeBuildEvent;
import me.gorgeousone.tangledmaze.generation.BlockSegment;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.generator.FloorGen;
import me.gorgeousone.tangledmaze.generation.generator.RoofGen;
import me.gorgeousone.tangledmaze.generation.generator.WallGen;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
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
		sessionHandler.backupMaze(maze);
		MazeBackup backup = sessionHandler.getBackup(maze);
		backup.createMazeMapIfAbsent(settings);
		MazeMap mazeMap = backup.getMazeMap();
		
		createBlockSegments(backup, mazePart, mazeMap, settings);
		Set<BlockSegment> segments = backup.getSegments(mazePart);
		
		new BlockPlacer(mazeMap.getWorld(), collectBlocks(segments), settings.getPalette(mazePart), ConfigSettings.BLOCKS_PLACED_PER_TICK, backupBlocks -> {
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
				backup.computeSegmentsIfAbsent(MazePart.WALLS, walls -> WallGen.genWalls(mazeMap, settings));
				break;
			case FLOOR:
				backup.computeSegmentsIfAbsent(MazePart.FLOOR, floor -> FloorGen.genFloor(mazeMap));
				break;
			case ROOF:
				backup.computeSegmentsIfAbsent(MazePart.ROOF, roof -> RoofGen.genRoof(mazeMap, settings));
				break;
		}
	}
	
	private Set<BlockVec> collectBlocks(Set<BlockSegment> segments) {
		Set<BlockVec> blocks = new HashSet<>();
		
		for (BlockSegment segment : segments) {
			blocks.addAll(segment.getBlocks());
		}
		return blocks;
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
	
	//	private void displayPaths(Clip maze, MazeMap mazeMap) {
	//		for (PathTree tree : mazeMap.getPathTrees()) {
	//			float maxDist = tree.getMaxExitDist();
	//
	//			for (GridCell cell : tree.getCells()) {
	//				Color color = rainbowColor(tree.getExitDist(cell) / maxDist);
	//				Vec2 segMin = cell.getMin();
	//				Vec2 segMax = cell.getMax();
	//
	//				for (int x = segMin.getX(); x < segMax.getX(); ++x) {
	//					for (int z = segMin.getZ(); z < segMax.getZ(); ++z) {
	//						Block block = maze.getWorld().getBlockAt(x, mazeMap.getY(x, z) + 1, z);
	//						spawnColor(block, color);
	//					}
	//				}
	//			}
	//		}
	//	}
	//
	//	private Color rainbowColor(float percent) {
	//		float r, g, b;
	//
	//		if (percent < 1 / 6f) {
	//			r = 255;
	//			g = 6 * percent * 255;
	//			b = 0;
	//		} else if (percent < 1 / 3f) {
	//			r = 255 - 6 * (percent - 1 / 6f) * 255;
	//			g = 255;
	//			b = 0;
	//		} else if (percent < 1 / 2f) {
	//			r = 0;
	//			g = 255;
	//			b = 6 * (percent - 1 / 3f) * 255;
	//		} else if (percent < 2 / 3f) {
	//			r = 0;
	//			g = 255 - 6 * (percent - 1 / 2f) * 255;
	//			b = 255;
	//		} else if (percent < 5 / 6f) {
	//			r = 6 * (percent - 2 / 3f) * 255;
	//			g = 0;
	//			b = 255;
	//		} else {
	//			r = 255;
	//			g = 0;
	//			b = 255 - 6 * (percent - 5 / 6f) * 255;
	//		}
	//		return Color.fromRGB(colorize(r), colorize(g), colorize(b));
	//	}
	//
	//	private int colorize(float f) {
	//		return (int) Math.max(0, Math.min(255, f));
	//	}
	//
	//	EulerAngle ninety = new EulerAngle(70, 0, 0);
	//
	//	private void spawnColor(Block block, Color color) {
	//		Location spawnLoc = block.getLocation().add(0.5, 0, 0.5);
	//		ArmorStand stand = block.getWorld().spawn(spawnLoc, ArmorStand.class);
	//		stand.setArms(true);
	//		stand.setBodyPose(ninety);
	//		stand.setLeftArmPose(ninety);
	//		stand.setRightArmPose(ninety);
	//		stand.setVisible(false);
	//
	//		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
	//		LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
	//		meta.setColor(color);
	//		chest.setItemMeta(meta);
	//		stand.setChestplate(chest);
	//	}
}
