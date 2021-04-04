package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import me.gorgeousone.tangledmaze.tool.Ray;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Creates player clips from blocks clicked by players
 */
public class ClickListener implements Listener {
	
	private final ClipHandler clipHandler;
	private final ToolHandler toolHandler;
	
	public ClickListener(ClipHandler clipHandler, ToolHandler toolHandler) {
		this.clipHandler = clipHandler;
		this.toolHandler = toolHandler;
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
		
		if (!isMazeWand(heldItem)) {
			return;
		}
		event.setCancelled(true);
		UUID playerId = player.getUniqueId();
		Block clickedBlock = traceBlock(player, event);
		
		if (clickedBlock == null) {
			return;
		}
		switch (toolHandler.getTool(playerId)) {
			case CLIP:
				clipHandler.getClipTool(playerId).addVertex(clickedBlock);
				break;
			case EXIT:
				Clip maze = clipHandler.getMazeClip(playerId);
				if (maze != null) {
					maze.toggleExit(clickedBlock);
				}
				break;
			case BRUSH:
				break;
		}
	}
	
	private Block traceBlock(Player player, PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		return clickedBlock == null ? new Ray(player).traceBlock(100) : clickedBlock;
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
				clipHandler.setClip(playerId, ClipFactory.createClip(playerId, tool.getVertices(), tool.getShape()));
				break;
			case RESTART:
			case PROGRESS:
				break;
		}
	}
	
	/**
	 * Returns if the given ItemStack is a wand for maze creation
	 */
	boolean isMazeWand(ItemStack item) {
		if (item.getType() != Material.GOLDEN_SHOVEL) {
			return false;
		}
		return true;
	}
}
