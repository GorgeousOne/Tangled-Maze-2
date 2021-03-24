package me.gorgeousone.tangledmaze.maze;

import me.gorgeousone.tangledmaze.clip.Clip;

import java.util.HashMap;
import java.util.UUID;

public class MazeHandler {
	
	private final HashMap<UUID, Clip> mazeClips;
	
	public MazeHandler() {
		this.mazeClips = new HashMap<>();
	}
	
	public Clip getMazeClip(UUID playerId) {
		return mazeClips.get(playerId);
	}
	
	public void setMazeClip(UUID playerId, Clip clip) {
		mazeClips.put(playerId, clip);
	}
}
