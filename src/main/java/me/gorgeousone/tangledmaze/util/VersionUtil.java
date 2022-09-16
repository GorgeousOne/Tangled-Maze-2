package me.gorgeousone.tangledmaze.util;

import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class VersionUtil {
	
	public static final Version PLUGIN_VERSION = new Version(getProjectVersion());
	public static final Version SERVER_VERSION = new Version(getServerVersionString(), "_");
	public static final boolean IS_LEGACY_SERVER = SERVER_VERSION.isBelow(new Version("1.13.0"));
	
	private VersionUtil() {}
	
	public static String getServerVersionString() {
		return Bukkit.getServer().getClass().getName().split("\\.")[3].replaceAll("[a-zA-Z]", "");
	}
	
	private static String getProjectVersion() {
		
		try (InputStream inputStream = VersionUtil.class.getClassLoader().getResourceAsStream("project.properties")) {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties.getProperty("version");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
