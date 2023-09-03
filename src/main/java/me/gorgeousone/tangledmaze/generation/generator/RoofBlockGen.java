package me.gorgeousone.tangledmaze.generation.generator;

import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.Set;

public class RoofBlockGen extends BlockGen {
	
	public static BlockCollection genRoof(MazeMap mazeMap, MazeSettings settings) {
		GridMap gridMap = mazeMap.getPathMap();
		BlockCollection roofSegments = new BlockCollection();
		
		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {
				GridCell cell = gridMap.getCell(gridX, gridZ);
				addRoofSegment(roofSegments, mazeMap, gridMap, cell, settings.getValue(MazeProperty.ROOF_WIDTH));
			}
		}
		return roofSegments;
	}
	
	private static void addRoofSegment(BlockCollection roof,
	                                   MazeMap mazeMap,
	                                   GridMap gridMap,
	                                   GridCell cell,
	                                   int roofWidth) {
		Set<Vec2> columns = getColumns(cell, mazeMap, null);
		
		if (columns.isEmpty()) {
			return;
		}
		Vec2 gridPos = cell.getGridPos();
		int roofY = gridMap.getWallY(gridPos) + 1;
		
		for (Vec2 column : columns) {
			int maxNeighborRoofY = roofY;
			
			for (Direction wallFacing : cell.getWallFacings(column.getX(), column.getZ())) {
				Vec2 neighborCellPos = gridPos.clone().add(wallFacing.getVec2());
				
				if (gridMap.contains(neighborCellPos)) {
					maxNeighborRoofY = Math.max(maxNeighborRoofY, gridMap.getWallY(neighborCellPos) + 1);
				}
			}
			for (int y = roofY; y < maxNeighborRoofY + roofWidth; ++y) {
				roof.addBlock(column.getX(), y, column.getZ());
			}
		}
	}
}
