package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.clip.ClipHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToolHandler {
	
	private final ClipHandler clipHandler;
	private final Map<UUID, ToolType> playerTools;
	
	public ToolHandler(ClipHandler clipHandler) {
		this.clipHandler = clipHandler;
		this.playerTools = new HashMap<>();
	}
	
	public ToolType getTool(UUID playerId) {
		playerTools.putIfAbsent(playerId, ToolType.CLIP);
		return playerTools.get(playerId);
	}
	
	public boolean setTool(UUID playerId, ToolType tool) {
		ToolType oldTool = playerTools.put(playerId, tool);
		boolean switchedTool = oldTool != tool;
		if (switchedTool && oldTool == ToolType.CLIP) {
			clipHandler.removeClip(playerId, true);
		}
		return switchedTool;
	}
}
