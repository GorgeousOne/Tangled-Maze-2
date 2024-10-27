package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class that stores the maze data of players while they are building a maze.
 */
public class SessionHandler {
	
	private final Map<UUID, Clip> playerClips;
	private final Map<UUID, Clip> playerMazes;
	private final Map<UUID, MazeSettings> mazeSettings;
	private final Map<Clip, MazeBackup> mazeBackups;
	
	public SessionHandler() {
		playerClips = new HashMap<>();
		playerMazes = new HashMap<>();
		mazeSettings = new HashMap<>();
		mazeBackups = new HashMap<>();
	}
	
	public void disable() {
		playerClips.clear();
		playerMazes.clear();
		mazeSettings.clear();
		mazeBackups.clear();
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
			Bukkit.getPluginManager().callEvent(new ClipDeleteEvent(playerId, clip));
		}
	}
	
	public void setMazeClip(UUID playerId, Clip clip) {
		playerMazes.put(playerId, clip);
	}
	
	public MazeSettings getSettings(UUID playerId) {
		return mazeSettings.computeIfAbsent(playerId, setting -> new MazeSettings());
	}
	
	public void setSettings(UUID playerId, MazeSettings settings) {
		mazeSettings.put(playerId, settings);
	}
	
	public boolean isBuilt(Clip maze) {
		return mazeBackups.containsKey(maze) && mazeBackups.get(maze).getMazeMap() != null;
	}
	
	/**
	 * Compute a maze backup of this maze if none exists yet.
	 */
	public MazeBackup backupMaze(Clip maze, MazeSettings settings) {
		return mazeBackups.computeIfAbsent(maze, backup -> new MazeBackup(maze, settings));
	}
	
	public MazeBackup getBackup(Clip maze) {
		return mazeBackups.get(maze);
	}
	
	public void removeBackup(Clip maze) {
		mazeBackups.remove(maze);
	}
	
	public void setMazeBackup(Clip maze, MazeBackup backup) {
		mazeBackups.put(maze, backup);
	}
}
