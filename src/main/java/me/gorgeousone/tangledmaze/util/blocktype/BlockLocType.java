package me.gorgeousone.tangledmaze.util.blocktype;

import org.bukkit.Location;

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
