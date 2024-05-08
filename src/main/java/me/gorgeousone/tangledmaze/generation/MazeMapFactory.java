package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathGen;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.generation.paving.RoomGen;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * A factory class to create the maze map for building a maze from a maze clip and settings.
 */
public class MazeMapFactory {
	
	/**
	 * Generates a maze map with
	 */
	public static MazeMap createMazeMapOf(Clip maze, MazeSettings settings, int worldMinY) {
		Map.Entry<Vec2, Vec2> clipBounds = calculateClipBounds(maze);
		MazeMap map = new MazeMap(maze.getWorld(), clipBounds.getKey(), clipBounds.getValue(), worldMinY);
		copyMazeOntoMazeMap(maze, map);
		
		TerrainEditor.levelOffSpikes(map);
		MazeMapFactory.createPaths(map, maze.getExits(), settings, BlockUtil.getWorldMinHeight(maze.getWorld()));
		map.flip();
		TerrainEditor.cleanWallEdges(map);
		return map;
	}
	
	/**
	 * Returns a pair of Vec2 marking the smallest and greatest x/z coordinates of clip
	 */
	private static Map.Entry<Vec2, Vec2> calculateClipBounds(Clip clip) {
		Vec2 min = null;
		Vec2 max = null;
		
		for (Vec2 loc : clip.getFill().keySet()) {
			if (min == null) {
				min = loc.clone();
				max = loc.clone();
				continue;
			}
			int x = loc.getX();
			int z = loc.getZ();
			
			if (x < min.getX()) {
				min.setX(x);
			} else if (x > max.getX()) {
				max.setX(x);
			}
			if (z < min.getZ()) {
				min.setZ(loc.getZ());
			} else if (z > max.getZ()) {
				max.setZ(z);
			}
		}
		return new AbstractMap.SimpleEntry<>(min, max);
	}
	
	/**
	 * Copies the shape of a maze clip onto a maze map
	 */
	private static void copyMazeOntoMazeMap(Clip maze, MazeMap map) {
		for (Vec2 loc : maze.getFill().keySet()) {
			map.setType(loc, AreaType.FREE);
			map.setY(loc, maze.getY(loc));
		}
		for (Vec2 loc : maze.getBorder()) {
			map.setType(loc, AreaType.WALL);
		}
	}
	
	/**
	 * Creates a grid map for the maze and generates the paths on it.
	 * Saves grid map and path trees in maze map.
	 */
	public static void createPaths(MazeMap mazeMap, List<Vec2> exits, MazeSettings settings, int worldMinY) {
		GridMap gridMap = new GridMap(
				mazeMap.getMin(),
				mazeMap.getMax(),
				settings.getValue(MazeProperty.PATH_WIDTH),
				settings.getValue(MazeProperty.WALL_WIDTH));
		
		for (int i = 0; i < exits.size(); i++) {
			Vec2 exitLoc = exits.get(i);
			
			if (i == 0) {
				gridMap.setEntrance(exitLoc, getExitFacing(exitLoc, mazeMap));
				copyMazeOntoGrid(mazeMap, gridMap, settings.getValue(MazeProperty.WALL_HEIGHT), worldMinY);
			} else {
				gridMap.setExit(exitLoc, getExitFacing(exitLoc, mazeMap));
			}
		}
		mazeMap.setGridMap(gridMap);
		RoomGen.genRooms(gridMap, settings);
		PathGen.genPaths(gridMap, settings.getValue(MazeProperty.CURLINESS));
		copyPathsOntoMazeMap(gridMap, mazeMap);
	}
	
	private static Direction getExitFacing(Vec2 exit, MazeMap mazeMap) {
		for (Direction dir : Direction.CARDINALS) {
			Vec2 neighbor = exit.clone().add(dir.getVec2());
			
			if (mazeMap.getType(neighbor) == AreaType.FREE) {
				return dir;
			}
		}
		throw new IllegalArgumentException("Exit " + exit + " does not touch the maze.");
	}
	
	private static void copyMazeOntoGrid(MazeMap mazeMap, GridMap gridMap, int wallHeight, int worldMinY) {
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				
				GridCell cell = gridMap.getCell(gridX, gridZ);
				int floorY = getCellFloorY(cell, mazeMap, worldMinY);
				gridMap.setFloorY(gridX, gridZ, floorY);
				
				if (!isCellFree(gridMap.getCell(gridX, gridZ), mazeMap)) {
					gridMap.setPathType(gridX, gridZ, PathType.BLOCKED);
				}
			}
		}
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				gridMap.setWallY(gridX, gridZ, calcWallY(gridMap, new Vec2(gridX, gridZ), wallHeight));
			}
		}
	}
	
	/**
	 * Returns max y coordinate of floor found in a grid cell
	 */
	private static int getCellFloorY(GridCell cell, MazeMap mazeMap, int worldMinY) {
		Vec2 cellMin = cell.getMin();
		Vec2 cellMax = cell.getMax();
		int maxY = worldMinY;
		
		for (int x = cellMin.getX(); x < cellMax.getX(); ++x) {
			for (int z = cellMin.getZ(); z < cellMax.getZ(); ++z) {
				if (mazeMap.contains(x, z)) {
					maxY = Math.max(maxY, mazeMap.getY(x, z));
				}
			}
		}
		return maxY;
	}
	
	/**
	 * Calculates wall height for a GridCell to be as high as walls of surrounding cells
	 */
	private static int calcWallY(GridMap gridMap, Vec2 gridPos, int wallHeight) {
		int maxFloorY = gridMap.getFloorY(gridPos) + wallHeight;
		
		for (Direction facing : Direction.CARDINALS) {
			Vec2 neighborCell = gridPos.clone().add(facing.getVec2());
			
			if (gridMap.contains(neighborCell)) {
				maxFloorY = Math.max(maxFloorY, gridMap.getFloorY(neighborCell) + 2);
			}
		}
		return maxFloorY;
	}
	
	private static void copyPathsOntoMazeMap(GridMap gridMap, MazeMap mazeMap) {
		for (ExitSegment exit : gridMap.getExits()) {
			mazeMap.setType(exit.getMin(), exit.getMax(), AreaType.PATH);
		}
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				PathType pathType = gridMap.getPathType(gridX, gridZ);

				if (pathType == PathType.PAVED || pathType == PathType.EXIT || pathType == PathType.ROOM) {
					GridCell cell = gridMap.getCell(gridX, gridZ);
					mazeMap.setType(cell.getMin(), cell.getMax(), AreaType.PATH);
				}
			}
		}
	}
	
	private static boolean isCellFree(GridCell cell, MazeMap mazeMap) {
		Vec2 cellMin = cell.getMin();
		Vec2 cellMax = cell.getMax();
		
		for (int x = cellMin.getX(); x < cellMax.getX(); ++x) {
			for (int z = cellMin.getZ(); z < cellMax.getZ(); ++z) {
				if (mazeMap.getType(x, z) != AreaType.FREE) {
					return false;
				}
			}
		}
		return true;
	}
}
