package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.BlockSegment;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class FloorGen {
	
	public static Set<BlockSegment> genFloor(MazeMap mazeMap) {
		GridMap gridMap = mazeMap.getPathMap();
		Set<BlockSegment> paths = new HashSet<>();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				GridCell cell = gridMap.getCell(gridX, gridZ);
				BlockSegment path = createFloorSegment(mazeMap, cell);
				
				if (path != null) {
					paths.add(path);
				}
			}
		}
		return paths;
	}
	
	private static BlockSegment createFloorSegment(MazeMap mazeMap, GridCell cell) {
		Vec2 min = cell.getMin();
		Vec2 max = cell.getMax();
		Set<Vec2> columns = new HashSet<>();
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (type != AreaType.PATH) {
					continue;
				}
				columns.add(new Vec2(x, z));
			}
		}
		if (columns.isEmpty()) {
			return null;
		}
		BlockSegment path = new BlockSegment(cell.getMin(), cell.getMax(), mazeMap.getWorld().getMaxHeight());
		
		for (Vec2 column : columns) {
			int maxFloorY = mazeMap.getY(column);
			int minFloorY = maxFloorY;
			
			for (Vec2 neighbor : BlockUtil.getNeighbors(column.getX(), column.getZ(), 1)) {
				if (mazeMap.getType(neighbor) == AreaType.PATH) {
					minFloorY = Math.min(minFloorY, mazeMap.getY(neighbor));
				}
			}
			for (int y = minFloorY; y <= maxFloorY; ++y) {
				path.addBlock(column.getX(), y, column.getZ());
			}
		}
		return path;
	}
}
