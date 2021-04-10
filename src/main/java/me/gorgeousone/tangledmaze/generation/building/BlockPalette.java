package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.Material;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A class for storing block types with a count. It allows picking blocks at random with differently weighted chances.
 */
public class BlockPalette {
	
	private final LinkedList<Map.Entry<BlockType, Integer>> blocks;
	private int size;
	
	public BlockPalette() {
		blocks = new LinkedList<>();
	}
	
	public static BlockPalette getDefault() {
		BlockPalette defaultPalette = new BlockPalette();
		defaultPalette.addBlock(BlockType.get(Material.STONE), 1);
		return defaultPalette;
	}
	
	public int size() {
		return size;
	}
	
	public void addBlock(BlockType data, int count) {
		if (blocks.add(new AbstractMap.SimpleEntry<>(data, count))) {
			size += count;
		}
	}
	
	/**
	 * Returns the block type at the given index.
	 *
	 * @param index between 0 and size-1 of palette
	 */
	public BlockType getBlock(int index) {
		int iter = -1;
		
		for (Map.Entry<BlockType, Integer> entry : blocks) {
			iter += entry.getValue();
			
			if (iter >= index) {
				return entry.getKey();
			}
		}
		return blocks.getLast().getKey();
	}
	
	public LinkedList<Map.Entry<BlockType, Integer>> getAllBlocks() {
		return blocks;
	}
}
