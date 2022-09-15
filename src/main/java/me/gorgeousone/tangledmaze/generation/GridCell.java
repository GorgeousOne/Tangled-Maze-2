package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.PathTree;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class GridCell {
	
	private final Vec2 min;
	private final Vec2 max;
	private final Vec2 gridPos;
	
	private transient PathTree tree;
	private transient GridCell parent;
	
	public GridCell(Vec2 min, Vec2 size, Vec2 gridPos) {
		this.min = min;
		this.max = min.clone().add(size);
		this.gridPos = gridPos;
	}
	
	public Vec2 getGridPos() {
		return gridPos.clone();
	}
	
	public int gridX() {
		return gridPos.getX();
	}
	
	public int gridZ() {
		return gridPos.getZ();
	}
	
	public Vec2 getMin() {
		return min.clone();
	}
	
	public Vec2 getMax() {
		return max.clone();
	}
	
	public boolean contains(BlockVec block) {
		return contains(block.getX(), block.getZ());
	}
	
	public boolean contains(int x, int z) {
		return x >= min.getX() && x < max.getX() &&
		       z >= min.getZ() && z < max.getZ();
	}
	
	public PathTree getTree() {
		return tree;
	}
	
	public void setTree(PathTree tree) {
		this.tree = tree;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public GridCell getParent() {
		return parent;
	}
	
	public void setParent(GridCell parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the
	 *
	 * @param x
	 * @param z
	 * @return
	 */
	public Set<Direction> getWallFacings(int x, int z) {
		Set<Direction> facings = new HashSet<>();
		
		if (x == min.getX()) {
			facings.add(Direction.WEST);
			
			if (z == min.getZ()) {
				facings.add(Direction.NORTH_WEST);
			}
			if (z == max.getZ() - 1) {
				facings.add(Direction.SOUTH_WEST);
			}
		}
		if (x == max.getX() - 1) {
			facings.add(Direction.EAST);
			
			if (z == min.getZ()) {
				facings.add(Direction.NORTH_EAST);
			}
			if (z == max.getZ() - 1) {
				facings.add(Direction.SOUTH_EAST);
			}
		}
		if (z == min.getZ()) {
			facings.add(Direction.NORTH);
		}
		if (z == max.getZ() - 1) {
			facings.add(Direction.SOUTH);
		}
		return facings;
	}
}
