package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.event.TerrainChangeEvent;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A listener that makes all clips adapt to terrain height changes.
 */
public class BlockChangeListener implements Listener {
	
	private final JavaPlugin plugin;
	private final SessionHandler sessionHandler;
	
	public BlockChangeListener(JavaPlugin plugin, SessionHandler sessionHandler) {
		this.plugin = plugin;
		this.sessionHandler = sessionHandler;
	}
	
	private void updateClips(Block updatedBlock, Player cause) {
		updateClips(Arrays.asList(updatedBlock), cause);
	}
	
	private void updateClips(Collection<Block> changedBlocks, Player cause) {
		Bukkit.getPluginManager().callEvent(new TerrainChangeEvent(changedBlocks, cause));
		for (Block changedBlock : changedBlocks) {
			Set<Clip> clipsToUpdate = new HashSet<>();
			
			for (Clip clip : sessionHandler.getPlayerClips().values()) {
				if (clip.isFillBlock(changedBlock)) {
					clipsToUpdate.add(clip);
				}
			}
			for (Clip maze : sessionHandler.getPlayerMazes().values()) {
				if (maze.isFillBlock(changedBlock)) {
					clipsToUpdate.add(maze);
				}
			}
			if (clipsToUpdate.isEmpty()) {
				continue;
			}
			
			new BukkitRunnable() {
				@Override
				public void run() {
					Block updatedBlock = BlockUtil.getSurface(changedBlock);
					Vec2 blockLoc = new Vec2(updatedBlock);
					
					for (Clip clip : clipsToUpdate) {
						clip.updateLoc(blockLoc, updatedBlock.getY());
					}
				}
			}.runTaskLater(plugin, 2);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		updateClips(event.getBlock().getRelative(BlockFace.DOWN), event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		updateClips(event.getBlock(), event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplode(EntityExplodeEvent event) {
		updateClips(event.blockList(), null);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		updateClips(event.getBlock(), null);
	}
	
	//pumpkin/melon growing
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		updateClips(event.getBlock().getRelative(BlockFace.DOWN), null);
	}
	
	//grass, mycelium spreading
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		updateClips(event.getBlock(), null);
	}
	
	//obsidian, concrete
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		updateClips(event.getBlock().getRelative(BlockFace.DOWN), null);
	}
	
	//ice melting
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		updateClips(event.getBlock(), null);
	}
	
	//falling sand... and maybe endermen
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		updateClips(event.getBlock().getRelative(BlockFace.DOWN), null);
	}
}