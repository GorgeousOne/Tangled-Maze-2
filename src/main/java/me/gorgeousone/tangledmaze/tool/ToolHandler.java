package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.ClipShape;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToolHandler implements Listener {
	
	private final SessionHandler sessionHandler;
	private final Map<UUID, ToolType> playerTools;
	private final Map<UUID, ClipTool> playerClipTools;
	
	public ToolHandler(SessionHandler sessionHandler) {
		this.sessionHandler = sessionHandler;
		playerTools = new HashMap<>();
		playerClipTools = new HashMap<>();
	}
	
	public void disable() {
		playerTools.clear();
		playerClipTools.clear();
	}
	
	public ToolType createToolIfAbsent(UUID playerId) {
		playerTools.putIfAbsent(playerId, ToolType.CLIP_TOOL);
		return playerTools.get(playerId);
	}
	
	/**
	 * Returns existing player clip or creates a new one
	 */
	public ClipTool createClipToolIfAbsent(UUID playerId) {
		playerClipTools.computeIfAbsent(playerId, function -> new ClipTool(playerId, ClipShape.RECTANGLE));
		return playerClipTools.get(playerId);
	}
	
	public void resetClipTool(UUID playerId) {
		playerClipTools.get(playerId).reset();
		sessionHandler.removeClip(playerId, true);
	}
	
	public boolean setClipShape(UUID playerId, ClipShape newShape) {
		ClipTool clipTool = createClipToolIfAbsent(playerId);
		
		if (clipTool.getShape() != newShape) {
			clipTool.setShape(newShape);
			
			if (sessionHandler.getClip(playerId) != null) {
				sessionHandler.removeClip(playerId, true);
				Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(clipTool, ClipToolChangeEvent.Cause.COMPLETE));
			}
			return true;
		}
		return false;
	}
	
	//	public boolean setTool(UUID playerId, ToolType toolType) {
	//		ToolType oldTool = playerTools.put(playerId, toolType);
	//		boolean switchedTool = oldTool != toolType;
	//
	//		if (switchedTool) {
	//			Clip clip = sessionHandler.getClip(playerId);
	//
	//			if (clip != null) {
	//				ClipTool tool = sessionHandler.getClipTool(playerId);
	//				tool.setShape();
	//				sessionHandler.removeClip(playerId, true);
	//				Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(tool, ClipToolChangeEvent.Cause.COMPLETE));
	//			}
	//		}
	//		return switchedTool;
	//	}
	
	//	@EventHandler
	//	public void onMazeBuild(MazeBuildEvent event) {
	//		UUID playerId = event.getPlayerId();
	//		playerTools.put(playerId, ToolType.CLIP);
	//	}
}
