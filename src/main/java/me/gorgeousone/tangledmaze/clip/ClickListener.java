package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Creates player clips from blocks clicked by players
 */
public class ClickListener implements Listener {
	
	private final ClipHandler clipHandler;
	
	public ClickListener(ClipHandler clipHandler) {this.clipHandler = clipHandler;}
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (event.getHand() != EquipmentSlot.HAND || action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		Player player = event.getPlayer();
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		
		if (!isMazeWand(heldItem)) {
			return;
		}
		
		event.setCancelled(true);
		clipHandler.getClipTool(player.getUniqueId()).addVertex(event.getClickedBlock());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onClipToolChange(ClipToolChangeEvent event) {
		ClipTool tool = event.getTool();
		UUID playerId = tool.getPlayerId();
		
		switch (event.getCause()) {
			case COMPLETE:
				clipHandler.setClip(playerId, ClipFactory.createClip(tool.getVertices(), tool.getShape()));
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
