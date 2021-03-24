package me.gorgeousone.tangledmaze.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class Vec2 implements Comparable<Vec2> {
	
	private int x;
	private int z;
	
	public Vec2(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public Vec2(Block block) {
		this.x = block.getX();
		this.z = block.getZ();
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public Vec2 add(Vec2 other) {
		x += other.x;
		z += other.z;
		return this;
	}
	
	public Location toLocation(World world, int y) {
		return new Location(world, x, y, z);
	}
	
	@Override
	public int compareTo(Vec2 vec) {
		int deltaX = Double.compare(getX(), vec.getX());
		return deltaX != 0 ? deltaX : Double.compare(getZ(), vec.getZ());
	}
	
	@Override
	public Vec2 clone() {
		return new Vec2(x, z);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Vec2)) {
			return false;
		}
		Vec2 vec = (Vec2) other;
		return x == vec.x &&
		       z == vec.z;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, z);
	}
}
