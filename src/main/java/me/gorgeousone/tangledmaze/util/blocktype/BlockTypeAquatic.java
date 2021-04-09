package me.gorgeousone.tangledmaze.util.blocktype;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

/**
 * Wrapper for the block data of blocks after the aquatic update (1.13)
 */
public class BlockTypeAquatic extends BlockType {
	
	private final BlockData blockData;
	private boolean isDirectional;
	private boolean osOrientable;
	
	public BlockTypeAquatic(Material material) {
		blockData = material.createBlockData();
	}
	
	public BlockTypeAquatic(Block block) {
		blockData = block.getBlockData().clone();
	}
	
	public BlockTypeAquatic(BlockState state) {
		blockData = state.getBlockData().clone();
	}
	
	public BlockTypeAquatic(BlockData data) {
		blockData = data.clone();
	}
	
	public BlockTypeAquatic(String serialized) {
		blockData = Bukkit.createBlockData(serialized);
	}
	
	@Override
	public Material getType() {
		return blockData.getMaterial();
	}
	
	@Override
	public BlockState updateBlock(Block block, boolean physics) {
		BlockState oldState = block.getState();
		BlockState newState = block.getState();
		newState.setBlockData(blockData);
		newState.update(true, physics);
		return oldState;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(blockData);
	}
	
	@Override
	public BlockTypeAquatic clone() {
		return new BlockTypeAquatic(blockData);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BlockTypeAquatic)) {
			return false;
		}
		BlockTypeAquatic blockType = (BlockTypeAquatic) o;
		return blockData.equals(blockType.blockData);
	}
	
	@Override
	public String toString() {
		return "AquaBlock{" + blockData.getAsString() + '}';
	}
}