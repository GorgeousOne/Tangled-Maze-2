package me.gorgeousone.tangledmaze.util;

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
}
