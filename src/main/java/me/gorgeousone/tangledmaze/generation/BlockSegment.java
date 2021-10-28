package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

/**
 * A class to store locations of blocks forming a maze in a big 3D boolean array.
 */
public class BlockSegment {
	
	private final Vec2 min;
	private final Vec2 max;
	
	private final int worldHeight;
	private int minY;
	private int maxY;
	private final boolean[][][] blocks;
	
	public BlockSegment(Vec2 min, Vec2 max, int worldHeight) {
		this.min = min;
		this.max = max;
		this.worldHeight = worldHeight;
		
		int sizeX = max.getX() - min.getX();
		int sizeZ = max.getZ() - min.getZ();
		blocks = new boolean[sizeX][worldHeight][sizeZ];
		minY = worldHeight;
		maxY = 0;
	}
	
	public int getWorldHeight() {
		return worldHeight;
	}
	
	public BlockVec getMin() {
		return new BlockVec(min.clone(), minY);
	}
	
	public BlockVec getMax() {
		return new BlockVec(max.clone(), maxY);
	}
	
	public void addBlock(BlockVec block) {
		addBlock(block.getX(), block.getY(), block.getZ());
	}
	
	public void addBlock(int x, int y, int z) {
		if (contains(x, z) && y >= 0 && y < worldHeight) {
			blocks[x - min.getX()][y][z - min.getZ()] = true;
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}
	}
	
	public void removeBlock(BlockVec block) {
		removeBlock(block.getX(), block.getY(), block.getZ());
	}
	
	public void removeBlock(int x, int y, int z) {
		blocks[x - min.getX()][y][z - min.getZ()] = false;
	}
	
	public boolean isFilled(int x, int y, int z) {
		if (contains(x, z)) {
			return blocks[x - min.getX()][y][z - min.getZ()];
		}
		return false;
	}
	
	public boolean contains(int x, int z) {
		return x >= min.getX() && x < max.getX() &&
		       z >= min.getZ() && z < max.getZ();
	}
	
	public Set<BlockVec> getBlocks() {
		Set<BlockVec> blockVecs = new HashSet<>();
		
		for (int x = 0; x < blocks.length; ++x) {
			for (int z = 0; z < blocks[0][0].length; ++z) {
				for (int y = minY; y <= maxY; ++y) {
					if (blocks[x][y][z]) {
						blockVecs.add(new BlockVec(x + min.getX(), y, z + min.getZ()));
					}
				}
			}
		}
		return blockVecs;
	}
	
	public Set<Direction> getWallFacings(int x, int z) {
		Set<Direction> facings = new HashSet<>();
		if (x == min.getX()) {
			facings.add(Direction.WEST);
			if (z == min.getZ()) {
				facings.add(Direction.NORTH_WEST);
			} else if (z == max.getZ() - 1) {
				facings.add(Direction.SOUTH_WEST);
			}
		} else if (x == max.getX() - 1) {
			facings.add(Direction.EAST);
			if (z == min.getZ()) {
				facings.add(Direction.NORTH_EAST);
			} else if (z == max.getZ() - 1) {
				facings.add(Direction.SOUTH_EAST);
			}
		}
		if (z == min.getZ()) {
			facings.add(Direction.NORTH);
		} else if (z == max.getZ() - 1) {
			facings.add(Direction.SOUTH);
		}
		return facings;
	}
}
