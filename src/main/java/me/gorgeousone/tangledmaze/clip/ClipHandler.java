package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClipHandler {
	
	private final Map<UUID, ClipTool> playerClipTools;
	private final Map<UUID, Clip> playerClips;
	private final Map<UUID, Clip> playerMazes;
	
	public ClipHandler() {
		this.playerClipTools = new HashMap<>();
		this.playerClips = new HashMap<>();
		this.playerMazes = new HashMap<>();
	}
	
	public void disable() {
		playerClipTools.clear();
		playerClips.clear();
		playerMazes.clear();
	}
	
	/**
	 * Returns existing player clip or creates a new one
	 */
	public ClipTool getClipTool(UUID playerId) {
		playerClipTools.computeIfAbsent(playerId, function -> new ClipTool(playerId, ClipShape.RECTANGLE));
		return playerClipTools.get(playerId);
	}
	
	public Clip getClip(UUID playerId) {
		return playerClips.get(playerId);
	}
	
	public Clip getMazeClip(UUID playerId) {
		return playerMazes.get(playerId);
	}
	
	public void setClip(UUID playerId, Clip clip) {
		playerClips.put(playerId, clip);
	}
	
	public void removeClip(UUID playerId) {
		playerClips.remove(playerId);
	}

	public void removeClip(UUID playerId, boolean callEvent) {
		Clip clip = playerClips.remove(playerId);
		if (clip != null && callEvent) {
			Bukkit.getPluginManager().callEvent(new ClipDeleteEvent(clip, playerId));
		}
	}
	
	public void setMazeClip(UUID playerId, Clip clip) {
		playerMazes.put(playerId, clip);
	}
}
