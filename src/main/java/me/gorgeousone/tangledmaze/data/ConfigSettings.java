package me.gorgeousone.tangledmaze.data;

import me.gorgeousone.tangledmaze.util.MathUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigSettings {
	
	public static Material WAND_ITEM;
	public static boolean HOVER_CLICKING_ENABLED;
	public static int HOVER_RANGE;
	
	private final JavaPlugin plugin;
	
	public ConfigSettings(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void loadSettings(FileConfiguration config) {
		WAND_ITEM = deserializeMaterial(config, "wand-item");
		HOVER_CLICKING_ENABLED = config.getBoolean("hover-clicking.enabled");
		HOVER_RANGE = MathUtil.clamp(config.getInt("hover-clicking.range"), 1, 128);
	}
	
	/**
	 * Configures config defaults depending on server type (
	 */
	public void addVersionDefaults(FileConfiguration config, boolean isLegacyServer) {
		if (isLegacyServer) {
			config.addDefault("wand-item", "gold_spade");
		} else {
			config.addDefault("wand-item", "golden_shovel");
		}
	}
	
	private Material deserializeMaterial(FileConfiguration config, String configPath) {
		String configValue = config.getString(configPath);
		String defaultValue = config.getDefaults().getString(configPath);
		
		try {
			return Material.valueOf(configValue.toUpperCase());
		} catch (Exception e) {
			plugin.getLogger().warning("Could not load '" + configValue + "' Using '" + defaultValue + "' instead.");
			return Material.valueOf(defaultValue.toUpperCase());
		}
	}
}
