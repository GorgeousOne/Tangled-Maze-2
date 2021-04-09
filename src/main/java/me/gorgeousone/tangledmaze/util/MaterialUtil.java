package me.gorgeousone.tangledmaze.util;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.Set;

public final class MaterialUtil {
	
	private static final Set<String> BLOCK_NAMES = new HashSet<>();
	
	static {
		for (Material mat : Material.values()) {
			if (mat.isBlock()) {
				BLOCK_NAMES.add(mat.name().toLowerCase());
			}
		}
	}
	
	private MaterialUtil() {}
	
	public static Set<String> getBlockNames() {
		return BLOCK_NAMES;
	}
}
