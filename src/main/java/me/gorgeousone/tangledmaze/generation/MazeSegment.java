package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

public class MazeSegment {
	
	private final Vec2 loc;
	private final Vec2 size;
	
	public MazeSegment(Vec2 min, Vec2 size) {
		this.loc = min;
		this.size = size;
	}
	
	public Vec2 getLoc() {
		return loc.clone();
	}
	
	public Vec2 getSize() {
		return size.clone();
	}
	
	public boolean contains(BlockVec block) {
		return contains(block.getX(), block.getZ());
	}
	
	public boolean contains(int x, int z) {
		return x >= loc.getX() && x < loc.getX() + size.getX() &&
		       z >= loc.getZ() && z < loc.getZ() + size.getZ();
	}
	
	public void expandLength(Direction facing, int size) {
	
	}
}
