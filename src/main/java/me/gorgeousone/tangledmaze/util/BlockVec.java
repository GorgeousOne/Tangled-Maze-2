package me.gorgeousone.tangledmaze.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class BlockVec {
	
	private int x;
	private int y;
	private int z;
	
	public BlockVec(Vec2 loc, int y) {
		this(loc.getX(), y, loc.getZ());
	}
	
	public BlockVec(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public Location toLoc(World world) {
		return new Location(world, x, y, z);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BlockVec)) {
			return false;
		}
		BlockVec block = (BlockVec) o;
		return x == block.x &&
		       y == block.y &&
		       z == block.z;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
	
	@Override
	public BlockVec clone() {
		return new BlockVec(x, y, z);
	}
}
