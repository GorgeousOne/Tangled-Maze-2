package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class WallSegment {
	
	private final Vec2 min;
	private final Vec2 max;
	private final Vec2 gridPos;
	
	private final int worldHeight;
	private int minY;
	private int maxY;
	private final boolean[][][] blocks;
	
	public WallSegment(Vec2 min, Vec2 max, Vec2 gridPos, int worldHeight) {
		this.min = min;
		this.max = max;
		this.gridPos = gridPos;
		this.worldHeight = worldHeight;
		
		int sizeX = max.getX() - min.getX();
		int sizeZ = max.getZ() - min.getZ();
		blocks = new boolean[sizeX][worldHeight][sizeZ];
		minY = worldHeight;
		maxY = 0;
	}
	
	public Vec2 getMin() {
		return min.clone();
	}
	
	public Vec2 getMax() {
		return max.clone();
	}
	
	public void addBlock(BlockVec block) {
		addBlock(block.getX(), block.getY(), block.getZ());
	}
	
	void addBlock(int x, int y, int z) {
		blocks[x - min.getX()][y][z - min.getZ()] = true;
		minY = Math.min(minY, y);
		maxY = Math.max(maxY, y);
	}
	
	public int getMinY(int x, int z) {
		int dx = x - min.getX();
		int dz = z - min.getZ();
		
		for (int y = 0; y < worldHeight; ++y) {
			if (blocks[dx][y][dz]) {
				return y;
			}
		}
		return -1;
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
}
