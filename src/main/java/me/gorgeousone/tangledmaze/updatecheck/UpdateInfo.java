package me.gorgeousone.tangledmaze.updatecheck;

import me.gorgeousone.tangledmaze.util.Version;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class UpdateInfo {
	
	private final String resourceName;
	private final int resourceId;
	private final Version version;
	private final String description;
	
	public UpdateInfo(String updateInfo, String resourceName, int resourceId) {
		this.resourceName = resourceName;
		this.resourceId = resourceId;
		
		String[] split = updateInfo.split(";");
		version = new Version(split[0]);
		description = split[1];
	}
	
	public Version getVersion() {
		return version;
	}
	
	public BaseComponent[] getChatMessage() {
		ComponentBuilder builder = new ComponentBuilder(version.toString()).color(ChatColor.GREEN);
		builder.append(" " + description).color(ChatColor.YELLOW);
		builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/" + resourceName + "." + resourceId));
		builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("view plugin page").create()));
		return builder.create();
	}
}