package me.gorgeousone.tangledmaze.util;

import me.gorgeousone.tangledmaze.data.Constants;
import org.bukkit.Material;

import java.util.LinkedList;
import java.util.List;

public final class MaterialUtil {
	
	private static final List<String> BLOCK_NAMES = new LinkedList<>();
	
	static {
		for (Material mat : Material.values()) {
			if (mat.isBlock() && (mat.isOccluding() || mat.name().endsWith("LEAVES"))) {
				BLOCK_NAMES.add(mat.name().toLowerCase());
			}
		}
	}
	
	private MaterialUtil() {}
	
	public static List<String> getBlockNames() {
		return BLOCK_NAMES;
	}
	
	public static boolean isSolidFloor(Material material) {
		return material.isSolid() && (material.isOccluding() || Constants.TRANSPARENT_SOLIDS.contains(material));
	}
	
	public static boolean canBeReplaced(Material material) {
		return material.isSolid() && (material.isOccluding() || Constants.TRANSPARENT_NOT_REPLACEABLES.contains(material));
	}
}
