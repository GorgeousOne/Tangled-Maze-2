package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;

public class MazeSegment {
	
	private final Vec2 min;
	private final Vec2 size;
	private final Vec2 gridPos;
	
	public MazeSegment(Vec2 min, Vec2 size, Vec2 gridPos) {
		this.min = min;
		this.size = size;
		this.gridPos = gridPos;
	}
	
	public Vec2 getGridPos() {
		return gridPos.clone();
	}
	
//	public int gridX() {
//		return gridPos.getX();
//	}
//
//	public int gridZ() {
//		return gridPos.getZ();
//	}
	
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
}
