package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.Map;

public class MazeMapFactory {
	
	public static MazeMap createTerrainMapOf(Clip maze) {
		Map.Entry<Vec2, Vec2> clipBounds = calculateClipBounds(maze);
		MazeMap map = new MazeMap(clipBounds.getKey(), clipBounds.getValue());
		copyMazeOntoMap(maze, map);
		return map;
	}
	
	public static Map.Entry<Vec2, Vec2> calculateClipBounds(Clip clip) {
		Vec2 min = null;
		Vec2 max = null;
		
		for (Vec2 point : clip.getFill().keySet()) {
			if (min == null) {
				min = point.clone();
				max = point.clone();
				continue;
			}
			int x = point.getX();
			int z = point.getZ();
			
			if (x < min.getX()) {
				min.setX(x);
			} else if (x > max.getX()) {
				max.setX(x);
			}
			if (z < min.getZ()) {
				min.setZ(point.getZ());
			} else if (z > max.getZ()) {
				max.setZ(z);
			}
		}
		return new AbstractMap.SimpleEntry<>(min, max);
	}
	
	private static void copyMazeOntoMap(Clip maze, MazeMap map) {
		for (Vec2 loc : maze.getFill().keySet()) {
			map.setType(loc, AreaType.UNDEFINED);
			map.setY(loc, maze.getY(loc));
		}
		for (Vec2 loc : maze.getBorder())
			map.setType(loc, AreaType.WALL);
	}
}
