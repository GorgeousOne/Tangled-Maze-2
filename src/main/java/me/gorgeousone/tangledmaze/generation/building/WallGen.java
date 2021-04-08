package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.PathSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockUtils;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class WallGen {
	
	public static Set<WallSegment> genWalls(MazeMap mazeMap, MazeSettings settings) {
		PathMap pathMap = mazeMap.getPathMap();
		Set<WallSegment> walls = new HashSet<>();
		
		for (int gridX = 0; gridX < pathMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < pathMap.getHeight(); ++gridZ) {
				PathSegment path = pathMap.getSegment(gridX, gridZ);
				WallSegment wall = createWall(mazeMap, path, settings.getValue(MazeProperty.WALL_HEIGHT));
				
				if (wall != null) {
					walls.add(wall);
				}
			}
		}
		return walls;
	}
	
	private static WallSegment createWall(MazeMap mazeMap, PathSegment path, int height) {
		Vec2 min = path.getMin();
		Vec2 max = path.getMax();
		WallSegment wall = new WallSegment(path.getMin(), path.getMax(), path.getGridPos(), mazeMap.getWorld().getMaxHeight());
		
		Set<Vec2> columns = new HashSet<>();
		int maxFloorY = -1;
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (type == AreaType.WALL) {
					columns.add(new Vec2(x, z));
					maxFloorY = Math.max(maxFloorY, mazeMap.getY(x, z));
				}
				
				for (Vec2 neighbor : BlockUtils.getNeighbors(x, z, 2)) {
					if (mazeMap.getType(neighbor) == AreaType.PATH) {
						maxFloorY = Math.max(maxFloorY, mazeMap.getY(neighbor));
					}
				}
			}
		}
		
		if (columns.isEmpty()) {
			return null;
		}
		
		for (Vec2 column : columns) {
			int floorY = mazeMap.getY(column);
			
			for (int y = floorY + 1; y <= maxFloorY + height; ++y) {
				wall.addBlock(column.getX(), y, column.getZ());
			}
		}
		return wall;
	}
	
//	private static void addColumn(WallSegment wall, int x, int z, int height, MazeMap mazeMap) {
//		int floorY = mazeMap.getY(x, z);
//
//		for (int i = 1; i <= height; ++i) {
//			wall.addBlock(x, floorY + i, z);
//		}
//	}
	
//	private static void levelWall(WallSegment wall, MazeMap mazeMap, MazeSettings settings) {
//		Vec2 min = wall.getMin();
//		Vec2 max = wall.getMax();
//		int wallHeight = settings.getValue(MazeProperty.WALL_HEIGHT);
//
//		for (int x = min.getX(); x < max.getX(); ++x) {
//			for (int z = min.getZ(); z < max.getZ(); ++z) {
//				for (Vec2 neighbor : getNeighbors(x, z)) {
//
//				}
//			}
//		}
//	}
}
