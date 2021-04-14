package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionHandler implements Listener {
	
	private final Map<UUID, Clip> playerClips;
	private final Map<UUID, Clip> playerMazes;
	private final HashMap<UUID, MazeSettings> mazeSettings;
	
	public SessionHandler() {
		playerClips = new HashMap<>();
		playerMazes = new HashMap<>();
		mazeSettings = new HashMap<>();
	}
	
	public void disable() {
		playerClips.clear();
		playerMazes.clear();
		mazeSettings.clear();
	}
	
	public void removePlayer(UUID playerId) {
		playerClips.remove(playerId);
		playerMazes.remove(playerId);
		mazeSettings.remove(playerId);
	}
	
	public Map<UUID, Clip> getPlayerClips() {
		return playerClips;
	}
	
	public Map<UUID, Clip> getPlayerMazes() {
		return playerMazes;
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
	
	public void removeClip(UUID playerId, boolean callEvent) {
		Clip clip = playerClips.remove(playerId);
		
		if (clip != null && callEvent) {
			Bukkit.getPluginManager().callEvent(new ClipDeleteEvent(clip));
		}
	}
	
	public void setMazeClip(UUID playerId, Clip clip) {
		playerMazes.put(playerId, clip);
	}
	
	public MazeSettings getSettings(UUID playerId) {
		mazeSettings.computeIfAbsent(playerId, setting -> new MazeSettings());
		return mazeSettings.get(playerId);
	}
}
