package me.gorgeousone.tangledmaze.render;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.event.ClipActionProcessEvent;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.event.ClipUpdateEvent;
import me.gorgeousone.tangledmaze.event.MazeExitSetEvent;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import me.gorgeousone.tangledmaze.event.MazeStateChangeEvent;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class to keep track of and render all clips and clip actions of a player.
 * It also listens to events that change the state of clips and clip actions and updates the render accordingly.
 */
public class RenderHandler implements Listener {
	
	private static int counter = 0;
	private final static int MAZE_BORDER_LAYER = ++counter;
	private final static int MAZE_SOLUTION_LAYER = ++counter;

	private final static int MAZE_EXIT_LAYER = ++counter;
	private final static int MAZE_MAIN_EXIT_LAYER = ++counter;

	private final static int CLIP_BORDER_LAYER = ++counter;
	private final static int CLIP_VERTEX_LAYER = ++counter;
	private final static int CLIP_RESIZE_LAYER = ++counter;
	
	
	private final BlockType MAZE_BORDER_MAT = BlockType.get(Material.REDSTONE_BLOCK);
	private final BlockType MAZE_MAIN_EXIT_MAT = BlockType.get(Material.DIAMOND_BLOCK);
	private final BlockType MAZE_EXIT_MAT = BlockType.get(Material.EMERALD_BLOCK);
	private final BlockType MAZE_SOLUTION_MAT = BlockType.get(Material.EMERALD_BLOCK);
	private final BlockType CLIP_BORDER_MAT = BlockType.get(Material.GOLD_BLOCK);
	private final BlockType CLIP_VERTEX_MAT = BlockType.get(Material.LAPIS_BLOCK);
	private final BlockType CLIP_RESIZE_MAT = BlockType.get(Material.LAPIS_ORE);
	
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
	
	public RenderSession getPlayerRender(UUID playerId) {
		return renderings.get(playerId);
	}
	
	public void removePlayer(UUID playerId) {
		renderings.remove(playerId);
	}
	
	/**
	 * Returns the render session of the player with this UUID or creates a new one
	 */
	private void createRenderIfAbsent(UUID playerId) {
		renderings.computeIfAbsent(playerId, render -> new RenderSession(playerId));
	}
	
	/**
	 * Updates vertices and borders of clips when being created with a clip tool
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClipToolChange(ClipToolChangeEvent event) {
		ClipTool tool = event.getTool();
		ClipToolChangeEvent.Cause cause = event.getCause();
		UUID playerID = event.getPlayerId();
		createRenderIfAbsent(playerID);
		RenderSession session = getPlayerRender(playerID);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				switch (cause) {
					case RESIZE_START:
						session.addLayer(CLIP_RESIZE_LAYER, tool.getVertices().subList(tool.getVertexToRelocate(), tool.getVertexToRelocate() + 1), CLIP_RESIZE_MAT);
						break;
					case RESIZE_FINISH:
						session.removeLayer(CLIP_RESIZE_LAYER, false);
						session.removeLayer(CLIP_BORDER_LAYER, true);
						session.removeLayer(CLIP_VERTEX_LAYER, true);
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
	 * Starts keeping track of a maze clip to render it if it's visible and the owner is a player.
	 */
	@EventHandler
	public void onMazeStart(MazeStartEvent event) {
		Clip maze = event.getMaze();
		
		if (!isPlayer(maze.getOwnerId())) {
			return;
		}
		createRenderIfAbsent(maze.getOwnerId());
		RenderSession session = getPlayerRender(maze.getOwnerId());
		
		session.removeLayer(MAZE_EXIT_LAYER, false);
		session.removeLayer(MAZE_MAIN_EXIT_LAYER, false);
		session.removeLayer(MAZE_BORDER_LAYER, true);
		displayMaze(session, maze);
	}
	
	/**
	 * Hides a (not maze) clip when being deleted
	 */
	@EventHandler
	public void onClipDelete(ClipDeleteEvent event) {
		UUID playerId = event.getClip().getOwnerId();
		
		if (!isPlayer(playerId)) {
			return;
		}
		RenderSession session = getPlayerRender(event.getPlayerId());
		session.removeLayer(CLIP_RESIZE_LAYER, false);
		session.removeLayer(CLIP_BORDER_LAYER, true);
		session.removeLayer(CLIP_VERTEX_LAYER, true);
	}
	
