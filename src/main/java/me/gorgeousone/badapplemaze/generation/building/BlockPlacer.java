package me.gorgeousone.badapplemaze.generation.building;

import me.gorgeousone.badapplemaze.util.BlockVec;
import me.gorgeousone.badapplemaze.util.MaterialUtil;
import me.gorgeousone.badapplemaze.util.blocktype.BlockLocType;
import me.gorgeousone.badapplemaze.util.blocktype.BlockType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public final class BlockPlacer extends BukkitRunnable {
	
	private final World world;
	private final BlockPalette palette;
	private final Consumer<Set<BlockLocType>> callback;
	private final int blocksPerTick;
	private final Iterator<BlockVec> blockIter;
	private final Iterator<BlockVec> airIter;

	private final BlockType AIR;

	public BlockPlacer(World world,
	                   Set<BlockVec> blocks,
	                   Set<BlockVec> air,
	                   BlockPalette palette, int blocksPerTick, Consumer<Set<BlockLocType>> callback) {
		this.world = world;
		this.callback = callback;
		this.palette = palette;
		this.blocksPerTick = blocksPerTick;
		this.blockIter = blocks.iterator();
		this.airIter = air.iterator();

		this.AIR = BlockType.get(Material.AIR);
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		int placedBlocks = 0;

		while (airIter.hasNext()) {
			placeAir(airIter.next());
			++placedBlocks;

			if (blockLimitReached(placedBlocks, startTime)) {
				return;
			}
		}
		while (blockIter.hasNext()) {
			placeBlock(blockIter.next());
			++placedBlocks;
			
			if (blockLimitReached(placedBlocks, startTime)) {
				return;
			}
		}
		if (callback != null) {
			callback.accept(null);
			this.cancel();
		}
	}
	
	void placeBlock(BlockVec blockVec) {
		Block block = world.getBlockAt(blockVec.getX(), blockVec.getY(), blockVec.getZ());
		BlockType type = palette.getRndBlock();
		new BlockLocType(block.getLocation(), type).updateBlock(false);
	}

	void placeAir(BlockVec blockVec) {
		Block block = world.getBlockAt(blockVec.getX(), blockVec.getY(), blockVec.getZ());
		new BlockLocType(block.getLocation(), AIR).updateBlock(false);
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
