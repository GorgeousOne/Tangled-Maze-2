package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.GridSegment;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class FloorGen {
	
	public static Set<WallSegment> genFloor(MazeMap mazeMap) {
		PathMap pathMap = mazeMap.getPathMap();
		Set<WallSegment> paths = new HashSet<>();
		
		for (int gridX = 0; gridX < pathMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < pathMap.getHeight(); ++gridZ) {
				GridSegment segment = pathMap.getSegment(gridX, gridZ);
				WallSegment path = createPath(mazeMap, segment);
				
				if (path == null) {
					continue;
				}
				paths.add(path);
			}
		}
		return paths;
	}
	
	private static WallSegment createPath(MazeMap mazeMap, GridSegment segment) {
		Vec2 min = segment.getMin();
		Vec2 max = segment.getMax();
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
		WallSegment path = new WallSegment(segment.getMin(), segment.getMax(), segment.getGridPos(), mazeMap.getWorld().getMaxHeight());
		
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
