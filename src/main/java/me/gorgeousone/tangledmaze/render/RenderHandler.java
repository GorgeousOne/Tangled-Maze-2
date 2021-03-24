package me.gorgeousone.tangledmaze.render;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.event.ClipActionProcessEvent;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.clip.ClipTool;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles events that change what fake blocks need to be displayed to players.
 */
public class RenderHandler implements Listener {
	
	private final static int MAZE_BORDER_LAYER = 10;
	private final static int MAZE_EXIT_LAYER = 20;
	private final static int CLIP_BORDER_LAYER = 30;
	private final static int CLIP_VERTEX_LAYER = 40;

	private final static BlockData MAZE_BORDER_MAT = Material.REDSTONE_BLOCK.createBlockData();
	private final static BlockData MAZE_MAIN_EXIT_MAT = Material.DIAMOND_BLOCK.createBlockData();
	private final static BlockData MAZE_EXIT_MAT = Material.EMERALD_BLOCK.createBlockData();
	private final static BlockData CLIP_BORDER_MAT = Material.GOLD_BLOCK.createBlockData();
	private final static BlockData CLIP_VERTEX_MAT = Material.LAPIS_BLOCK.createBlockData();
	
	private final JavaPlugin plugin;
	private final ClipHandler clipHandler;
	private final Map<UUID, RenderSession> renderings;
	
	public RenderHandler(JavaPlugin plugin, ClipHandler clipHandler) {
		this.plugin = plugin;
		this.clipHandler = clipHandler;
		this.renderings = new HashMap<>();
	}
	
	public void disable() {
		for (RenderSession session : renderings.values()) {
			session.clear();
		}
	}
	
	RenderSession getRenderSession(UUID playerId) {
		if (renderings.containsKey(playerId)) {
			return renderings.get(playerId);
		}
		RenderSession render = new RenderSession(playerId);
		renderings.put(playerId, render);
		return render;
	}
	
	/**
	 * Updates vertices and borders of clips when being created with clip tools
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
						Clip clip = clipHandler.getClip(tool.getPlayerId());
						session.addLayer(CLIP_VERTEX_LAYER, tool.getVertices(), CLIP_VERTEX_MAT);
						session.addLayer(CLIP_BORDER_LAYER, clip.getBorderBlocks(), CLIP_BORDER_MAT);
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
	
	@EventHandler
	public void onMazeStart(MazeStartEvent event) {
		RenderSession session = getRenderSession(event.getPlayerId());
		Clip clip = event.getClip();
		
		session.removeLayer(CLIP_BORDER_LAYER, false);
		session.removeLayer(MAZE_EXIT_LAYER, false);
		session.removeLayer(MAZE_BORDER_LAYER, true);
		session.removeLayer(CLIP_VERTEX_LAYER, true);
		session.addLayer(MAZE_BORDER_LAYER, clip.getBorderBlocks(), MAZE_BORDER_MAT);
	}
	
	@EventHandler
	public void onClipActionProcess(ClipActionProcessEvent event) {
		RenderSession session = getRenderSession(event.getPlayerId());
		ClipAction change = event.getAction();
		
		session.removeFromLayer(MAZE_EXIT_LAYER, change.getRemovedExits(), false);
		session.removeFromLayer(MAZE_BORDER_LAYER, change.getRemovedBorder(), true);
		session.addToLayer(MAZE_BORDER_LAYER, change.getAddedBorderBlocks());
	}
}
