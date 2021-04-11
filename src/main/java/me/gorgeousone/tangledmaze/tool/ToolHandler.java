package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.event.MazeBuildEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToolHandler implements Listener {
	
	private final SessionHandler sessionHandler;
	private final Map<UUID, ToolType> playerTools;
	
	public ToolHandler(SessionHandler sessionHandler) {
		this.sessionHandler = sessionHandler;
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
			sessionHandler.removeClip(playerId, true);
		}
		return switchedTool;
	}
	
	@EventHandler
	public void onMazeBuild(MazeBuildEvent event) {
		UUID playerId = event.getPlayerId();
		playerTools.put(playerId, ToolType.CLIP);
	}
}
