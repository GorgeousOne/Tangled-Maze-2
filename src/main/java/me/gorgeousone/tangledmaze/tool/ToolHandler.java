package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.ClipType;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class to keep track of and manage the maze editing and clip creating tools of players.
 */
public class ToolHandler {
	
	private final SessionHandler sessionHandler;
	private final Map<UUID, ToolType> playerToolTypes;
	private final Map<UUID, ClipTool> playerClipTools;
	// mapping of clip type each player uses currently
	// separate to the type of the ClipTool itself because TODO find out
	private final Map<UUID, ClipType> playerClipTypes;
	
	public ToolHandler(SessionHandler sessionHandler) {
		this.sessionHandler = sessionHandler;
		playerToolTypes = new HashMap<>();
		playerClipTools = new HashMap<>();
		playerClipTypes = new HashMap<>();
	}
	
	public void disable() {
		playerToolTypes.clear();
		playerClipTools.clear();
	}
	
	public ToolType createToolIfAbsent(UUID playerId) {
		//dont return putIfAbsent, it returns null you idiot
		return playerToolTypes.computeIfAbsent(playerId, id -> ToolType.EXIT_SETTER);
	}
	
	public boolean setToolType(UUID playerId, ToolType newToolType) {
		return playerToolTypes.put(playerId, newToolType) != newToolType;
	}
	
	/**
	 * Returns existing player clip or creates a new one
	 */
	public ClipTool createClipToolIfAbsent(UUID playerId) {
		return playerClipTools.computeIfAbsent(playerId, function -> new ClipTool(playerId, ClipType.RECTANGLE));
	}
	
	/**
	 * Removes any (golden) clip started by the player
	 */
	public void resetClipTool(UUID playerId) {
		if (!playerClipTools.containsKey(playerId)) {
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
		return playerClipTypes.computeIfAbsent(playerId, id -> ClipType.RECTANGLE);
	}
	
	public void removePlayer(UUID playerId) {
		playerClipTypes.remove(playerId);
		playerClipTools.remove(playerId);
		playerToolTypes.remove(playerId);
	}
}
