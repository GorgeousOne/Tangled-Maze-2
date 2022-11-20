package me.gorgeousone.tangledmaze.updatecheck;

import me.gorgeousone.tangledmaze.util.VersionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateCheck {
	
	private final JavaPlugin plugin;
	private final int resourceId;
	private final String resourceName;
	private final String updateInfoPasteUrl;
	
	public UpdateCheck(JavaPlugin plugin, int resourceId, String resourceName, String updateInfoPasteUrl) {
		this.plugin = plugin;
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.updateInfoPasteUrl = updateInfoPasteUrl;
	}
	
	public void run() {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				UpdateInfo latestUpdate = getLatestUpdateInfo();
				
				if (null == latestUpdate) {
					plugin.getLogger().info("Plugin is up to date :)");
					return;
				}
				sendStaffInfo("A new version of TangledMaze is available!");
				sendStaffInfo(latestUpdate.getChatMessage());
			} catch (IOException exception) {
				plugin.getLogger().info("Unable to check for updates...");
			}
		});
	}
	
	private UpdateInfo getLatestUpdateInfo() throws IOException {
		InputStream inputStream = new URL(updateInfoPasteUrl).openStream();
		Scanner scanner = new Scanner(inputStream);
		UpdateInfo latestUpdate = new UpdateInfo(scanner.nextLine(), resourceName, resourceId);
		
		if (VersionUtil.PLUGIN_VERSION.isBelow(latestUpdate.getVersion())) {
			return latestUpdate;
		}
		return null;
	}
	
	private void sendStaffInfo(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isOp()) {
				player.sendMessage(message);
			}
		}
		Bukkit.getConsoleSender().sendMessage(message);
	}
	
	private void sendStaffInfo(BaseComponent[] message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isOp()) {
				try {
					player.spigot().sendMessage(message);
				} catch (NoSuchMethodError ignored) {}
			}
		}
		try {
			Bukkit.getConsoleSender().spigot().sendMessage(message);
		} catch (NoSuchMethodError ignored) {}
	}
}