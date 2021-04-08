package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.PathSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockUtils;
import me.gorgeousone.tangledmaze.util.BlockVec;
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
				
				if (wall == null) {
					continue;
				}
				walls.add(wall);
					
				if (settings.getValue(MazeProperty.WALL_WIDTH) > 2) {
					hollowOutWall(wall);
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
	
	private static void hollowOutWall(WallSegment wall) {
		BlockVec min = wall.getMin();
		BlockVec max = wall.getMax();
		Set<BlockVec> blocksToRemove = new HashSet<>();
		
		for (int x = min.getX() + 1; x < max.getX(); ++x) {
			for (int y = min.getY() + 1; y < max.getY(); ++y) {
				for (int z = min.getZ() + 1; z < max.getZ(); ++z) {
					
					if (!wall.isFilled(x, y, z)) {
						continue;
					}
					boolean isSurfaceBlock = false;
					
					for (BlockVec block : BlockUtils.getNeighborBlocks(x, y, z)) {
						if (!wall.isFilled(block.getX(), block.getY(), block.getZ())) {
							isSurfaceBlock = true;
							break;
						}
					}
					if (!isSurfaceBlock) {
						blocksToRemove.add(new BlockVec(x, y, z));
					}
				}
			}
		}
		for (BlockVec block : blocksToRemove) {
			wall.removeBlock(block);
		}
	}
}
