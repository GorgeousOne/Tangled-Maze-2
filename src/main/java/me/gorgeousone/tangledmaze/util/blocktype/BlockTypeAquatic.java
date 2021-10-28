package me.gorgeousone.tangledmaze.util.blocktype;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import sun.rmi.runtime.Log;

import java.util.Objects;
import java.util.Random;

/**
 * Wrapper for the block data of blocks after the aquatic update (1.13)
 */
public class BlockTypeAquatic extends BlockType {
	
	private static final Random RANDOM = new Random();
	
	private final BlockData blockData;
	private boolean isFreelyDirectional;
	private boolean isFreelyOrientable;
	private String[] allowedFaces;
	private String[] allowedAxes;
	
	public BlockTypeAquatic(BlockTypeAquatic other) {
		blockData = other.blockData.clone();
		isFreelyDirectional = other.isFreelyDirectional;
		isFreelyOrientable = other.isFreelyOrientable;
		allowedFaces = other.allowedFaces.clone();
		allowedAxes = other.allowedAxes.clone();
	}
	
	public BlockTypeAquatic(BlockData data) {
		blockData = data.clone();
		allowedFaces = new String[0];
		allowedAxes = new String[0];
	}
	
	public BlockTypeAquatic(Material material) {
		this(material.createBlockData());
	}
	
	public BlockTypeAquatic(Block block) {
		this(block.getBlockData().clone());
	}
	
	public BlockTypeAquatic(BlockState state) {
		this(state.getBlockData().clone());
	}
	
	public BlockTypeAquatic(String serialized) {
		this(serialized, false);
	}
	
	public BlockTypeAquatic(String serialized, boolean randomizeFacing) {
		this(deserialize(serialized));
		
		if (!randomizeFacing) {
			return;
		}
		this.isFreelyDirectional = blockData instanceof Directional && !blockData.getAsString(true).contains("facing");
		this.isFreelyOrientable = blockData instanceof Orientable && !blockData.getAsString(true).contains("axis");
		
		if (isFreelyDirectional) {
			allowedFaces = ((Directional) blockData).getFaces().stream().map(face -> face.name().toLowerCase()).toArray(String[]::new);
		} else if (isFreelyOrientable) {
			allowedAxes = ((Orientable) blockData).getAxes().stream().map(face -> face.name().toLowerCase()).toArray(String[]::new);
		}
	}
	
	public static BlockData deserialize(String serialized) {
		BlockData data = Bukkit.createBlockData(serialized);
		
		if (serialized.contains("leaves") && !serialized.contains("persistent")) {
			data = data.merge(data.getMaterial().createBlockData("[persistent=true]"));
		}
		return data;
	}
	
	@Override
	public Material getType() {
		return blockData.getMaterial();
	}
	
	@Override
	public BlockType updateBlock(Block block, boolean applyPhysics) {
		BlockState oldState = block.getState();
		BlockState newState = block.getState();
		BlockData copy = blockData.clone();
		
		if (isFreelyOrientable) {
			copy = copy.merge(copy.getMaterial().createBlockData("[axis=" + allowedAxes[RANDOM.nextInt(allowedAxes.length)] + "]"));
		} else if (isFreelyDirectional) {
			copy = copy.merge(copy.getMaterial().createBlockData("[facing=" + allowedFaces[RANDOM.nextInt(allowedFaces.length)] + "]"));
		}
		newState.setBlockData(copy);
		newState.update(true, applyPhysics);
		return BlockType.get(oldState);
	}
	
	@Override
	public void sendBlockChange(Player player, Location location) {
		player.sendBlockChange(location, blockData);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(blockData);
	}
	
	@Override
	public BlockTypeAquatic clone() {
		return new BlockTypeAquatic(this);
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
		return blockData.getAsString(true);
	}
}