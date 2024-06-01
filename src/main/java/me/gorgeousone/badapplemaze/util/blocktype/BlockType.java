package me.gorgeousone.badapplemaze.util.blocktype;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

/**
 * A wrapper class for block materials and their (block-) data to support copying blocks across legacy and aquatic Minecraft versions.
 */
public abstract class BlockType {
	
	private static Boolean isLegacyServer;
	
	public abstract Material getType();
	
	/**
	 * Updates the state of this block with the information of the BlockType.
	 * @param block
	 * @param applyPhysics
	 * @return the previous type of the block
	 */
	public abstract BlockType updateBlock(Block block, boolean applyPhysics);
	
	public abstract void sendBlockChange(Player player, Location location);
	
	public abstract BlockType clone();
	
	/**
	 * Sets whether BlockType.of() will create a LegacyBlockType or an AquaticBlockType
	 */
	public static void configureVersion(boolean isLegacyServer) {
		BlockType.isLegacyServer = isLegacyServer;
	}
	
	public static BlockType get(Block block) {
		return isLegacyServer ? new BlockTypeLegacy(block) : new BlockTypeAquatic(block);
	}
	
	public static BlockType get(Material material) {
		return isLegacyServer ? new BlockTypeLegacy(material) : new BlockTypeAquatic(material);
	}
	
	public static BlockType get(BlockState state) {
		return isLegacyServer ? new BlockTypeLegacy(state) : new BlockTypeAquatic(state);
	}

	public static BlockType get(String serialized) {
		return get(serialized, false);
	}

	public static BlockType get(String serialized, String legacySerialized) {
		return isLegacyServer ? new BlockTypeLegacy(legacySerialized) : new BlockTypeAquatic(serialized, false);
	}
	public static BlockType get(String serialized, boolean randomizeFacing) {
		return isLegacyServer ? new BlockTypeLegacy(serialized) : new BlockTypeAquatic(serialized, randomizeFacing);
	}
}
