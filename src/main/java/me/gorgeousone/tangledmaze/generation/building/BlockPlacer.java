package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public final class BlockPlacer extends BukkitRunnable {
	
	private final Random random = new Random();
	private final Set<BlockState> backupBlocks = new HashSet<>();
	
	private final World world;
	private final BlockPalette palette;
	private final BuildCallBack callback;
	private final int blocksPerTick;
	private final Iterator<BlockVec> blockIter;
	
	public BlockPlacer(World world,
	                   Set<BlockVec> blocks,
	                   BlockPalette palette, int blocksPerTick, BuildCallBack callback) {
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
			
			if (blockLimitReached(placedBlocks, blocksPerTick, startTime)) {
				return;
			}
		}
		if (callback != null) {
			callback.onBuildFinish(backupBlocks);
			this.cancel();
		}
	}
	
	void placeBlock(BlockVec blockVec) {
		if ()
		Block block = world.getBlockAt(blockVec.getX(), blockVec.getY(), blockVec.getZ());
		BlockType type = palette.getBlock(random.nextInt(palette.size()));
		backupBlocks.add(type.updateBlock(block, false));
	}
	
	boolean blockLimitReached(int placedBlocks, int bpt, long startTime) {
		if (bpt > -1) {
			if (placedBlocks >= bpt) {
				return true;
			}
		}
		return System.currentTimeMillis() - startTime >= 40;
	}
}
