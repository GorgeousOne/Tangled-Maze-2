package me.gorgeousone.badapplemaze.generation;

import me.gorgeousone.badapplemaze.util.BlockVec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class to store locations of blocks forming a maze in a big 3D nested map.
 */
public class BlockCollection {
	
	private final Map<Integer, Map<Integer, Set<Integer>>> blocks;
	
	public BlockCollection() {
		blocks = new HashMap<>();
	}
	
	public void addBlock(int x, int y, int z) {
		Map<Integer, Set<Integer>> zCoords = blocks.computeIfAbsent(x, map -> new HashMap<>());
		zCoords.computeIfAbsent(z, set -> new HashSet<>()).add(y);
	}
	
	public boolean isFilled(int x, int y, int z) {
		if (blocks.containsKey(x)) {
			Map<Integer, Set<Integer>> zCoords = blocks.get(x);
			if (zCoords.containsKey(z)) {
				return zCoords.get(z).contains(y);
			}
		}
		return false;
	}

	public Set<BlockVec> listBlocks() {
		Set<BlockVec> blockVecs = new HashSet<>();
		
		for (Map.Entry<Integer, Map<Integer, Set<Integer>>> xCoord : blocks.entrySet()) {
			int x = xCoord.getKey();
			
			for (Map.Entry<Integer, Set<Integer>> zCoord : xCoord.getValue().entrySet()) {
				int z = zCoord.getKey();
				
				for (int y : zCoord.getValue()) {
					blockVecs.add(new BlockVec(x, y, z));
				}
			}
		}
		return blockVecs;
	}
}
