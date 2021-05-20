package me.gorgeousone.tangledmaze.data;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.TreeSet;

public class Constants {
	
	public static final String
			BUILD_PERM = "tangledmaze.build",
			RELOAD_PERM = "tangledmaze.reload";
	
	public static final String prefix =
			ChatColor.DARK_GREEN + "[" +
			ChatColor.GREEN + "TM" +
			ChatColor.DARK_GREEN + "] " +
			ChatColor.YELLOW;
	
	public static final TreeSet<Material> TRANSPARENT_SOLIDS = new TreeSet<>();
	public static final TreeSet<Material> TRANSPARENT_NOT_REPLACEABLES = new TreeSet<>();
	
	public static void loadMaterials(FileConfiguration materialLists) {
		TRANSPARENT_SOLIDS.clear();
		TRANSPARENT_NOT_REPLACEABLES.clear();
		
		for (String materialName : (List<String>) materialLists.getList("transparent-solids")) {
			try {
				TRANSPARENT_SOLIDS.add(Material.valueOf(materialName));
			} catch (IllegalArgumentException ignored) {}
		}
		for (String materialName : (List<String>) materialLists.getList("transparent-not-replaceables")) {
			try {
				TRANSPARENT_NOT_REPLACEABLES.add(Material.valueOf(materialName));
			} catch (IllegalArgumentException ignored) {}
		}
	}
}
