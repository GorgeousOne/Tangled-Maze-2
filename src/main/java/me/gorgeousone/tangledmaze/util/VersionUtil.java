package me.gorgeousone.tangledmaze.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class VersionUtil {
	
	public static Version PLUGIN_VERSION;
	public static Version SERVER_VERSION;
	public static boolean IS_LEGACY_SERVER;
	
	private VersionUtil() {}

	public static void setup(String tangledMazeVersion) {
		PLUGIN_VERSION = new Version(tangledMazeVersion);
		SERVER_VERSION = new Version(getServerVersionString());
		IS_LEGACY_SERVER = SERVER_VERSION.isBelow(new Version("1.13.0"));
	}

	public static String getServerVersionString() {
		return Bukkit.getBukkitVersion().split("-")[0];
	}
}
