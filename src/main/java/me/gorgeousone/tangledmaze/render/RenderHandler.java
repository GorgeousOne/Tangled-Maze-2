package me.gorgeousone.tangledmaze.render;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.event.ClipActionProcessEvent;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.event.ClipUpdateEvent;
import me.gorgeousone.tangledmaze.event.MazeBuildEvent;
import me.gorgeousone.tangledmaze.event.MazeExitSetEvent;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles events that change what fake blocks need to be displayed in render sessions
 */
public class RenderHandler implements Listener {
	
	private final static int MAZE_BORDER_LAYER = 10;
	private final static int MAZE_EXIT_LAYER = 20;
	private final static int MAZE_MAIN_EXIT_LAYER = 30;
	private final static int CLIP_BORDER_LAYER = 40;
	private final static int CLIP_VERTEX_LAYER = 50;
	
	private final static BlockData MAZE_BORDER_MAT = Material.REDSTONE_BLOCK.createBlockData();
	private final static BlockData MAZE_MAIN_EXIT_MAT = Material.DIAMOND_BLOCK.createBlockData();
	private final static BlockData MAZE_EXIT_MAT = Material.EMERALD_BLOCK.createBlockData();
	private final static BlockData CLIP_BORDER_MAT = Material.GOLD_BLOCK.createBlockData();
	private final static BlockData CLIP_VERTEX_MAT = Material.LAPIS_BLOCK.createBlockData();
	
	private final JavaPlugin plugin;
	private final SessionHandler sessionHandler;
	private final Map<UUID, RenderSession> renderings;
	
	public RenderHandler(JavaPlugin plugin, SessionHandler sessionHandler) {
		this.plugin = plugin;
		this.sessionHandler = sessionHandler;
		this.renderings = new HashMap<>();
	}
	
	/**
	 * Hides and clears all render sessions on plugin disable
	 */
	public void disable() {
		for (RenderSession session : renderings.values()) {
			session.clear();
		}
		renderings.clear();
	}
	
	/**
	 * Returns the render session of the player with this UUID or creates a new one
	 */
	RenderSession getRenderSession(UUID playerId) {
		if (renderings.containsKey(playerId)) {
			return renderings.get(playerId);
		}
		RenderSession render = new RenderSession(playerId);
		renderings.put(playerId, render);
		return render;
	}
	
	public void removePlayer(UUID playerId) {
		renderings.remove(playerId);
	}
	
	/**
	 * Updates vertices and borders of clips when being created with a clip tool
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClipToolChange(ClipToolChangeEvent event) {
		ClipTool tool = event.getTool();
		ClipToolChangeEvent.Cause cause = event.getCause();
		RenderSession session = getRenderSession(event.getPlayerId());
		
		new BukkitRunnable() {
			@Override
			public void run() {
				switch (cause) {
					case COMPLETE:
						Clip clip = sessionHandler.getClip(tool.getPlayerId());
						session.addLayer(CLIP_VERTEX_LAYER, tool.getVertices(), CLIP_VERTEX_MAT);
						session.addLayer(CLIP_BORDER_LAYER, clip.getBlocks(clip.getBorder()), CLIP_BORDER_MAT);
						break;
					case RESTART:
						session.removeLayer(CLIP_BORDER_LAYER, true);
						session.removeLayer(CLIP_VERTEX_LAYER, true);
						session.addLayer(CLIP_VERTEX_LAYER, tool.getVertices(), CLIP_VERTEX_MAT);
						break;
					case PROGRESS:
						session.addLayer(CLIP_VERTEX_LAYER, tool.getVertices(), CLIP_VERTEX_MAT);
						break;
				}
			}
		}.runTaskLater(plugin, 2);
	}
	
	/**
	 * Renders new created maze clips in redstone
	 */
	@EventHandler
	public void onMazeStart(MazeStartEvent event) {
		Clip clip = event.getClip();
		RenderSession session = getRenderSession(clip.getPlayerId());
		
		session.removeLayer(CLIP_BORDER_LAYER, false);
		session.removeLayer(MAZE_EXIT_LAYER, false);
		session.removeLayer(MAZE_MAIN_EXIT_LAYER, false);
		session.removeLayer(MAZE_BORDER_LAYER, true);
		session.removeLayer(CLIP_VERTEX_LAYER, true);
		
		session.addLayer(MAZE_BORDER_LAYER, clip.getBlocks(clip.getBorder()), MAZE_BORDER_MAT);
		List<Vec2> exits = clip.getExits();
		
		if (!exits.isEmpty()) {
			session.addLayer(MAZE_MAIN_EXIT_LAYER, clip.getBlocks(exits.subList(0, 1)), MAZE_MAIN_EXIT_MAT);
			session.addLayer(MAZE_EXIT_LAYER, clip.getBlocks(exits), MAZE_EXIT_MAT);
		} else {
			session.addLayer(MAZE_MAIN_EXIT_LAYER, new HashMap<>(), MAZE_MAIN_EXIT_MAT);
			session.addLayer(MAZE_EXIT_LAYER, new HashMap<>(), MAZE_EXIT_MAT);
		}
	}
	
	/**
	 * Hides a (not maze) clip when bring deleted
	 */
	@EventHandler
	public void onClipDelete(ClipDeleteEvent event) {
		RenderSession session = getRenderSession(event.getClip().getPlayerId());
		session.removeLayer(CLIP_BORDER_LAYER, true);
		session.removeLayer(CLIP_VERTEX_LAYER, true);
	}
	
	/**
	 * Renders changes applied to a clip with a clip action
	 */
	@EventHandler
	public void onClipActionProcess(ClipActionProcessEvent event) {
		UUID playerId = event.getClip().getPlayerId();
		RenderSession session = getRenderSession(playerId);
		ClipAction change = event.getAction();
		
		session.removeFromLayer(MAZE_BORDER_LAYER, change.getRemovedBorder(), true);
		session.removeFromLayer(MAZE_EXIT_LAYER, change.getRemovedExits(), true);
		session.addToLayer(MAZE_BORDER_LAYER, change.getAddedBorderBlocks());
	}
	
	@EventHandler
	public void onExitSet(MazeExitSetEvent event) {
		UUID playerId = event.getMaze().getPlayerId();
		RenderSession session = getRenderSession(playerId);
		
		session.removeFromLayer(MAZE_EXIT_LAYER, event.getRemovedExits(), true);
		session.removeFromLayer(MAZE_MAIN_EXIT_LAYER, event.getRemovedMainExits(), true);
		session.addToLayer(MAZE_MAIN_EXIT_LAYER, event.getAddedMainExits());
		session.addToLayer(MAZE_EXIT_LAYER, event.getAddedMainExits());
	}
	
	@EventHandler
	public void onMazeBuild(MazeBuildEvent event) {
		UUID playerId = event.getPlayerId();
		RenderSession session = getRenderSession(playerId);
		session.clear();
		removePlayer(playerId);
	}
	
	@EventHandler
	public void onClipUpdate(ClipUpdateEvent event) {
		UUID playerId = event.getClip().getPlayerId();
		RenderSession session = getRenderSession(playerId);
		session.updateBlock(event.getLoc(), event.getNewY());
	}
}
