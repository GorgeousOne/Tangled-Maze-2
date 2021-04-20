package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class RoofGen {
	
	public static Set<BlockSegment> genRoof(MazeMap mazeMap, int wallHeight) {
		GridMap gridMap = mazeMap.getPathMap();
		Set<BlockSegment> roof = new HashSet<>();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				GridCell cell = gridMap.getCell(gridX, gridZ);
				BlockSegment roofTile = createRoofTile(mazeMap, gridMap, cell, wallHeight);
				
				if (roofTile != null) {
					roof.add(roofTile);
				}
			}
		}
		return roof;
	}
	
	private static BlockSegment createRoofTile(MazeMap mazeMap, GridMap gridMap, GridCell cell, int wallHeight) {
		Vec2 min = cell.getMin();
		Vec2 max = cell.getMax();
		Set<Vec2> columns = new HashSet<>();
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (type != null) {
					columns.add(new Vec2(x, z));
				}
			}
		}
		if (columns.isEmpty()) {
			return null;
		}
		BlockSegment roof = new BlockSegment(cell.getMin(), cell.getMax(), cell.getGridPos(), mazeMap.getWorld().getMaxHeight());
		
//		int[] neighborRoof
//		Vec2 gridPos = cell.getGridPos();
//
//		for (Direction facing : Direction.fourCardinals()) {
//			Vec2 neighbor = gridPos.add(facing.getVec2());
//
//			if (gridMap.getPathType(neighbor) == PathType.BLOCKED) {
//				heightSum += gridMap.getWallY(neighbor.getX(), neighbor.getZ());
//
//			}
//		}
		
		Vec2 gridPos = cell.getGridPos();
		int roofY;
		
		if (gridMap.getPathType(gridPos) == PathType.PAVED) {
			roofY = gridMap.getFloorY(gridPos) + wallHeight + 1;
		}else {
			roofY = gridMap.getWallY(gridPos) + 1;
		}

		for (Vec2 column : columns) {
			roof.addBlock(column.getX(), roofY, column.getZ());
		}
		return roof;
	}
}
