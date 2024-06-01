package me.gorgeousone.badapplemaze.util;

import org.bukkit.Material;

import java.util.LinkedList;
import java.util.List;

public final class MaterialUtil {
	
	private static final List<String> BLOCK_NAMES = new LinkedList<>();
	
	private MaterialUtil() {}
	
	/**
	 * @return LinkedList of all blocks that mazes can be built with
	 */
	public static List<String> getBlockNames() {
		return BLOCK_NAMES;
	}
	
	public static boolean isSolidFloor(Material material) {
		return material.isSolid() && (material.isOccluding());
	}
	
	public static boolean canBeReplaced(Material material) {
		return !material.isSolid();
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
