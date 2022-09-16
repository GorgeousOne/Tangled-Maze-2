package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;

public class FloorGen {
	
	public static BlockCollection genFloor(MazeMap mazeMap) {
		GridMap gridMap = mazeMap.getPathMap();
		BlockCollection paths = new BlockCollection();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				GridCell cell = gridMap.getCell(gridX, gridZ);
				addFloorSegment(paths, mazeMap, cell);
			}
		}
		return paths;
	}
	
	private static void addFloorSegment(BlockCollection path, MazeMap mazeMap, GridCell cell) {
		Vec2 min = cell.getMin();
		Vec2 max = cell.getMax();
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (null == type) {
					continue;
				}
				Vec2 column = new Vec2(x, z);
				int maxFloorY = mazeMap.getY(column);
				int minFloorY = maxFloorY;
				
				for (Vec2 neighbor : BlockUtil.getNeighbors(column.getX(), column.getZ(), 1)) {
					if (mazeMap.contains(neighbor)) {
						minFloorY = Math.min(minFloorY, mazeMap.getY(neighbor));
					}
				}
				for (int y = minFloorY; y <= maxFloorY; ++y) {
					path.addBlock(column.getX(), y, column.getZ());
				}
			}
		}
	}
}
