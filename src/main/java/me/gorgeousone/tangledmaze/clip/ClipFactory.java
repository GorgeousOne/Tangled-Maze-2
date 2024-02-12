package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A factory class for calculating the vertices for all different shapes of clips
 * and for creating the clips based on the vertices.
 */
public class ClipFactory {
	
	/**
	 * Computes all vertices for a clip tool based on the list of minimum vertices required to create the shape
	 */
	public static List<Block> createVertices(List<Block> vertices, ClipType type) {
		switch (type) {
			case RECTANGLE:
			case ELLIPSE:
				return createRectVertices(vertices);
			case TRIANGLE:
				return vertices;
			default:
				return null;
		}
	}
	
	public static List<Block> relocateVertices(List<Block> vertices, ClipType type, int relocatedVertex) {
		switch (type) {
			case RECTANGLE:
			case ELLIPSE:
				if (relocatedVertex == 0 || relocatedVertex == 2) {
					vertices.remove(3);
					vertices.remove(1);
				} else {
					vertices.remove(2);
					vertices.remove(0);
				}
				return createRectVertices(vertices);
			case TRIANGLE:
				return vertices;
			default:
				return null;
		}
	}
	
	public static List<Block> createRectVertices(List<Block> vertices) {
		Block v0 = vertices.get(0);
		Block v2 = vertices.get(1);
		
		int maxY = Math.max(v0.getY(), v2.getY());
		World world = v0.getWorld();
		
		Block v1 = BlockUtil.getSurface(world, v0.getX(), maxY, v2.getZ());
		Block v3 = BlockUtil.getSurface(world, v2.getX(), maxY, v0.getZ());
		return new ArrayList<>(Arrays.asList(v0, v1, v2, v3));
	}
	
	public static Clip createClip(UUID playerId, List<Block> vertices, ClipType type) {
		Clip clip = new Clip(playerId, vertices.get(0).getWorld());
		switch (type) {
			case RECTANGLE:
				return createRectClip(clip, vertices.get(0), vertices.get(2));
			case ELLIPSE:
				return createEllipseClip(clip, vertices.get(0), vertices.get(2));
			case TRIANGLE:
				return createTriangleClip(clip, vertices.get(0), vertices.get(1), vertices.get(2));
			default:
				return null;
		}
	}
	
	public static Clip createRectClip(Clip clip, Block v0, Block v1) {
		int height = Math.max(v0.getY(), v1.getY());
		int minX = Math.min(v0.getX(), v1.getX());
		int maxX = Math.max(v0.getX(), v1.getX());
		int minZ = Math.min(v0.getZ(), v1.getZ());
		int maxZ = Math.max(v0.getZ(), v1.getZ());
		
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				Block block = BlockUtil.getSurface(clip.getWorld(), x, height, z);
				clip.add(block);
				
				if (x == minX || x == maxX || z == minZ || z == maxZ) {
					clip.addBorder(block);
				}
			}
		}
		return clip;
	}
	
	private static Clip createEllipseClip(Clip clip, Block v0, Block v1) {
		int height = Math.max(v0.getY(), v1.getY());
		int minX = Math.min(v0.getX(), v1.getX());
		int maxX = Math.max(v0.getX(), v1.getX());
		int minZ = Math.min(v0.getZ(), v1.getZ());
		int maxZ = Math.max(v0.getZ(), v1.getZ());
		
		double centerX = (minX + maxX) / 2d;
		double centerZ = (minZ + maxZ) / 2d;
		double radius = (maxX - minX) / 2d + 0.25;
		double radiusZ = (maxZ - minZ) / 2d + 0.25;
		double stretchZ = radius / radiusZ;
		
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				double centerDistX = x - centerX;
				double centerDistZ = z - centerZ;
				
				if (!ellipseContains(centerDistX, centerDistZ, stretchZ, radius)) {
					continue;
				}
				Block block = BlockUtil.getSurface(clip.getWorld(), x, height, z);
				clip.add(block);
				
				if (ellipseBorderContains(centerDistX, centerDistZ, stretchZ, radius)) {
					clip.addBorder(block);
				}
			}
		}
		return clip;
	}
	
	private static boolean ellipseContains(double x, double z, double stretchZ, double radius) {
		double circleZ = z * stretchZ;
		return x * x + circleZ * circleZ <= radius * radius;
	}
	
	private static boolean ellipseBorderContains(double x, double z, double stretchZ, double radius) {
		for (Direction facing : Direction.values()) {
			Vec2 faceVec = facing.getVec2();
			
			if (!ellipseContains(x + faceVec.getX(), z + faceVec.getZ(), stretchZ, radius)) {
				return true;
			}
		}
		return false;
	}
	
	public static Clip createTriangleClip(Clip clip, Block b0, Block b1, Block b2) {
		int height = Math.max(b0.getY(), Math.min(b1.getY(), b2.getY()));
		int minX = Math.min(b0.getX(), Math.min(b1.getX(), b2.getX()));
		int minZ = Math.min(b0.getZ(), Math.min(b1.getZ(), b2.getZ()));
		int maxX = Math.max(b0.getX(), Math.max(b1.getX(), b2.getX()));
		int maxZ = Math.max(b0.getZ(), Math.max(b1.getZ(), b2.getZ()));
		
		Vec2 v0 = new Vec2(b0);
		Vec2 v1 = new Vec2(b1);
		Vec2 v2 = new Vec2(b2);
		
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				Vec2 loc = new Vec2(x, z);
				
				if (!triangleContains(loc, v0, v1, v2)) {
					continue;
				}
				Block block = BlockUtil.getSurface(clip.getWorld(), x, height, z);
				clip.add(block);
				addBorder(loc, v0, v1, v2, clip);
			}
		}
		return clip;
	}
	
	private static void addBorder(Vec2 loc, Vec2 v0, Vec2 v1, Vec2 v2, Clip clip) {
		boolean isBorder = false;
		boolean isAlone = true;
		
		for (Direction facing : Direction.values()) {
			boolean neighborIn = triangleContains(loc.clone().add(facing.getVec2()), v0, v1, v2);
			if (!neighborIn) {
				isBorder = true;
			} else if (facing.isCollinearX() || facing.isCollinearZ()) {
				isAlone = false;
			}
			if (isBorder && !isAlone) {
				break;
			}
		}
		if (isAlone) {
			clip.removeFill(Collections.singletonList(loc));
		} else if (isBorder) {
			clip.addBorder(loc);
		}
	}
	
	//some confusing barycentric stuff
	public static boolean triangleContains(Vec2 p, Vec2 v0, Vec2 v1, Vec2 v2) {
		float d1 = sign(p, v0, v1);
		float d2 = sign(p, v1, v2);
		float d3 = sign(p, v2, v0);
		boolean hasNegative = (d1 < 0) || (d2 < 0) || (d3 < 0);
		boolean hasPositive = (d1 > 0) || (d2 > 0) || (d3 > 0);
		return !(hasNegative && hasPositive);
	}
	
	private static float sign(Vec2 p0, Vec2 p1, Vec2 p2) {
		return (p0.getX() - p2.getX()) * (p1.getZ() - p2.getZ()) - (p1.getX() - p2.getX()) * (p0.getZ() - p2.getZ());
	}
	
}
