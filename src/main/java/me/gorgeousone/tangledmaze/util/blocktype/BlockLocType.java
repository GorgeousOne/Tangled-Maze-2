package me.gorgeousone.tangledmaze.util.blocktype;

import org.bukkit.Location;

import java.util.Objects;

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
	
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BlockLocType)) {
			return false;
		}
		BlockLocType that = (BlockLocType) o;
		return Objects.equals(location.getWorld(), that.location.getWorld()) &&
		       location.getBlockX() == that.location.getBlockX() &&
		       location.getBlockY() == that.location.getBlockZ() &&
		       location.getBlockZ() == that.location.getBlockZ();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}
