package me.gorgeousone.tangledmaze.util.blocktype;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

import java.util.Locale;
import java.util.Objects;

/**
 * A wrapper for material data used before the aquatic update (1.12 and before)
 */
@SuppressWarnings("deprecation")
public class BlockTypeLegacy extends BlockType {
	
	private final MaterialData materialData;
	
	public BlockTypeLegacy(Material material) {
		materialData = new MaterialData(material);
	}
	
	public BlockTypeLegacy(Block block) {
		materialData = block.getState().getData().clone();
	}
	
	public BlockTypeLegacy(BlockState state) {
		materialData = state.getData().clone();
	}
	
	public BlockTypeLegacy(MaterialData data) {
		materialData = data.clone();
	}
	
	public BlockTypeLegacy(String serialized) {
		Material material;
		byte data = 0;
		
		if (serialized.contains(":")) {
			String[] stringParts = serialized.split(":");
			material = Material.valueOf(stringParts[0].toUpperCase(Locale.ENGLISH));
			data = Byte.parseByte(stringParts[1]);
		} else {
			material = Material.valueOf(serialized.toUpperCase(Locale.ENGLISH));
		}
		materialData = new MaterialData(material, data);
	}
	
	@Override
	public Material getType() {
		return materialData.getItemType();
	}
	
	@Override
	public BlockState updateBlock(Block block, boolean physics) {
		BlockState oldState = block.getState();
		BlockState newState = block.getState();
		newState.setType(materialData.getItemType());
		newState.setRawData(materialData.getData());
		newState.update(true, physics);
		return oldState;
	}
	
	@Override
	public BlockTypeLegacy clone() {
		return new BlockTypeLegacy(materialData.clone());
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BlockTypeLegacy)) {
			return false;
		}
		BlockTypeLegacy blockType = (BlockTypeLegacy) o;
		return materialData.equals(blockType.materialData);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(materialData);
	}
	
	@Override
	public String toString() {
		return "LegacyBlock{" + materialData.toString() + '}';
	}
}