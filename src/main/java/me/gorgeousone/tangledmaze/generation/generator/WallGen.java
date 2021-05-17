package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.BlockSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class WallGen extends Gen{
	
	public static Set<BlockSegment> genWalls(MazeMap mazeMap, MazeSettings settings) {
		GridMap gridMap = mazeMap.getPathMap();
		Set<BlockSegment> walls = new HashSet<>();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				
				if (gridMap.getPathType(gridX, gridZ) != PathType.PAVED) {
					GridCell cell = gridMap.getCell(gridX, gridZ);
					BlockSegment wall = createWall(mazeMap, gridMap, cell, settings.getValue(MazeProperty.WALL_HEIGHT));
					
					if (wall == null) {
						continue;
					}
					walls.add(wall);
					
					if (settings.getValue(MazeProperty.WALL_WIDTH) > 2) {
						hollowOutWall(wall);
					}
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
	 * @param cell segment to build the wall in
	 * @param height  height of the wall
	 */
	//gettin a bit of a noodle code here aint we?
	private static BlockSegment createWall(MazeMap mazeMap, GridMap gridMap, GridCell cell, int height) {
		Set<Vec2> columns = getColumns(cell, mazeMap, AreaType.WALL);
		
		if (columns.isEmpty()) {
			return null;
		}
		Vec2 gridPos = cell.getGridPos();
		int maxFloorY = gridMap.getFloorY(gridPos);
		
		for (Direction facing : Direction.fourCardinals()) {
			Vec2 neighborCell = gridPos.clone().add(facing.getVec2());
			maxFloorY = Math.max(maxFloorY, gridMap.getFloorY(neighborCell));
		}
		BlockSegment wall = new BlockSegment(cell.getMin(), cell.getMax(), cell.getGridPos(), mazeMap.getWorld().getMaxHeight());
		
		for (Vec2 column : columns) {
			int floorY = mazeMap.getY(column);
			
			for (int y = floorY + 1; y <= maxFloorY + height; ++y) {
				wall.addBlock(column.getX(), y, column.getZ());
			}
		}
		gridMap.setWallY(cell.gridX(), cell.gridZ(), maxFloorY + height);
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
