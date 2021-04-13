package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipShape;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import me.gorgeousone.tangledmaze.event.TerrainChangeEvent;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SessionHandler implements Listener {
	
	private final Map<UUID, ClipTool> playerClipTools;
	private final Map<UUID, Clip> playerClips;
	private final Map<UUID, Clip> playerMazes;
	private final HashMap<UUID, MazeSettings> mazeSettings;
	
	public SessionHandler() {
		playerClipTools = new HashMap<>();
		playerClips = new HashMap<>();
		playerMazes = new HashMap<>();
		mazeSettings = new HashMap<>();
	}
	
	public void disable() {
		playerClipTools.clear();
		playerClips.clear();
		playerMazes.clear();
		mazeSettings.clear();
	}
	
	public void removePlayer(UUID playerId) {
		playerClipTools.remove(playerId);
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
