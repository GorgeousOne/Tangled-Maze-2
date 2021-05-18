package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.clip.ClipType;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.render.RenderSession;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Creates player clips from blocks clicked by players
 */
public class ClickListener implements Listener {
	
	private final JavaPlugin plugin;
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	private final RenderHandler renderHandler;
	
	public ClickListener(JavaPlugin plugin, SessionHandler sessionHandler,
	                     ToolHandler toolHandler,
	                     RenderHandler renderHandler) {
		this.plugin = plugin;
		this.sessionHandler = sessionHandler;
		this.toolHandler = toolHandler;
		this.renderHandler = renderHandler;
	}
	
	/**
	 * Adds block clicked with a golden shovel as vertex to the clip tool of the player
	 */
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		Player player = event.getPlayer();
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		Block clickedBlock = event.getClickedBlock();
		
		if (!isMazeWand(heldItem)) {
			if (clickedBlock != null) {
				hideClipsOnClick(player, clickedBlock);
			}
			return;
		}
		event.setCancelled(true);
		UUID playerId = player.getUniqueId();
		Block tracedBlock = traceBlock(player, clickedBlock);
		
		if (tracedBlock == null) {
			return;
		}
		handleWandClick(playerId, tracedBlock);
		if (event.getClickedBlock() != null) {
			updateClickedBlocks(player, event.getClickedBlock());
		}
	}
	
	private void handleWandClick(UUID playerId, Block clickedBlock) {
		switch (toolHandler.createToolIfAbsent(playerId)) {
			case CLIP_TOOL:
				ClipTool clipTool = toolHandler.createClipToolIfAbsent(playerId);
				Clip clip = sessionHandler.getClip(playerId);
				Clip maze = sessionHandler.getMazeClip(playerId);
				
				if (isOnlyMazeBorderClicked(clipTool, clip, maze, clickedBlock)) {
					if (ClipActionFactory.canBeExit(maze, new Vec2(clickedBlock))) {
						maze.toggleExit(clickedBlock);
					}
					break;
				}
				ClipType clipType = toolHandler.createClipTypeIfAbsent(playerId);
				
				if (clipTool.getShape() != clipType) {
					sessionHandler.removeClip(playerId, true);
					toolHandler.resetClipTool(playerId);
					clipTool.setType(clipType);
				}
				clipTool.addVertex(clickedBlock);
				break;
			case BRUSH:
				break;
		}
	}
	private void hideClipsOnClick(Player player, Block clickedBlock) {
		UUID playerId = player.getUniqueId();
		RenderSession render = renderHandler.getPlayerRender(playerId);
		
		if (render == null || !render.isVisible()) {
			return;
		}
		ClipTool clipTool = toolHandler.createClipToolIfAbsent(playerId);
		Clip clip = sessionHandler.getClip(playerId);
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (clipTool.getVertices().contains(clickedBlock) ||
		    clip != null && clip.isBorderBlock(clickedBlock) ||
		    maze != null && maze.isBorderBlock(clickedBlock)) {
			render.hide();
		}
	}
	
	private Block traceBlock(Player player, Block clickedBlock) {
		if (clickedBlock == null && ConfigSettings.HOVER_CLICKING_ENABLED) {
			BlockIterator iter = new BlockIterator(player, ConfigSettings.HOVER_RANGE);
			
			while (iter.hasNext()) {
				Block nextBlock = iter.next();
				
				if (nextBlock.getType().isSolid()) {
					return nextBlock;
				}
			}
		}
		return clickedBlock;
	}
	
	private boolean isOnlyMazeBorderClicked(ClipTool clipTool, Clip clip, Clip maze, Block clickedBlock) {
		return maze != null && maze.isBorderBlock(clickedBlock) &&
		       (clip == null || !clip.isBorderBlock(clickedBlock)) &&
		       !clipTool.getVertices().contains(clickedBlock) &&
		       (clip != null || clipTool.getVertices().size() == 0);
	}
	
	private void updateClickedBlocks(Player player, Block clickedBlock) {
		RenderSession render = renderHandler.getPlayerRender(player.getUniqueId());
		
		if (render == null) {
			return;
		}
		Set<Vec2> updatedBlocks = new HashSet<>();
		updatedBlocks.add(new Vec2(clickedBlock));
		
		for (Direction dir : Direction.fourCardinals()) {
			Vec2 facing = dir.getVec2();
			updatedBlocks.add(new Vec2(clickedBlock.getRelative(facing.getX(), 0, facing.getZ())));
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				render.redisplayBlocks(updatedBlocks);
			}
		}.runTaskLater(plugin, 2);
	}
	
	/**
	 * Creates a clip for the player when a clip tool reaches the required amount of vertices for it.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onClipToolChange(ClipToolChangeEvent event) {
		ClipTool tool = event.getTool();
		UUID playerId = tool.getPlayerId();
		
		switch (event.getCause()) {
			case COMPLETE:
			case RESIZE_FINISH:
				sessionHandler.setClip(playerId, ClipFactory.createClip(playerId, tool.getVertices(), tool.getShape()));
				break;
			case RESTART:
				sessionHandler.removeClip(playerId, false);
			case PROGRESS:
				break;
		}
	}
	
	@EventHandler
	public void onSlotSwitch(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
		
		if (!isMazeWand(newItem)) {
			return;
		}
		RenderSession render = renderHandler.getPlayerRender(player.getUniqueId());
		
		if (render != null && !render.isVisible()) {
			render.show();
		}
	}
	
	/**
	 * Returns if the given ItemStack is a wand for maze creation
	 */
	boolean isMazeWand(ItemStack item) {
		return item != null && item.getType() == ConfigSettings.WAND_ITEM;
	}
}
