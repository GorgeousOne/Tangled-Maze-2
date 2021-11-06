package me.gorgeousone.tangledmaze.render;

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
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Stores layers of fake blocks to be displayed to a player. Layers with greater index will be displayed on top of
 * layer with a lower index. Each layer will be displayed with one type of block.
 */
public class RenderSession {
	
	private final UUID playerId;
	private final UUID worldId;
	private final Map<Vec2, Integer> displayedBlocks;
	private final Map<Integer, Set<Vec2>> layers;
	private final Map<Integer, BlockType> layerTypes;
	private boolean isVisible;
	
	public RenderSession(UUID playerId) {
		Player player = Bukkit.getPlayer(playerId);

		if (null == player) {
			throw new IllegalArgumentException("Could not find player to UUID " + playerId);
		}
		this.playerId = playerId;
		worldId = player.getWorld().getUID();
		
		displayedBlocks = new HashMap<>();
		layers = new TreeMap<>(Collections.reverseOrder());
		layerTypes = new HashMap<>();
		isVisible = true;
	}
	
	/**
	 * Hides fake blocks displayed to the player and clears all layers
	 */
	public void clear() {
		hide();
		layers.clear();
		layerTypes.clear();
		displayedBlocks.clear();
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public void hide() {
		if (isVisible) {
			Player player = Bukkit.getPlayer(playerId);
			World world = player.getWorld();
			
			for (Map.Entry<Vec2, Integer> block : displayedBlocks.entrySet()) {
				Location blockLoc = block.getKey().toLocation(world, block.getValue());
				BlockType.get(blockLoc.getBlock()).sendBlockChange(player, blockLoc);
			}
			isVisible = false;
		}
	}
	
	public void show() {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Map.Entry<Vec2, Integer> block : displayedBlocks.entrySet()) {
			Location blockLoc = block.getKey().toLocation(world, block.getValue());
			layerTypes.get(getTopLayer(block.getKey())).sendBlockChange(player, blockLoc);
		}
		isVisible = true;
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
			displayedBlocks.putIfAbsent(loc, y);
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
	private void hideBlocks(int layerIndex, Set<Vec2> locs) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Vec2 loc : locs) {
			Location blockLoc = loc.toLocation(world, displayedBlocks.get(loc));
			int topLayerIndex = getTopLayer(loc);
			
			if (topLayerIndex == -1) {
				displayedBlocks.remove(loc);
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
	
	public void updateBlock(Vec2 loc, int newY) {
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		boolean updatedTopLayer = !isVisible;
		
		for (int layerKey : layers.keySet()) {
			Set<Vec2> layer = layers.get(layerKey);
			
			if (layer.contains(loc)) {
				if (!updatedTopLayer) {
					Location blockLoc = loc.toLocation(world, displayedBlocks.get(loc));
					BlockType.get(blockLoc.getBlock()).sendBlockChange(player, blockLoc);
					
					blockLoc.setY(newY);
					layerTypes.get(layerKey).sendBlockChange(player, blockLoc);
					updatedTopLayer = true;
				}
				displayedBlocks.put(loc, newY);
			}
		}
	}
	
	public void redisplayBlocks(Set<Vec2> locs) {
		if (!isVisible) {
			return;
		}
		Player player = Bukkit.getPlayer(playerId);
		World world = player.getWorld();
		
		for (Vec2 loc : locs) {
			int topLayer = getTopLayer(loc);
			
			if (topLayer != -1) {
				layerTypes.get(topLayer).sendBlockChange(player, loc.toLocation(world, displayedBlocks.get(loc)));
			}
		}
	}
}