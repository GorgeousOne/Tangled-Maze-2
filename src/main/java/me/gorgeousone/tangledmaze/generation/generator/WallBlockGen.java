package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WallBlockGen extends BlockGen {
	
	public static BlockCollection genWalls(MazeMap mazeMap, MazeSettings settings, boolean areWallsHollow) {
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
		if (areWallsHollow &&
		    settings.getValue(MazeProperty.WALL_WIDTH) > 2 &&
		    settings.getValue(MazeProperty.WALL_HEIGHT) > 2) {
			for (BlockVec block : hollowOutWall(mazeMap, gridMap)) {
				walls.removeBlock(block);
			}
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

	/**
	 * Removes the inside of the walls to make them hollow
	 * @param mazeMap for looking for wall coordinates
	 * @param gridMap
	 * @return
	 */
	private static Set<BlockVec> hollowOutWall(MazeMap mazeMap, GridMap gridMap) {
		Set<BlockVec> blocksToRemove = new HashSet<>();

		// iterate over all grid cells
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				if (gridMap.getPathType(gridX, gridZ) == PathType.PAVED) {
					continue;
				}
				Map.Entry<Vec2, Vec2> surfaceMinMax = getWallCoreMinMax(gridMap.getCell(gridX, gridZ), gridMap);
				Vec2 coreMin = surfaceMinMax.getKey();
				Vec2 coreMax = surfaceMinMax.getValue();

				// iterate over all blocks in the
				for (int x = coreMin.getX(); x < coreMax.getX(); ++x) {
					for (int z = coreMin.getZ(); z < coreMax.getZ(); ++z) {
						if (!isSurrounded(x, z, mazeMap)) {
							continue;
						}
						int minY = getMinHollowY(x, z, mazeMap);
						int maxY = getMaxHollowY(x, z, gridMap);

						for (int y = minY; y < maxY; ++y) {
							blocksToRemove.add(new BlockVec(x, y, z));
						}
					}
				}
			}
		}
		return blocksToRemove;
	}

	/**
	 * Returns the minimum and maximum coordinates of the invisible wall core in the given grid cell,
	 * which is 1 block bigger in directions where the wall is surrounded by other walls
	 */
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

	/**
	 * Returns true if an xz position on a maze map is surrounded by walls in all 8 directions
	 */
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

	/**
	 * Returns the minimum y coordinate of the hollow wall in the given xz position
	 * in order to keep the floor covered (of neighboring floor blocks as well)
	 */
	private static int getMinHollowY(int x, int z, MazeMap mazeMap) {
		// make hollow outs start at minimum 1 block deep inside the wall
		int minHollowY = mazeMap.getY(x, z) + 2;

		// make sure direct neighbors floor blocks are covered as well
		for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
			if (mazeMap.contains(neighbor)) {
				minHollowY = Math.max(minHollowY, mazeMap.getY(neighbor) + 2);
			}
		}
		return minHollowY;
	}


	/**
	 * Returns the maximum y coordinate of the hollow wall in the given xz position
	 * in order to not tear holes in the wall if neighbor cell walls are lower
	 */
	private static int getMaxHollowY(int x, int z, GridMap gridMap) {
		Vec2 gridPos = gridMap.getGridPos(new Vec2(x, z));
		GridCell cell = gridMap.getCell(gridPos);
		int maxHollowY = gridMap.getWallY(gridPos);
		
		for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
			if (cell.contains(neighbor)) {
				continue;
			}
			Vec2 neighborGridPos = gridMap.getGridPos(neighbor);

			if (gridMap.contains(neighborGridPos)) {
				maxHollowY = Math.min(maxHollowY, gridMap.getWallY(neighborGridPos));
			}
		}
		return maxHollowY;
	}
}
