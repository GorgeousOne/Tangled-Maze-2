package me.gorgeousone.tangledmaze.util;

import org.bukkit.block.BlockFace;

public enum Direction {
	
	EAST(new Vec2(1, 0), BlockFace.EAST),
	SOUTH_EAST(new Vec2(1, 1), BlockFace.SOUTH_EAST),
	SOUTH(new Vec2(0, 1), BlockFace.SOUTH),
	SOUTH_WEST(new Vec2(-1, 1), BlockFace.SOUTH_WEST),
	WEST(new Vec2(-1, 0), BlockFace.WEST),
	NORTH_WEST(new Vec2(-1, -1), BlockFace.NORTH_WEST),
	NORTH(new Vec2(0, -1), BlockFace.NORTH),
	NORTH_EAST(new Vec2(1, -1), BlockFace.NORTH_EAST);
	
	public static final Direction[] CARDINALS = {EAST, SOUTH, WEST, NORTH};
	public static final Direction[] DIAGONALS = {SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST};
	
	private final Vec2 dirVec;
	private final BlockFace face;

	Direction(Vec2 dirVec, BlockFace face) {
		this.dirVec = dirVec;
		this.face = face;
	}
	
	/**
	 * Returns true if the direction's vector is pointing towards positive (with it's x and z coordinate)
	 */
	public boolean isPositive() {
		return dirVec.getZ() >= 0 && dirVec.getX() >= 0;
	}
	
	/**
	 * Returns if the x coordinate of the direction's vector is not 0
	 */
	public boolean isCollinearX() {
		return dirVec.getZ() == 0;
	}
	
	public boolean isCollinearZ() {
		return dirVec.getX() == 0;
	}
	
	public Vec2 getVec2() {
		return dirVec.clone();
	}
	
	public Direction getOpposite() {
		return values()[(ordinal() + 4) % values().length];
	}
	
	public Direction getRight() {
		return values()[(ordinal() + 2) % values().length];
	}
	
	public Direction getLeft() {
		return values()[(ordinal() + 6) % values().length];
	}

	/**
	 * Returns the direction that is in the middle of this direction and the other direction.
	 * But it only works for directions that are exactly 90° apart.
	 */
	public Direction getStupidMiddle(Direction other) {
		if (other == null) {
			return this;
		}
		int diff = Math.floorMod(other.ordinal() - ordinal() + 4, 8) - 4;
		return values()[Math.floorMod(ordinal() + diff / 2, 8)];
	}

	public BlockFace getFace() {
		return face;
	}
}
