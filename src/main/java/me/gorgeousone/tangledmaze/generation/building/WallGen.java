package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.PathSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.Direction;
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
		WallSegment wall = new WallSegment(path.getMin(), path.getMax(), path.getGridPos(), mazeMap.getWorld().getMaxHeight());
		Vec2 min = path.getMin();
		Vec2 max = path.getMin().add(path.getSize());
		boolean addedBlocks = false;
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (type == AreaType.WALL) {
					addColumn(wall, x, z, height, mazeMap);
					addedBlocks = true;
				}
			}
		}
		return addedBlocks ? wall : null;
	}
	
	private static void addColumn(WallSegment wall, int x, int z, int height, MazeMap mazeMap) {
		int floorY = mazeMap.getY(x, z);
		
		for (Vec2 neighbor : getNeighbors(x, z)) {
			if (mazeMap.getType(neighbor) == AreaType.PATH) {
				floorY = Math.min(floorY, mazeMap.getY(neighbor));
			}
		}
		for (int i = 1; i <= height; ++i) {
			wall.addBlock(x, floorY + i, z);
		}
	}
	
	private static Vec2[] getNeighbors(int x, int z) {
		Vec2[] neighbors = new Vec2[8];
		int i = 0;
		
		for (Direction dir : Direction.values()) {
			neighbors[i] = new Vec2(x, z).add(dir.getVec2());
			++i;
		}
		return neighbors;
	}
}
