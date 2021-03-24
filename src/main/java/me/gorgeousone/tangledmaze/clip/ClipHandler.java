package me.gorgeousone.tangledmaze.clip;

import java.util.HashMap;
import java.util.UUID;

public class ClipHandler {
	
	private final HashMap<UUID, ClipTool> playerClipTools;
	private final HashMap<UUID, Clip> playerClips;
	
	public ClipHandler() {
		this.playerClipTools = new HashMap<>();
		this.playerClips = new HashMap<>();
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
}
