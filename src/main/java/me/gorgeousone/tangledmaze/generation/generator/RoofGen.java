package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.BlockSegment;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class RoofGen extends Gen {
	
	public static Set<BlockSegment> genRoof(MazeMap mazeMap, int wallHeight) {
		GridMap gridMap = mazeMap.getPathMap();
		Set<BlockSegment> roofSegments = new HashSet<>();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				GridCell cell = gridMap.getCell(gridX, gridZ);
				BlockSegment roofTile = createRoofTile(mazeMap, gridMap, cell, wallHeight);
				
				if (roofTile != null) {
					roofSegments.add(roofTile);
				}
			}
		}
		return roofSegments;
	}
	
	private static BlockSegment createRoofTile(MazeMap mazeMap, GridMap gridMap, GridCell cell, int wallHeight) {
		Set<Vec2> columns = getColumns(cell, mazeMap, null);
		
		if (columns.isEmpty()) {
			return null;
		}
		BlockSegment roof = new BlockSegment(cell.getMin(), cell.getMax(), mazeMap.getWorld().getMaxHeight());
		Vec2 gridPos = cell.getGridPos();
		int roofY = gridMap.getWallY(gridPos, wallHeight) + 1;
		
		for (Vec2 column : columns) {
			int maxNeighborRoofY = roofY;
			
			for (Direction wallFacing : roof.getWallFacings(column.getX(), column.getZ())) {
				Vec2 neighborCellPos = gridPos.clone().add(wallFacing.getVec2());
				int neighborRoofY = gridMap.getWallY(neighborCellPos, wallHeight) + 1;
				
				if (neighborRoofY == 0) {
					continue;
				}
				maxNeighborRoofY = Math.max(maxNeighborRoofY, neighborRoofY);
			}
			for (int y = roofY; y <= maxNeighborRoofY; ++y) {
				roof.addBlock(column.getX(), y, column.getZ());
			}
		}
		return roof;
	}
}
