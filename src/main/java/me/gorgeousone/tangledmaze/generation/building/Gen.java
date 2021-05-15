package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public abstract class Gen {
	
	protected static Set<Vec2> getColumns(GridCell cell, MazeMap mazeMap, AreaType areaType) {
		Vec2 min = cell.getMin();
		Vec2 max = cell.getMax();
		Set<Vec2> columns = new HashSet<>();
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				AreaType type = mazeMap.getType(x, z);
				
				if (areaType == null ^ type == areaType) {
					columns.add(new Vec2(x, z));
				}
			}
		}
		return columns;
	}
}
