package me.gorgeousone.badapplemaze.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

/**
 * TODO run a performance test to see if it would be better to make this class immutable.
 * Talking about all the for loops with Direction#getVec2() and GridCell#getGridPos()
 * I guess this is surely not the bottleneck but it itches my brain
 */

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
		return add(other.x, other.z);
	}
	
	public Vec2 add(int dx, int dz) {
		x += dx;
		z += dz;
		return this;
	}
	
	public Vec2 sub(Vec2 other) {
		return sub(other.x, other.z);
	}

	public Vec2 sub(int dx, int dz) {
		x -= dx;
		z -= dz;
		return this;
	}

	public Location toLocation(World world, int y) {
		return new Location(world, x, y, z);
	}

	public Vec2 mult(int scalar) {
		x *= scalar;
		z *= scalar;
		return this;
	}

	public Vec2 floorDiv(int scalar) {
		x = Math.floorDiv(x, scalar);
		z = Math.floorDiv(z, scalar);
		return this;
	}

	public Vec2 floorMod(int scalar) {
		x = Math.floorMod(x, scalar);
		z = Math.floorMod(z, scalar);
		return this;
	}

	@Override
	public int compareTo(Vec2 vec) {
		int deltaX = Double.compare(getX(), vec.getX());
		return deltaX != 0 ? deltaX : Double.compare(getZ(), vec.getZ());
	}

	public double getMcAngle() {
		return Math.toDegrees(Math.atan2(-x, z));
	}

	public int sqrDist(Vec2 other) {
		int dx = x - other.x;
		int dz = z - other.z;
		return dx * dx + dz * dz;
	}

	public int manhattanDist(Vec2 other) {
		return Math.abs(x - other.x) + Math.abs(z - other.z);
	}

	@Override
	public Vec2 clone() {
		return new Vec2(x, z);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Vec2)) {
			return false;
		}
		Vec2 vec = (Vec2) o;
		return x == vec.x &&
		       z == vec.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, z);
	}

	@Override
	public String toString() {
		return "[" + "x=" + x + ", z=" + z + ']';
	}
}
