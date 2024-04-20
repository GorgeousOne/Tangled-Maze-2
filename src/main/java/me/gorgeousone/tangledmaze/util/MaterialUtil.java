package me.gorgeousone.tangledmaze.util;

import me.gorgeousone.tangledmaze.data.Constants;
import org.bukkit.Material;

import java.util.LinkedList;
import java.util.List;

public final class MaterialUtil {
	
	private static final List<String> BLOCK_NAMES = new LinkedList<>();
	
	public static void load() {
		BLOCK_NAMES.clear();
		
		for (Material mat : Material.values()) {
			if (mat.isBlock() && (mat.isOccluding() || mat.name().endsWith("LEAVES"))) {
				BLOCK_NAMES.add(mat.name().toLowerCase());
			}
		}
		for (Material mat : Constants.TRANSPARENT_SOLIDS) {
			BLOCK_NAMES.add(mat.name().toLowerCase());
		}
	}
	
	private MaterialUtil() {}
	
	/**
	 * @return LinkedList of all blocks that mazes can be built with
	 */
	public static List<String> getBlockNames() {
		return BLOCK_NAMES;
	}
	
	public static boolean isSolidFloor(Material material) {
		return material.isSolid() && (material.isOccluding() || Constants.TRANSPARENT_SOLIDS.contains(material));
	}
	
	public static boolean canBeReplaced(Material material) {
		return !material.isSolid() || !Constants.NOT_REPLACEABLES.contains(material);
	}

	public static Material match(String... names) {
		Material mat;

		for (String name : names) {
			mat = Material.matchMaterial(name);

			if (mat != null) {
				return mat;
			}
		}
		return null;
	}
}
