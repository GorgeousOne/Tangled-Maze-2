package me.gorgeousone.tangledmaze.clip;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClipHandler {
	
	private final HashMap<UUID, ClipTool> playerClipTools;
	private final HashMap<UUID, Clip> playerClips;
	private final HashMap<UUID, Clip> playerMazes;
	
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
	
	public Clip getMazeClip(UUID playerId) {
		return playerMazes.get(playerId);
	}
	
	public void setMazeClip(UUID playerId, Clip clip) {
		playerMazes.put(playerId, clip);
	}
	
	/**
	 * Returns existing player clip or creates a new one
	 */
	ClipTool getClipTool(UUID playerId) {
		if (playerClipTools.containsKey(playerId)) {
			return playerClipTools.get(playerId);
		}
		ClipTool clipTool = new ClipTool(playerId, ClipShape.RECTANGLE);
		playerClipTools.put(playerId, clipTool);
		return clipTool;
	}
	
	public Clip getClip(UUID playerId) {
		return playerClips.get(playerId);
	}
	
	public void setClip(UUID playerId, Clip clip) {
		playerClips.put(playerId, clip);
	}
	
	public void removeClip(UUID playerId) {
		playerClips.remove(playerId);
	}
	
	public UUID getClipOwner(Clip clip) {
		if (playerClips.containsValue(clip)) {
			for (Map.Entry<UUID, Clip> entry : playerClips.entrySet()) {
				if (entry.getValue() == clip) {
					return entry.getKey();
				}
			}
		} else if (playerMazes.containsValue(clip)) {
			for (Map.Entry<UUID, Clip> entry : playerMazes.entrySet()) {
				if (entry.getValue() == clip) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
}
