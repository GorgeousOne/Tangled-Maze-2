package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

/**
 * A class for creating the dimensions of a maze exit. Instead of a min/max corner for a rectangle the exit
 * is represented by 2 squares lying on each other. The squares persist of start/end loc as min corner and path width
 * as side length. The end square can be shifted towards faced direction.
 * The area covered by and inbetween the squares is the segment.
 */
public class ExitSegment {
	
	private final Vec2 startLoc;
	private final Vec2 endLoc;
	private final Direction facing;
	private final int width;
	
	public ExitSegment(Vec2 startLoc, Direction facing, int pathWidth) {
		this.startLoc = startLoc.clone();
		this.endLoc = startLoc.clone();
		this.facing = facing;
		this.width = pathWidth;
	}
	
	public Vec2 getMin() {
		return facing.isPositive() ? startLoc.clone() : endLoc.clone();
	}
	
	public Vec2 getMax() {
		Vec2 maximum = facing.isPositive() ? endLoc.clone() : startLoc.clone();
		return maximum.add(width, width);
	}
	
	/**
	 * Returns the min location of the start square
	 */
	public Vec2 getStart() {
		return startLoc.clone();
	}
	
	/**
	 * Returns the min location of the end square
	 */
	public Vec2 getEnd() {
		return endLoc.clone();
	}
	
	/**
	 * Moves the end square of the exit towards the faced direction by dLength blocks.
	 */
	public void extend(int dLength) {
		if (length() + dLength < 1) {
			throw new IllegalArgumentException("Exit " + startLoc + " cannot be shorter than 1 block.");
		}
		endLoc.add(facing.getVec2().mult(dLength));
	}
	
	public int width() {
		return width;
	}
	
	public int length() {
		return Math.abs(endLoc.getX() - startLoc.getX()) +
		       Math.abs(endLoc.getZ() - startLoc.getZ()) + width;
	}
}