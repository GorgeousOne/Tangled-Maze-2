package me.gorgeousone.tangledmaze.render;

import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores layers of fake blocks to be displayed to a player. Layers with greater index will be displayed on top of
 * layer with a lower index. Each layer will be displayed with one type of block.
 */
public class RenderSession {
	
	private final UUID playerId;
	private final Map<Integer, Map<Vec2, Integer>> layers;
	private final Map<Integer, BlockData> layerMats;
	
	public RenderSession(UUID playerId) {
		this.playerId = Objects.requireNonNull(playerId);
		layers = new TreeMap<>(Collections.reverseOrder());
		layerMats = new HashMap<>();
	}
	
	/**
	 * Hides fake blocks displayed to the player and clears all layers
	 */
	public void clear() {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Map<Vec2, Integer> blocks : layers.values()) {
			for (Map.Entry<Vec2, Integer> entry : blocks.entrySet()) {
				Location blockLoc = entry.getKey().toLocation(world, entry.getValue());
				player.sendBlockChange(blockLoc, blockLoc.getBlock().getBlockData());
			}
		}
		layers.clear();
		layerMats.clear();
	}
	
	public void addLayer(int layerIndex, Collection<Block> blocks, BlockData material) {
		Map<Vec2, Integer> blockLocs = new TreeMap<>();
		blocks.forEach(block -> blockLocs.put(new Vec2(block), block.getY()));
		addLayer(layerIndex, blockLocs, material);
	}
	
	public void addLayer(int layerIndex, Map<Vec2, Integer> blocks, BlockData material) {
		layers.put(layerIndex, new HashMap<>());
		layerMats.put(layerIndex, material);
		addToLayer(layerIndex, blocks);
	}
	
	public void addToLayer(int layerIndex, Map<Vec2, Integer> blocks) {
		if (!layers.containsKey(layerIndex)) {
			return;
		}
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		Map<Vec2, Integer> layer = layers.get(layerIndex);
		BlockData material = layerMats.get(layerIndex);
		
		for (Map.Entry<Vec2, Integer> entry : blocks.entrySet()) {
			Vec2 loc = entry.getKey();
			int topLayerIndex = getTopLayer(loc);
			
			//displays the block of the layer if layers containing the same blocks are lower than this one
			if (layerIndex >= topLayerIndex) {
				Location blockLoc = loc.toLocation(world, entry.getValue());
				player.sendBlockChange(blockLoc, material);
			}
		}
		layer.putAll(blocks);
	}
	
	public void removeLayer(int layerIndex, boolean updateFakeBlocks) {
		if (!layers.containsKey(layerIndex)) {
			return;
		}
		layerMats.remove(layerIndex);
		Map<Vec2, Integer> removedLayer = layers.remove(layerIndex);
		
		if (updateFakeBlocks) {
			hideBlocks(layerIndex, removedLayer);
		}
	}
	
	public void removeFromLayer(int layerIndex, Set<Vec2> locs, boolean updateFakeBlocks) {
		if (!layers.containsKey(layerIndex)) {
			return;
		}
		Map<Vec2, Integer> layer = layers.get(layerIndex);
		Map<Vec2, Integer> blocks = locs.stream().filter(layer::containsKey).collect(Collectors.toMap(Function.identity(), layer::get));
		layers.get(layerIndex).keySet().removeAll(blocks.keySet());
		
		if (updateFakeBlocks) {
			hideBlocks(layerIndex, blocks);
		}
	}
	
	/**
	 * Hides the given blocks on the selected layer by displaying underlying layers or the original block type itself
	 */
	private void hideBlocks(int layerIndex, Map<Vec2, Integer> blocks) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Map.Entry<Vec2, Integer> entry : blocks.entrySet()) {
			Vec2 loc = entry.getKey();
			Location blockLoc = loc.toLocation(world, entry.getValue());
			int topLayerIndex = getTopLayer(loc);
			
			if (topLayerIndex == -1) {
				player.sendBlockChange(blockLoc, blockLoc.getBlock().getBlockData());
			} else if (topLayerIndex <= layerIndex) {
				player.sendBlockChange(blockLoc, layerMats.get(topLayerIndex));
			}
		}
	}
	
	/**
	 * Returns the greatest layer index for the given xz coordinate for determining the top layer to display
	 */
	int getTopLayer(Vec2 loc) {
		for (int layerKey : layers.keySet()) {
			if (layers.get(layerKey).containsKey(loc)) {
				return layerKey;
			}
		}
		return -1;
	}
	
	public void updateBlock(Vec2 loc, int newY) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		boolean updatedTopLayer = false;
		
		for (int layerKey : layers.keySet()) {
			Map<Vec2, Integer> layer = layers.get(layerKey);
			
			if (layer.containsKey(loc)) {
				if (!updatedTopLayer) {
					Location blockLoc = loc.toLocation(world, layer.get(loc));
					player.sendBlockChange(blockLoc, blockLoc.getBlock().getBlockData());
					
					blockLoc.setY(newY);
					player.sendBlockChange(blockLoc, layerMats.get(layerKey));
					updatedTopLayer = true;
				}
				layer.put(loc, newY);
			}
		}
	}
}