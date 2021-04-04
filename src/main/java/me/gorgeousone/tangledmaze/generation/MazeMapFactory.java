package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathGen;
import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class MazeMapFactory {
	
	public static MazeMap createMazeMapOf(Clip maze) {
		Map.Entry<Vec2, Vec2> clipBounds = calculateClipBounds(maze);
		MazeMap map = new MazeMap(clipBounds.getKey(), clipBounds.getValue());
		copyMazeOntoMazeMap(maze, map);
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
	
	private static void copyMazeOntoMazeMap(Clip maze, MazeMap map) {
		for (Vec2 loc : maze.getFill().keySet()) {
			map.setType(loc, AreaType.FREE);
			map.setY(loc, maze.getY(loc));
		}
		for (Vec2 loc : maze.getBorder()) {
			map.setType(loc, AreaType.WALL);
		}
	}
	
	public static void createPaths(MazeMap mazeMap, List<Vec2> exits, MazeSettings settings) {
		PathMap pathMap = new PathMap(mazeMap.getMin(), mazeMap.getMax(), 1, 2);
		
		for (int i = 0; i < exits.size(); i++) {
			Vec2 exitLoc = exits.get(i);
			
			if (i == 0) {
				pathMap.setEntrance(exitLoc, getExitFacing(exitLoc, mazeMap));
				copyMazeOntoPathMap(mazeMap, pathMap);
			} else {
				pathMap.setExit(exitLoc, getExitFacing(exitLoc, mazeMap));
			}
		}
		PathGen.generatePaths(pathMap, 3);
		copyPathsOntoMazeMap(pathMap, mazeMap);
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
	
	private static void copyMazeOntoPathMap(MazeMap mazeMap, PathMap pathMap) {
		for (int gridX = 0; gridX < pathMap.getWidth(); gridX++) {
			for (int gridZ = 0; gridZ < pathMap.getHeight(); gridZ++) {
				if (!isSegmentFree(pathMap.getSegment(gridX, gridZ), mazeMap)) {
					pathMap.setSegmentType(gridX, gridZ, PathType.BLOCKED);
				}
			}
		}
	}
	
	private static void copyPathsOntoMazeMap(PathMap pathMap, MazeMap mazeMap) {
		for (ExitSegment exit : pathMap.getExits()) {
			mazeMap.setType(exit.getMin(), exit.getMax(), AreaType.EXIT);
		}
		for (int gridX = 0; gridX < pathMap.getWidth(); gridX++) {
			for (int gridZ = 0; gridZ < pathMap.getHeight(); gridZ++) {
				if (pathMap.getSegmentType(gridX, gridZ) == PathType.PAVED) {
					MazeSegment segment = pathMap.getSegment(gridX, gridZ);
					mazeMap.setType(segment.getMin(), segment.getMax(), AreaType.PATH);
				}
			}
		}
	}
	
	private static boolean isSegmentFree(MazeSegment segment, MazeMap mazeMap) {
		Vec2 segMin = segment.getMin();
		Vec2 segMax = segment.getMin().add(segment.getSize());
		
		for (int x = segMin.getX(); x < segMax.getX(); x++) {
			for (int z = segMin.getZ(); z < segMax.getZ(); z++) {
				if (mazeMap.getType(x, z) != AreaType.FREE) {
					return false;
				}
			}
		}
		return true;
	}
}
