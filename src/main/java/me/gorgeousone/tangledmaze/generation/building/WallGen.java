package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.GridSegment;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class WallGen {
	
	public static Set<BlockSegment> genWalls(MazeMap mazeMap, MazeSettings settings) {
		PathMap pathMap = mazeMap.getPathMap();
		Set<BlockSegment> walls = new HashSet<>();
		
		for (int gridX = 0; gridX < pathMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < pathMap.getHeight(); ++gridZ) {
				GridSegment segment = pathMap.getSegment(gridX, gridZ);
				BlockSegment wall = createWall(mazeMap, segment, settings.getValue(MazeProperty.WALL_HEIGHT));
				
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
	
	/**
	 * Creates a piece of wall on the given grid segment. The height of each wall column is the same,
	 * dependent on the highest piece of path nearby all the columns
	 *
	 * @param mazeMap for looking for wall coordinates
	 * @param segment segment to build the wall in
	 * @param height  height of the wall
	 */
	private static BlockSegment createWall(MazeMap mazeMap, GridSegment segment, int height) {
		Vec2 min = segment.getMin();
		Vec2 max = segment.getMax();
		Set<Vec2> columns = new HashSet<>();
		int maxFloorY = -1;
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (type != AreaType.WALL) {
					continue;
				}
				columns.add(new Vec2(x, z));
				maxFloorY = Math.max(maxFloorY, mazeMap.getY(x, z));
				
				for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 2)) {
					if (mazeMap.getType(neighbor) == AreaType.PATH) {
						maxFloorY = Math.max(maxFloorY, mazeMap.getY(neighbor));
					}
				}
			}
		}
		if (columns.isEmpty()) {
			return null;
		}
		BlockSegment wall = new BlockSegment(segment.getMin(), segment.getMax(), segment.getGridPos(), mazeMap.getWorld().getMaxHeight());
		
		for (Vec2 column : columns) {
			int floorY = mazeMap.getY(column);
			
			for (int y = floorY + 1; y <= maxFloorY + height; ++y) {
				wall.addBlock(column.getX(), y, column.getZ());
			}
		}
		return wall;
	}
	
	private static void hollowOutWall(BlockSegment wall) {
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
					
					for (BlockVec block : BlockUtil.getNeighborBlocks(x, y, z)) {
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
