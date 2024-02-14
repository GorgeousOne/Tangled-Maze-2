package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WallBlockGen extends BlockGen {
	
	public static BlockCollection genWalls(MazeMap mazeMap) {
		GridMap gridMap = mazeMap.getPathMap();
		BlockCollection walls = new BlockCollection();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				
				if (gridMap.getPathType(gridX, gridZ) != PathType.PAVED) {
					GridCell cell = gridMap.getCell(gridX, gridZ);
					addWallSegment(walls, mazeMap, gridMap, cell);
				}
			}
		}
		for (BlockVec block : hollowOutWall(mazeMap, gridMap)) {
			walls.removeBlock(block);
		}
		return walls;
	}
	
	/**
	 * Creates a piece of wall on the given grid segment. The height of each wall column is the same,
	 * dependent on the highest piece of path surrounding all columns together
	 * @param mazeMap for looking for wall coordinates
	 * @param cell    segment to build the wall in
	 */
	private static void addWallSegment(BlockCollection walls, MazeMap mazeMap, GridMap gridMap, GridCell cell) {
		Set<Vec2> columns = getColumns(cell, mazeMap, AreaType.WALL);
		
		if (columns.isEmpty()) {
			return;
		}
		Vec2 gridPos = cell.getGridPos();
		int maxFloorY = gridMap.getFloorY(gridPos);
		
		for (Direction facing : Direction.fourCardinals()) {
			Vec2 neighborCell = gridPos.clone().add(facing.getVec2());
			
			if (gridMap.contains(neighborCell)) {
				maxFloorY = Math.max(maxFloorY, gridMap.getFloorY(neighborCell));
			}
		}
		for (Vec2 column : columns) {
			for (int y = mazeMap.getY(column) + 1; y <= gridMap.getWallY(gridPos); ++y) {
				walls.addBlock(column.getX(), y, column.getZ());
			}
		}
	}

	private static Set<BlockVec> hollowOutWall(MazeMap mazeMap, GridMap gridMap) {
		Set<BlockVec> blocksToRemove = new HashSet<>();

		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getWidth(); ++gridZ) {
				if (gridMap.getPathType(gridX, gridZ) == PathType.PAVED) {
					continue;
				}
				Map.Entry<Vec2, Vec2> surfaceMinMax = getWallCoreMinMax(gridMap.getCell(gridX, gridZ), gridMap);
				Vec2 coreMin = surfaceMinMax.getKey();
				Vec2 coreMax = surfaceMinMax.getValue();

				for (int x = coreMin.getX(); x < coreMax.getX(); ++x) {
					for (int z = coreMin.getZ(); z < coreMax.getZ(); ++z) {
						if (!isSurrounded(x, z, mazeMap)) {
							continue;
						}
						int minY = getMinHollowY(x, z, mazeMap);
						int maxY = getMaxHollowY(x, z, new Vec2(gridX, gridZ), gridMap);

						for (int y = minY; y < maxY; ++y) {
							blocksToRemove.add(new BlockVec(x, y, z));
						}
					}
				}
			}
		}
		return blocksToRemove;
	}

	private static Map.Entry<Vec2, Vec2> getWallCoreMinMax(GridCell cell, GridMap gridMap) {
		Vec2 gridPos = cell.getGridPos();
		Vec2 min = cell.getMin();
		Vec2 max = cell.getMax();

		for (Direction facing : Direction.fourCardinals()) {
			Vec2 facingVec = facing.getVec2();
			Vec2 neighborPos = gridPos.clone().add(facingVec);

			if (gridMap.contains(neighborPos) && gridMap.getPathType(neighborPos) != PathType.PAVED) {
				continue;
			}
			if (facing.isPositive()) {
				max.sub(facingVec);
			} else {
				min.sub(facingVec);
			}
		}
		return new AbstractMap.SimpleEntry<>(min, max);
	}

	private static boolean isSurrounded(int x, int z, MazeMap mazeMap) {
		Vec2 point = new Vec2(x, z);

		if (!mazeMap.contains(point)) {
			return false;
		}
		for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
			if (!mazeMap.contains(neighbor) || mazeMap.getType(neighbor) != AreaType.WALL) {
				return false;
			}
		}
		return true;
	}

	private static int getMinHollowY(int x, int z, MazeMap mazeMap) {
		int minHollowY = mazeMap.getY(x, z) + 2;

		for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
			if (mazeMap.contains(neighbor)) {
				minHollowY = Math.max(minHollowY, mazeMap.getY(neighbor) + 1);
			}
		}
		return minHollowY;
	}

	private static int getMaxHollowY(int x, int z, Vec2 gridPos, GridMap gridMap) {
//		Vec2 gridPos = gridMap.getGridPos(new Vec2(x, z));
		GridCell cell = gridMap.getCell(gridPos);
		int maxHollowY = gridMap.getWallY(gridPos);
		System.out.println(gridPos + ", " + gridMap.getGridPos(new Vec2(x, z)));
		for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
			if (cell.contains(neighbor)) {
				continue;
			}
			Vec2 neighborGridPos = gridMap.getGridPos(neighbor);
			if (gridMap.contains(neighborGridPos)) {
				maxHollowY = Math.max(maxHollowY, gridMap.getWallY(neighborGridPos) - 1);
			}
		}
		return maxHollowY;
	}

}
