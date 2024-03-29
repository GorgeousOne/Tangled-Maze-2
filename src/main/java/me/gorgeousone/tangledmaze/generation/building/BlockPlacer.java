package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public final class BlockPlacer extends BukkitRunnable {
	
	private final Set<BlockLocType> backupBlocks = new HashSet<>();
	
	private final World world;
	private final BlockPalette palette;
	private final Consumer<Set<BlockLocType>> callback;
	private final int blocksPerTick;
	private final Iterator<BlockVec> blockIter;
	
	public BlockPlacer(World world,
	                   Set<BlockVec> blocks,
	                   BlockPalette palette, int blocksPerTick, Consumer<Set<BlockLocType>> callback) {
		this.world = world;
		this.callback = callback;
		this.palette = palette;
		this.blocksPerTick = blocksPerTick;
		this.blockIter = blocks.iterator();
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		int placedBlocks = 0;
		
		while (blockIter.hasNext()) {
			placeBlock(blockIter.next());
			++placedBlocks;
			
			if (blockLimitReached(placedBlocks, startTime)) {
				return;
			}
		}
		if (callback != null) {
			callback.accept(backupBlocks);
			this.cancel();
		}
	}
	
	void placeBlock(BlockVec blockVec) {
		Block block = world.getBlockAt(blockVec.getX(), blockVec.getY(), blockVec.getZ());
		
		if (MaterialUtil.canBeReplaced(block.getType())) {
			BlockType type = palette.getRndBlock();
			backupBlocks.add(new BlockLocType(block.getLocation(), type).updateBlock(false));
		}
	}
	
	boolean blockLimitReached(int placedBlocks, long startTime) {
		if (blocksPerTick > -1) {
			if (placedBlocks >= blocksPerTick) {
				return true;
			}
		}
		return System.currentTimeMillis() - startTime >= 25;
	}
}
