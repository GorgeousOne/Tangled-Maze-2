package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.ClipType;
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
	private final Map<UUID, ClipType> playerClipTypes;
	
	public ToolHandler(SessionHandler sessionHandler) {
		this.sessionHandler = sessionHandler;
		playerTools = new HashMap<>();
		playerClipTools = new HashMap<>();
		playerClipTypes = new HashMap<>();
	}
	
	public void disable() {
		playerTools.clear();
		playerClipTools.clear();
	}
	
	public ToolType createToolIfAbsent(UUID playerId) {
		playerTools.putIfAbsent(playerId, ToolType.EXIT_SETTER);
		return playerTools.get(playerId);
	}
	
	public boolean setToolType(UUID playerId, ToolType newToolType) {
		return playerTools.put(playerId, newToolType) != newToolType;
	}
	
	/**
	 * Returns existing player clip or creates a new one
	 */
	public ClipTool createClipToolIfAbsent(UUID playerId) {
		playerClipTools.computeIfAbsent(playerId, function -> new ClipTool(playerId, ClipType.RECTANGLE));
		return playerClipTools.get(playerId);
	}
	
	/**
	 * Removes any (not maze) clip started by the player
	 */
	public void resetClipTool(UUID playerId) {
		if (null == Bukkit.getPlayer(playerId)) {
			return;
		}
		playerClipTools.get(playerId).reset();
		sessionHandler.removeClip(playerId, true);
	}
	
	public boolean setClipType(UUID playerId, ClipType newClipType) {
		ClipType oldClipType = createClipTypeIfAbsent(playerId);
		
		if (oldClipType == newClipType) {
			return false;
		}
		playerClipTypes.put(playerId, newClipType);
		ClipTool clipTool = createClipToolIfAbsent(playerId);
		
		if (areShapesCompatible(oldClipType, newClipType)) {
			clipTool.setType(newClipType);
			
			if (clipTool.isComplete()) {
				sessionHandler.removeClip(playerId, true);
				Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(clipTool, ClipToolChangeEvent.Cause.COMPLETE));
			}
		} else if (!clipTool.isComplete()) {
			if (clipTool.getVertices().size() < newClipType.getVertexCount()) {
				clipTool.setType(newClipType);
			} else {
				clipTool.reset();
			}
		}
		return true;
	}
	
	public boolean areShapesCompatible(ClipType type0, ClipType type1) {
		switch (type0) {
			case RECTANGLE:
				return type1 == ClipType.ELLIPSE;
			case ELLIPSE:
				return type1 == ClipType.RECTANGLE;
			case TRIANGLE:
			default:
				return false;
		}
	}
	
	public ClipType createClipTypeIfAbsent(UUID playerId) {
		playerClipTypes.putIfAbsent(playerId, ClipType.RECTANGLE);
		return playerClipTypes.get(playerId);
	}
	
	public void removePlayer(UUID playerId) {
		playerClipTypes.remove(playerId);
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
