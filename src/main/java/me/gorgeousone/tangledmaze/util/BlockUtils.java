package me.gorgeousone.tangledmaze.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.LinkedList;
import java.util.List;

public class BlockUtils {
	
	public static int getSurfaceY(World world, Vec2 loc, int y) {
		return getSurface(world.getBlockAt(loc.getX(), y, loc.getZ())).getY();
	}
	
	public static Block getSurface(World world, int x, int y, int z) {
		return getSurface(world.getBlockAt(x, y, z));
	}
	
	public static Block getSurface(Block block) {
		block.getRelative(BlockFace.DOWN);
		if (block.getType().isSolid()) {
			Block surfaceBlock = block;
			Location nextBlockLoc = block.getLocation();
			
			while (nextBlockLoc.getY() < block.getWorld().getMaxHeight() - 1) {
				nextBlockLoc.add(0, 1, 0);
				Block nextBlock = nextBlockLoc.getBlock();
				
				if (nextBlock.getType().isSolid()) {
					surfaceBlock = nextBlock;
				} else {
					return surfaceBlock;
				}
			}
			return surfaceBlock;
		} else {
			Location nextBlockLoc = block.getLocation();
			
			while (nextBlockLoc.getY() > 1) {
				nextBlockLoc.add(0, -1, 0);
				Block nextBlock = nextBlockLoc.getBlock();
				
				if (nextBlock.getType().isSolid()) {
					return nextBlock;
				}
			}
			return block;
		}
	}
	
	public static List<Vec2> getNeighbors(int x, int z, int radius) {
		List<Vec2> neighbors = new LinkedList<>();
		
		for (int dx = -radius; dx <= radius; ++dx) {
			for (int dz = -radius; dz <= radius; ++dz) {
				if (dx != 0 && dz != 0) {
					neighbors.add(new Vec2(x + dx, z + dz));
				}
			}
		}
		return neighbors;
	}
}
