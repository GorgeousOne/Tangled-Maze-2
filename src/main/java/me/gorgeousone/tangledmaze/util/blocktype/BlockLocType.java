package me.gorgeousone.tangledmaze.util.blocktype;

import org.bukkit.Location;

/**
 * A class to store the location and a version and its independent block type.
 * Like a BlockState, but... idk, better?
 */
public class BlockLocType {
	
	private final Location location;
	private final BlockType type;
	
	public BlockLocType(Location location, BlockType type) {
		this.location = location.clone();
		this.type = type.clone();
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	public BlockType getType() {
		return type.clone();
	}
	
	public BlockLocType updateBlock(boolean applyPhysics) {
		return new BlockLocType(location, type.updateBlock(location.getBlock(), applyPhysics));
	}
}
