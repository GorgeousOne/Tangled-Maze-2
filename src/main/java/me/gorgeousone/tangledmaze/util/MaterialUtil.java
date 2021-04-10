package me.gorgeousone.tangledmaze.util;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
