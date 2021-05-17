package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClipFactory {
	
	/**
	 * Computes all vertices for a clip tool based on the list of minimum vertices required to create the shape
	 *
	 * @param vertices
	 * @param shape
	 * @return
	 */
	public static List<Block> createVertices(List<Block> vertices, ClipShape shape) {
		switch (shape) {
			case RECTANGLE:
			case ELLIPSE:
				return createRectVertices(vertices);
			case TRIANGLE:
				return new ArrayList<>(vertices);
			default:
				return null;
		}
	}
	
	public static List<Block> relocateVertices(List<Block> vertices, ClipShape shape, int relocatedVertex) {
		switch (shape) {
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
				return new ArrayList<>(vertices);
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
	
	public static Clip createClip(UUID playerId, List<Block> vertices, ClipShape shape) {
		Clip clip = new Clip(playerId, vertices.get(0).getWorld());
		switch (shape) {
			case RECTANGLE:
				return createRectClip(clip, vertices.get(0), vertices.get(2),
				                      Math.max(vertices.get(0).getY(), vertices.get(2).getY()));
			case ELLIPSE:
				return createEllipseClip(clip, vertices.get(0), vertices.get(2),
				                         Math.max(vertices.get(0).getY(), vertices.get(2).getY()));
			case TRIANGLE:
			default:
				return null;
		}
	}
	
	public static Clip createRectClip(Clip clip, Block v0, Block v1, int height) {
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
	
	private static Clip createEllipseClip(Clip clip, Block v0, Block v1, int height) {
		int minX = Math.min(v0.getX(), v1.getX());
		int maxX = Math.max(v0.getX(), v1.getX());
		int minZ = Math.min(v0.getZ(), v1.getZ());
		int maxZ = Math.max(v0.getZ(), v1.getZ());
		
		double centerX = (minX + maxX + 1) / 2d;
		double centerZ = (minZ + maxZ + 1) / 2d;
		double radius = (maxX - minX) / 2d + 0.25;
		double radiusZ = (maxZ - minZ) / 2d + 0.25;
		double stretchZ = radius / radiusZ;
		
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				double centerDistX = x + 0.5 - centerX;
				double centerDistZ = z + 0.5 - centerZ;
				
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
}
