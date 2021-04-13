package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.tool.ClipTool;
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
import org.bukkit.util.BlockIterator;

import java.util.UUID;

/**
 * Creates player clips from blocks clicked by players
 */
public class ClickListener implements Listener {
	
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	
	public ClickListener(SessionHandler sessionHandler, ToolHandler toolHandler) {
		this.sessionHandler = sessionHandler;
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
		
		if (heldItem.getType() == Material.STONE_SHOVEL) {
			event.setCancelled(true);
			return;
		}
		if (!isMazeWand(heldItem)) {
			return;
		}
		event.setCancelled(true);
		UUID playerId = player.getUniqueId();
		Block tracedBlock = traceBlock(player, event);
		
		if (tracedBlock == null) {
			return;
		}
		switch (toolHandler.getTool(playerId)) {
			case CLIP:
				sessionHandler.getClipTool(playerId).addVertex(tracedBlock);
				break;
			case EXIT:
				Clip maze = sessionHandler.getMazeClip(playerId);
				if (maze != null) {
					maze.toggleExit(tracedBlock);
				}
				break;
			case BRUSH:
				break;
		}
		//		if (event.getClickedBlock() != null) {
		//			createBlockUpdateEvent(event.getClickedBlock());
		//		}
	}
	
	boolean hoverClickEnabled = true;
	int hoverRange = 100;
	
	private Block traceBlock(Player player, PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		
		if (clickedBlock == null && hoverClickEnabled) {
			BlockIterator iter = new BlockIterator(player, hoverRange);
			
			while (iter.hasNext()) {
				Block nextBlock = iter.next();
				
				if (nextBlock.getType().isSolid()) {
					return nextBlock;
				}
			}
		}
		return clickedBlock;
	}
	
	//	public void createBlockUpdateEvent(Block clickedBlock) {
	//		Set<Block> updatedBlocks = new HashSet<>();
	//		updatedBlocks.add(clickedBlock);
	//
	//		for (BlockFace face : BlockUtil.DIRECT_FACES) {
	//			updatedBlocks.add(clickedBlock.getRelative(face));
	//		}
	//		Bukkit.getPluginManager().callEvent(new BlockUpdateEvent(updatedBlocks));
	//	}
	
	/**
	 * Creates a clip for the player when a clip tool reaches the required amount of vertices for it.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onClipToolChange(ClipToolChangeEvent event) {
		ClipTool tool = event.getTool();
		UUID playerId = tool.getPlayerId();
		
		switch (event.getCause()) {
			case COMPLETE:
				sessionHandler.setClip(playerId, ClipFactory.createClip(playerId, tool.getVertices(), tool.getShape()));
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
		return item.getType() == Material.GOLDEN_SHOVEL;
	}
}
