package me.gorgeousone.tangledmaze.util;

import org.bukkit.Bukkit;

public final class VersionUtil {

	public static final Version SERVER_VERSION = new Version(getServerVersionString(), "_");
	public static final boolean IS_LEGACY_SERVER = SERVER_VERSION.isLower(new Version("1.13.0", "\\."));
	
	private VersionUtil() {}
	
	public static String getServerVersionString() {
		return Bukkit.getServer().getClass().getName().split("\\.")[3].replaceAll("[a-zA-Z]", "");
	}
}
