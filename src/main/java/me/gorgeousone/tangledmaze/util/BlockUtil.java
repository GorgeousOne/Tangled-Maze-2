package me.gorgeousone.tangledmaze.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockUtil {
	
	public static final BlockFace[] DIRECT_FACES = {
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.NORTH,
			BlockFace.SOUTH,
			BlockFace.EAST,
			BlockFace.WEST
	};
	
	public static int getSurfaceY(World world, Vec2 loc, int y) {
		return getSurface(world.getBlockAt(loc.getX(), y, loc.getZ())).getY();
	}
	
	public static Block getSurface(World world, int x, int y, int z) {
		return getSurface(world.getBlockAt(x, y, z));
	}
	
	public static Block getSurface(Block block) {
		if (MaterialUtil.isSolidFloor(block.getType())) {
			Block surfaceBlock = block;
			Location nextBlockLoc = block.getLocation();
			
			while (nextBlockLoc.getY() < block.getWorld().getMaxHeight()) {
				nextBlockLoc.add(0, 1, 0);
				Block nextBlock = nextBlockLoc.getBlock();
				
				if (MaterialUtil.isSolidFloor(nextBlock.getType())) {
					surfaceBlock = nextBlock;
				} else {
					return surfaceBlock;
				}
			}
			return surfaceBlock;
		} else {
			Location nextBlockLoc = block.getLocation();
			
			while (nextBlockLoc.getY() > getWorldMinHeight(block.getWorld())) {
				nextBlockLoc.add(0, -1, 0);
				Block nextBlock = nextBlockLoc.getBlock();
				
				if (MaterialUtil.isSolidFloor(nextBlock.getType())) {
					return nextBlock;
				}
			}
			return block;
		}
	}
	
	public static Set<Vec2> getNeighbors(int x, int z, int radius) {
		Set<Vec2> neighbors = new HashSet<>();
		
		for (int dx = -radius; dx <= radius; ++dx) {
			for (int dz = -radius; dz <= radius; ++dz) {
				if (dx != 0 && dz != 0) {
					neighbors.add(new Vec2(x + dx, z + dz));
				}
			}
		}
		return neighbors;
	}
	
	public static Set<BlockVec> getNeighborBlocks(int x, int y, int z) {
		return new HashSet<>(Arrays.asList(
				new BlockVec(x - 1, y, z),
				new BlockVec(x + 1, y, z),
				new BlockVec(x, y - 1, z),
				new BlockVec(x, y + 1, z),
				new BlockVec(x, y, z - 1),
				new BlockVec(x, y, z + 1)
		));
	}
	
	public static int getWorldMinHeight(World world) {
		try {
			return world.getMinHeight();
		} catch (NoSuchMethodError legacyError) {
			return 0;
		}
	}
}
