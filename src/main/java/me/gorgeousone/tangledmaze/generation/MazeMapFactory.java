package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class MazeMapFactory {
	
	public static MazeMap createMazeMapOf(Clip maze) {
		Map.Entry<Vec2, Vec2> clipBounds = calculateClipBounds(maze);
		MazeMap map = new MazeMap(clipBounds.getKey(), clipBounds.getValue());
		copyMazeOntoMap(maze, map);
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
	
	private static void copyMazeOntoMap(Clip maze, MazeMap map) {
		for (Vec2 loc : maze.getFill().keySet()) {
			map.setType(loc, AreaType.FREE);
			map.setY(loc, maze.getY(loc));
		}
		for (Vec2 loc : maze.getBorder())
			map.setType(loc, AreaType.WALL);
	}
	
	public static void createPaths(MazeMap mazeMap, List<Vec2> exits, Map<String, Integer> settings) {
		PathMap pathMap = new PathMap(mazeMap.getMin(), mazeMap.getMax(), 2, 4);
		
		for(int i = 0; i < exits.size(); i++) {
			Vec2 exitLoc = exits.get(i);
			
			if (i == 0) {
				pathMap.setEntrance(exitLoc, getExitFacing(exitLoc, mazeMap));
			}else {
				pathMap.setExit(exitLoc, getExitFacing(exitLoc, mazeMap));
			}
		}
		copyPathsOntoMap(pathMap, mazeMap);
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
	
	private static void copyPathsOntoMap(PathMap pathMap, MazeMap mazeMap) {
		
		for (ExitSegment exit : pathMap.getExits()) {
			mazeMap.setType(exit.getMin(), exit.getMax(), AreaType.EXIT);
		}
		
//		for (int gridX = 0; gridX < pathMap.getWidth(); gridX++) {
//			for (int gridZ = 0; gridZ < pathMap.getHeight(); gridZ++) {
//
//			}
//		}
	}
	
	private static void setSegmentTypes(PathMap pathMap, MazeMap mazeMap) {
		for (int gridX = 0; gridX < pathMap.getWidth(); gridX++) {
			for (int gridZ = 0; gridZ < pathMap.getHeight(); gridZ++) {
				PathType type;
				if (!isSegmentFree(pathMap.getSegment(gridX, gridZ), mazeMap) || (gridX % 2 != 0 && gridZ % 2 != 0)) {
					type = PathType.BLOCKED;
				} else {
					type = PathType.FREE;
				}
				pathMap.setSegmentType(gridX, gridZ, type);
			}
		}
	}

	private static boolean isSegmentFree(MazeSegment segment, MazeMap mazeMap) {
		Vec2 segMin = segment.getLoc();
		Vec2 segMax = segment.getLoc().add(segment.getSize());

		for (int x = segMin.getX(); x < segMax.getX(); x++) {
			for (int z = segMin.getZ(); z < segMax.getZ(); z++) {
				if (mazeMap.getType(z, z) != AreaType.FREE) {
					return false;
				}
			}
		}
		return true;
	}
	
}
