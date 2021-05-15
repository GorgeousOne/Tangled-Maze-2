package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.building.TerrainEditor;
import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathGen;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class MazeMapFactory {
	
	public static MazeMap createMazeMapOf(Clip maze, MazeSettings settings) {
		Map.Entry<Vec2, Vec2> clipBounds = calculateClipBounds(maze);
		MazeMap map = new MazeMap(maze.getWorld(), clipBounds.getKey(), clipBounds.getValue());
		copyMazeOntoMazeMap(maze, map);
		
		MazeMapFactory.createPaths(map, maze.getExits(), settings);
		map.flip();
		TerrainEditor.improveTerrain(map);
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
	public static void createPaths(MazeMap mazeMap, List<Vec2> exits, MazeSettings settings) {
		GridMap gridMap = new GridMap(
				mazeMap.getMin(),
				mazeMap.getMax(),
				settings.getValue(MazeProperty.PATH_WIDTH),
				settings.getValue(MazeProperty.WALL_WIDTH));
		
		for (int i = 0; i < exits.size(); i++) {
			Vec2 exitLoc = exits.get(i);
			
			if (i == 0) {
				gridMap.setEntrance(exitLoc, getExitFacing(exitLoc, mazeMap));
				copyMazeOntoGrid(mazeMap, gridMap, settings.getValue(MazeProperty.WALL_HEIGHT));
			} else {
				gridMap.setExit(exitLoc, getExitFacing(exitLoc, mazeMap));
			}
		}
		mazeMap.setGridMap(gridMap);
		mazeMap.setPathTrees(PathGen.genPaths(gridMap, settings.getValue(MazeProperty.CURLINESS)));
		copyPathsOntoMazeMap(gridMap, mazeMap);
	}
	
	private static Direction getExitFacing(Vec2 exit, MazeMap mazeMap) {
		for (Direction dir : Direction.fourCardinals()) {
			Vec2 neighbor = exit.clone().add(dir.getVec2());
			
			if (mazeMap.getType(neighbor) == AreaType.FREE) {
				return dir;
			}
		}
		throw new IllegalArgumentException("Exit " + exit + " does not touch the maze.");
	}
	
	private static void copyMazeOntoGrid(MazeMap mazeMap, GridMap gridMap, int wallHeight) {
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				
				GridCell cell = gridMap.getCell(gridX, gridZ);
				int floorY = getCellFloorY(cell, mazeMap);
				
				gridMap.setFloorY(gridX, gridZ, floorY);
				gridMap.setWallY(gridX, gridZ, floorY + wallHeight);
				
				if (!isCellFree(gridMap.getCell(gridX, gridZ), mazeMap)) {
					gridMap.setPathType(gridX, gridZ, PathType.BLOCKED);
				}
			}
		}
	}
	
	private static void copyPathsOntoMazeMap(GridMap gridMap, MazeMap mazeMap) {
		for (ExitSegment exit : gridMap.getExits()) {
			mazeMap.setType(exit.getMin(), exit.getMax(), AreaType.PATH);
		}
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				
				if (gridMap.getPathType(gridX, gridZ) == PathType.PAVED) {
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
	
	private static int getCellFloorY(GridCell cell, MazeMap mazeMap) {
		Vec2 cellMin = cell.getMin();
		Vec2 cellMax = cell.getMax();
		int maxY = -1;
		
		for (int x = cellMin.getX(); x < cellMax.getX(); ++x) {
			for (int z = cellMin.getZ(); z < cellMax.getZ(); ++z) {
				maxY = Math.max(maxY, mazeMap.getY(x, z));
			}
		}
		return maxY;
	}
}
