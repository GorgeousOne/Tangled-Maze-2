package me.gorgeousone.badapplemaze.generation.generator;

import me.gorgeousone.badapplemaze.generation.AreaType;
import me.gorgeousone.badapplemaze.generation.GridCell;
import me.gorgeousone.badapplemaze.generation.MazeMap;
import me.gorgeousone.badapplemaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public abstract class BlockGen {
	
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
