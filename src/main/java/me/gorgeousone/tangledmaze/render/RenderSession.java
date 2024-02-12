package me.gorgeousone.tangledmaze.render;

import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * A class to store fake blocks displayed to a player. Fake blocks are stored in layers,
 * where the max index layer is rendered on top of the lower ones.
 */
public class RenderSession {
	
	private final UUID playerId;
	private final Map<Vec2, Integer> renderedBlocks;
	private final Map<Integer, Set<Vec2>> layers;
	private final Map<Integer, BlockType> layerTypes;
	
	public RenderSession(UUID playerId) {
		Player player = Bukkit.getPlayer(playerId);
		
		if (null == player) {
			throw new IllegalArgumentException("Could not find player to UUID " + playerId);
		}
		this.playerId = playerId;
		
		renderedBlocks = new HashMap<>();
		layers = new TreeMap<>(Collections.reverseOrder());
		layerTypes = new HashMap<>();
	}
	
	/**
	 * Hides fake blocks displayed to the player and clears all layers
	 */
	public void clear() {
		hide();
		layers.clear();
		layerTypes.clear();
		renderedBlocks.clear();
	}
	
	public boolean containsVisible(Block block) {
		Vec2 pos = new Vec2(block);
		int y = block.getY();
		return renderedBlocks.containsKey(pos) && renderedBlocks.get(pos) == y;
	}
	
	public void hide() {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();

		for (Map.Entry<Vec2, Integer> block : renderedBlocks.entrySet()) {
			Location blockLoc = block.getKey().toLocation(world, block.getValue());
			BlockType.get(blockLoc.getBlock()).sendBlockChange(player, blockLoc);
		}
	}
	
	public void show() {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Map.Entry<Vec2, Integer> block : renderedBlocks.entrySet()) {
			Location blockLoc = block.getKey().toLocation(world, block.getValue());
			layerTypes.get(getTopLayer(block.getKey())).sendBlockChange(player, blockLoc);
		}
	}
	
	public void addLayer(int layerIndex, Collection<Block> blocks, BlockType blockType) {
		Map<Vec2, Integer> blockLocs = new TreeMap<>();
		blocks.forEach(block -> blockLocs.put(new Vec2(block), block.getY()));
		addLayer(layerIndex, blockLocs, blockType);
	}
	
	public void addLayer(int layerIndex, Map<Vec2, Integer> blocks, BlockType blockType) {
		layers.put(layerIndex, new HashSet<>());
		layerTypes.put(layerIndex, blockType);
		addToLayer(layerIndex, blocks);
	}
	
	public void addToLayer(int layerIndex, Map<Vec2, Integer> blocks) {
		if (!layers.containsKey(layerIndex)) {
			return;
		}
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		Set<Vec2> layer = layers.get(layerIndex);
		BlockType blockType = layerTypes.get(layerIndex);
		
		for (Map.Entry<Vec2, Integer> entry : blocks.entrySet()) {
			Vec2 loc = entry.getKey();
			int y = entry.getValue();
			int topLayerIndex = getTopLayer(loc);
			
			//displays the block of the layer if layers containing the same blocks are lower than this one
			if (layerIndex >= topLayerIndex) {
				Location blockLoc = loc.toLocation(world, y);
				blockType.sendBlockChange(player, blockLoc);
			}
			renderedBlocks.putIfAbsent(loc, y);
		}
		layer.addAll(blocks.keySet());
	}
	
	public void removeLayer(int layerIndex, boolean updateFakeBlocks) {
		if (!layers.containsKey(layerIndex)) {
			return;
		}
		layerTypes.remove(layerIndex);
		Set<Vec2> removedLayer = layers.remove(layerIndex);
		
		if (updateFakeBlocks) {
			hideBlocks(layerIndex, removedLayer);
		}
	}
	
	public void removeFromLayer(int layerIndex, Set<Vec2> locs, boolean updateFakeBlocks) {
		if (!layers.containsKey(layerIndex)) {
			return;
		}
		Set<Vec2> layer = layers.get(layerIndex);
		locs.removeIf(loc -> !layer.contains(loc));
		layers.get(layerIndex).removeAll(locs);
		
		if (updateFakeBlocks) {
			hideBlocks(layerIndex, locs);
		}
	}
	
	/**
	 * Hides the given blocks on the selected layer by displaying underlying layers or the original block type itself
	 */
	public void hideBlocks(int layerIndex, Set<Vec2> locs) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Vec2 loc : locs) {
			Location blockLoc = loc.toLocation(world, renderedBlocks.get(loc));
			int topLayerIndex = getTopLayer(loc);
			
			if (topLayerIndex == -1) {
				renderedBlocks.remove(loc);
				BlockType.get(blockLoc.getBlock()).sendBlockChange(player, blockLoc);
			} else if (topLayerIndex <= layerIndex) {
				layerTypes.get(topLayerIndex).sendBlockChange(player, blockLoc);
			}
		}
	}
	
	/**
	 * Returns the greatest layer index for the given xz coordinate for determining the top layer to display
	 */
	int getTopLayer(Vec2 loc) {
		for (int layerKey : layers.keySet()) {
			if (layers.get(layerKey).contains(loc)) {
				return layerKey;
			}
		}
		return -1;
	}
	
	/**
	 * Updates the y value for all render layers at the given position, and re-renders the block if it is currently visible.
	 * @param loc
	 * @param newY
	 */
	public void updateBlock(Vec2 loc, int newY) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		boolean updatedTopLayer = renderedBlocks.containsKey(loc);
		
		for (int layerKey : layers.keySet()) {
			Set<Vec2> layer = layers.get(layerKey);
			
			if (layer.contains(loc)) {
				if (!updatedTopLayer) {
					Location blockLoc = loc.toLocation(world, renderedBlocks.get(loc));
					BlockType.get(blockLoc.getBlock()).sendBlockChange(player, blockLoc);
					
					blockLoc.setY(newY);
					layerTypes.get(layerKey).sendBlockChange(player, blockLoc);
					updatedTopLayer = true;
				}
				renderedBlocks.put(loc, newY);
			}
		}
	}
	
	public void redisplayBlocks(Set<Vec2> locs) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Vec2 loc : locs) {
			int topLayer = getTopLayer(loc);
			
			if (topLayer != -1) {
				layerTypes.get(topLayer).sendBlockChange(player, loc.toLocation(world, renderedBlocks.get(loc)));
			}
		}
	}
}