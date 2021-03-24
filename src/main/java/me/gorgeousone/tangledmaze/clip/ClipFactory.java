package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.util.BlockUtils;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClipFactory {
	
	public static ArrayList<Block> createVertices(ArrayList<Block> vertices, ClipShape shape) {
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
	
	public static ArrayList<Block> createRectVertices(ArrayList<Block> vertices) {
		Block v0 = vertices.get(0);
		Block v2 = vertices.get(1);
		
		int maxY = Math.max(v0.getY(), v2.getY());
		World world = v0.getWorld();
		
		Block v1 = BlockUtils.getSurface(world, v0.getX(), maxY, v2.getZ());
		Block v3 = BlockUtils.getSurface(world, v2.getX(), maxY, v0.getZ());
		return new ArrayList<>(Arrays.asList(v0, v1, v2, v3));
	}
	
	public static Clip createClip(List<Block> vertices, ClipShape shape) {
		
		switch (shape) {
			case RECTANGLE:
				return createRectClip(vertices.get(0), vertices.get(2),
				                      Math.max(vertices.get(0).getY(), vertices.get(2).getY()));
			case ELLIPSE:
				return null;
			case TRIANGLE:
				return null;
			default:
				return null;
		}
	}
	
	public static Clip createRectClip(Block v0, Block v1, int height) {
		int minX = Math.min(v0.getX(), v1.getX());
		int maxX = Math.max(v0.getX(), v1.getX());
		
		int minZ = Math.min(v0.getZ(), v1.getZ());
		int maxZ = Math.max(v0.getZ(), v1.getZ());
		World world = v0.getWorld();
		Clip clip = new Clip(world);
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				Block block = BlockUtils.getSurface(world, x, height, z);
				clip.add(block);
				if (x == minX || x == maxX || z == minZ || z == maxZ) {
					clip.addBorder(block);
				}
			}
		}
		return clip;
	}
}
