package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.PathTree;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;

public class MazeSegment {
	
	private final Vec2 min;
	private final Vec2 size;
	private final Vec2 gridPos;
	
	private PathTree tree;
	private MazeSegment parent;
	
	public MazeSegment(Vec2 min, Vec2 size, Vec2 gridPos) {
		this.min = min;
		this.size = size;
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
		return min.clone().add(size);
	}
	
	public Vec2 getSize() {
		return size.clone();
	}
	
	public boolean contains(BlockVec block) {
		return contains(block.getX(), block.getZ());
	}
	
	public boolean contains(int x, int z) {
		return x >= min.getX() && x < min.getX() + size.getX() &&
		       z >= min.getZ() && z < min.getZ() + size.getZ();
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
	
	public MazeSegment getParent() {
		return parent;
	}
	
	public void setParent(MazeSegment parent) {
		this.parent = parent;
	}
}