	/**
	 * Renders changes applied to a clip with a clip action
	 */
	@EventHandler
	public void onClipActionProcess(ClipActionProcessEvent event) {
		UUID playerId = event.getClip().getOwnerId();
		
		if (!isPlayer(playerId)) {
			return;
		}
		RenderSession session = getPlayerRender(playerId);
		ClipAction change = event.getAction();
		
		session.removeFromLayer(MAZE_BORDER_LAYER, change.getRemovedBorder(), true);
		session.removeFromLayer(MAZE_MAIN_EXIT_LAYER, change.getRemovedExits(), false);
		session.removeFromLayer(MAZE_EXIT_LAYER, change.getRemovedExits(), true);
		session.addToLayer(MAZE_BORDER_LAYER, change.getAddedBorderBlocks());
	}
	
	@EventHandler
	public void onExitSet(MazeExitSetEvent event) {
		UUID playerId = event.getMaze().getOwnerId();
		
		if (!isPlayer(playerId)) {
			return;
		}
		RenderSession session = getPlayerRender(playerId);
		session.removeFromLayer(MAZE_EXIT_LAYER, event.getRemovedExits(), true);
		session.removeFromLayer(MAZE_MAIN_EXIT_LAYER, event.getRemovedMainExits(), true);
		session.addToLayer(MAZE_MAIN_EXIT_LAYER, event.getAddedMainExits());
		session.addToLayer(MAZE_EXIT_LAYER, event.getAddedMainExits());
	}
	
	@EventHandler
	public void onClipUpdate(ClipUpdateEvent event) {
		UUID playerId = event.getClip().getOwnerId();
		
		if (!isPlayer(playerId)) {
			return;
		}
		RenderSession session = getPlayerRender(playerId);
		session.updateBlock(event.getLoc(), event.getNewY());
	}
	
	@EventHandler
	public void onMazeStateChange(MazeStateChangeEvent event) {
		Clip maze = event.getMaze();
		UUID playerId = maze.getOwnerId();
		
		if (!isPlayer(playerId)) {
			return;
		}
		RenderSession session = getPlayerRender(playerId);
		
		if (event.isMazeActive()) {
			session.removeLayer(MAZE_SOLUTION_LAYER, true);
			displayMaze(session, maze);
		} else {
			session.removeLayer(MAZE_MAIN_EXIT_LAYER, false);
			session.removeLayer(MAZE_EXIT_LAYER, true);
			session.removeLayer(MAZE_BORDER_LAYER, true);
		}
	}
	
	/**
	 * Removes the render session of a player and creates a new one with their maze int it, if it is located in the new world
	 */
	public void changePlayerWorld(UUID playerId, World newWorld) {
		removePlayer(playerId);
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (null == maze || maze.getWorld() != newWorld) {
			return;
		}
		createRenderIfAbsent(playerId);
		RenderSession session = getPlayerRender(playerId);
		session.hide();
		displayMaze(session, maze);
	}
	
	public void displayMazeSolution(RenderSession session, MazeMap mazeMap, Collection<GridCell> solution) {
		Map<Vec2, Integer> blocks = new HashMap<>();
		
		for (GridCell cell : solution) {
			Vec2 min = cell.getMin();
			Vec2 max = cell.getMax();
			
			for (int x = min.getX(); x < max.getX(); ++x) {
				for (int z = min.getZ(); z < max.getZ(); ++z) {
					blocks.put(new Vec2(x, z), mazeMap.getY(x, z));
				}
			}
		}
		session.addLayer(MAZE_SOLUTION_LAYER, blocks, MAZE_SOLUTION_MAT);
	}
	
	private void displayMaze(RenderSession render, Clip maze) {
		if (!maze.isEditable()) {
			return;
		}
		render.addLayer(MAZE_BORDER_LAYER, maze.getBlocks(maze.getBorder()), MAZE_BORDER_MAT);
		List<Vec2> exits = maze.getExits();
		
		if (!exits.isEmpty()) {
			render.addLayer(MAZE_MAIN_EXIT_LAYER, maze.getBlocks(exits.subList(0, 1)), MAZE_MAIN_EXIT_MAT);
			render.addLayer(MAZE_EXIT_LAYER, maze.getBlocks(exits), MAZE_EXIT_MAT);
		} else {
			render.addLayer(MAZE_MAIN_EXIT_LAYER, new HashMap<>(), MAZE_MAIN_EXIT_MAT);
			render.addLayer(MAZE_EXIT_LAYER, new HashMap<>(), MAZE_EXIT_MAT);
		}
	}
	
	private boolean isPlayer(UUID senderId) {
		return null != senderId && null != Bukkit.getPlayer(senderId);
	}
}
