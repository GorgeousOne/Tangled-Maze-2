package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class MazeSegment {
	
	private final Vec2 min;
	private final Vec2 max;
	private final Set<BlockVec> wallBlocks;
	
	public MazeSegment(Vec2 min, Vec2 max) {
		this.min = min;
		this.max = max;
		wallBlocks = new HashSet<>();
	}
	
	public Vec2 getMin() {
		return min;
	}
	
	public Vec2 getMax() {
		return max;
	}
	
	public Set<BlockVec> getBlocks() {
		return wallBlocks;
	}
	
	public void addBlock(BlockVec block) {
		wallBlocks.add(block);
	}
	
	public boolean contains(BlockVec block) {
		return contains(block.getX(), block.getZ());
	}
	
	public boolean contains(int x, int z) {
		return x >= min.getX() && x <= max.getX() &&
		       z >= min.getZ() && z <= max.getZ();
	}
}
