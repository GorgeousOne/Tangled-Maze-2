package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Set;

public class BlockResetter extends BukkitRunnable {
	
	private final JavaPlugin plugin;
	private final Runnable callback;
	private final int blocksPerTick;
	private final Iterator<BlockLocType> blockIter;
	
	public BlockResetter(JavaPlugin plugin,
	                     Set<BlockLocType> blocks,
	                     int blocksPerTick,
	                     Runnable callback) {
		this.plugin = plugin;
		this.callback = callback;
		this.blocksPerTick = blocksPerTick;
		this.blockIter = blocks.iterator();
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		int placedBlocks = 0;
		
		while (blockIter.hasNext()) {
			blockIter.next().updateBlock(true);
			++placedBlocks;
			
			if (blockLimitReached(placedBlocks, blocksPerTick, startTime)) {
				return;
			}
		}
		if (callback != null) {
			//why is this delay here?
			new BukkitRunnable() {
				@Override
				public void run() {
					callback.run();
				}
			}.runTaskLater(plugin, 2);
			this.cancel();
		}
	}
	
	boolean blockLimitReached(int placedBlocks, int bpt, long startTime) {
		if (bpt > -1) {
			if (placedBlocks >= bpt) {
				return true;
			}
		}
		return System.currentTimeMillis() - startTime >= 25;
	}
}
