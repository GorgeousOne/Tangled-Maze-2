package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.PathTree;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;

public class PathSegment {
	
	private final Vec2 min;
	private final Vec2 max;
	private final Vec2 gridPos;
	
	private PathTree tree;
	private PathSegment parent;
	
	public PathSegment(Vec2 min, Vec2 size, Vec2 gridPos) {
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
	
	public PathSegment getParent() {
		return parent;
	}
	
	public void setParent(PathSegment parent) {
		this.parent = parent;
	}
}
