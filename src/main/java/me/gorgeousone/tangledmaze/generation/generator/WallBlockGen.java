package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

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
		return walls;
	}
	
	/**
	 * Creates a piece of wall on the given grid segment. The height of each wall column is the same,
	 * dependent on the highest piece of path surrounding all columns together
	 *
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
	
	//	private static void hollowOutWall(BlockSegment wall) {
	//		BlockVec min = wall.getMin();
	//		BlockVec max = wall.getMax();
	//		Set<BlockVec> blocksToRemove = new HashSet<>();
	//
	//		for (int x = min.getX() + 1; x < max.getX(); ++x) {
	//			for (int y = min.getY() + 1; y < max.getY(); ++y) {
	//				for (int z = min.getZ() + 1; z < max.getZ(); ++z) {
	//
	//					if (!wall.isFilled(x, y, z)) {
	//						continue;
	//					}
	//					boolean isSurfaceBlock = false;
	//
	//					for (BlockVec block : BlockUtil.getNeighborBlocks(x, y, z)) {
	//						if (!wall.isFilled(block.getX(), block.getY(), block.getZ())) {
	//							isSurfaceBlock = true;
	//							break;
	//						}
	//					}
	//					if (!isSurfaceBlock) {
	//						blocksToRemove.add(new BlockVec(x, y, z));
	//					}
	//				}
	//			}
	//		}
	//		for (BlockVec block : blocksToRemove) {
	//			wall.removeBlock(block);
	//		}
	//	}
}
