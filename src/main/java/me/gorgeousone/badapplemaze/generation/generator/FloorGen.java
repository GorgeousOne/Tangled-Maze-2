package me.gorgeousone.badapplemaze.generation.generator;

import me.gorgeousone.badapplemaze.generation.AreaType;
import me.gorgeousone.badapplemaze.generation.BlockCollection;
import me.gorgeousone.badapplemaze.generation.GridCell;
import me.gorgeousone.badapplemaze.generation.GridMap;
import me.gorgeousone.badapplemaze.generation.MazeMap;
import me.gorgeousone.badapplemaze.util.BlockUtil;
import me.gorgeousone.badapplemaze.util.Vec2;

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
				int maxFloorY = mazeMap.getY(x, z);
				int minFloorY = maxFloorY;
				
				for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
					if (mazeMap.contains(neighbor)) {
						minFloorY = Math.min(minFloorY, mazeMap.getY(neighbor));
					}
				}
				for (int y = minFloorY; y <= maxFloorY; ++y) {
					path.addBlock(x, y, z);
				}
			}
		}
	}
}
