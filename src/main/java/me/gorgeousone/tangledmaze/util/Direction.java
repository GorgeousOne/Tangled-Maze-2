package me.gorgeousone.tangledmaze.util;

public enum Direction {
	
	EAST(new Vec2(1, 0)),
	SOUTH_EAST(new Vec2(1, 1)),
	SOUTH(new Vec2(0, 1)),
	SOUTH_WEST(new Vec2(-1, 1)),
	WEST(new Vec2(-1, 0)),
	NORTH_WEST(new Vec2(-1, -1)),
	NORTH(new Vec2(0, -1)),
	NORTH_EAST(new Vec2(1, -1));
	
	private final Vec2 facing;
	
	Direction(Vec2 facing) {
		this.facing = facing;
	}
	
	public static Direction[] fourCardinals() {
		return new Direction[]{EAST, WEST, SOUTH, NORTH};
	}
	
	/**
	 * Returns true if the direction's vector is pointing towards positive (with it's x and z coordinate)
	 */
	public boolean isPositive() {
		return facing.getZ() >= 0 && facing.getX() >= 0;
	}
	
	/**
	 * Returns if the x coordinate of the direction's vector is not 0
	 */
	public boolean isCollinearX() {
		return facing.getZ() == 0;
	}
	
	public boolean isCollinearZ() {
		return facing.getX() == 0;
	}
	
	public Vec2 getVec2() {
		return facing.clone();
	}
	
	public Direction getOpposite() {
		int index = java.util.Arrays.asList(values()).indexOf(this);
		return values()[(index + 4) % values().length];
	}
	
	public Direction getRight() {
		int index = java.util.Arrays.asList(values()).indexOf(this);
		return values()[(index + 2) % values().length];
	}
	
	public Direction getLeft() {
		int index = java.util.Arrays.asList(values()).indexOf(this);
		return values()[(index + 6) % values().length];
	}
}
