package me.gorgeousone.tangledmaze.loot;

import fr.black_eyes.lootchest.Lootchest;
import fr.black_eyes.lootchest.Main;
import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LootHandler {

	private final SessionHandler sessionHandler;
	private final Main lootChestPlugin;
	private final Map<BlockFace, Integer> legacyDirIds;

	public LootHandler(SessionHandler sessionHandler, Main lootChestPlugin) {
		this.sessionHandler = sessionHandler;
		this.lootChestPlugin = lootChestPlugin;
		this.legacyDirIds = new HashMap<>();

		legacyDirIds.put(BlockFace.NORTH, 2);
		legacyDirIds.put(BlockFace.SOUTH, 3);
		legacyDirIds.put(BlockFace.WEST, 4);
		legacyDirIds.put(BlockFace.EAST, 5);
	}

	public boolean chestExists(String chestName) {
		return lootChestPlugin.getLootChest().containsKey(chestName);
	}

	public Map<BlockVec, String> placeChests(MazeMap mazeMap, Map<String, Integer> chestAmounts) throws TextException {
		List<String> chestList = listChests(chestAmounts);
		Map<Vec2, Direction> wallDirs = LootChestLocator.findRoomWallCells(mazeMap);
		List<Vec2> walls = new ArrayList<>(wallDirs.keySet());

		Collections.shuffle(chestList);
		Collections.shuffle(walls);

		while (!chestList.isEmpty() && !walls.isEmpty()) {
			String name = chestList.remove(0);
			Vec2 wall = walls.remove(0);

			Direction dir = wallDirs.get(wall);
			removeNeighbors(walls, wall);
			spawnLootChest(name, wall.toLocation(mazeMap.getWorld(), mazeMap.getY(wall) + 1), dir.getFace());

		}
		Bukkit.broadcastMessage("done");
		return null;
	}

	private List<String> listChests(Map<String, Integer> chestAmounts) {
		List<String> chestList = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : chestAmounts.entrySet()) {
			String item = entry.getKey();
			int amount = entry.getValue();
			for (int i = 0; i < amount; i++) {
				chestList.add(item);
			}
		}
		Collections.shuffle(chestList);
		return chestList;
	}

	private void removeNeighbors(List<Vec2> positions, Vec2 pos) {
		for (Direction dir : Direction.CARDINALS) {
			positions.remove(pos.clone().add(dir.getVec2()));
		}
	}

	private void spawnLootChest(String chestPrefabName, Location location, BlockFace facing) {
		Bukkit.broadcastMessage("spawn " + chestPrefabName + " at " + location.toVector());
		if (!chestExists(chestPrefabName)) {
			throw new IllegalArgumentException("Could not find loot chest \"" + chestPrefabName + "\".");
		}
		Block chestBlock = placeChestBlock(location, facing);
		String chestName = String.format("zz-%s-%s", chestPrefabName, UUID.randomUUID());
		Lootchest chest = registerLootChest(chestName, chestBlock);
		Lootchest prefab = lootChestPlugin.getLootChest().get(chestPrefabName);
		lootChestPlugin.getUtils().copychest(chest, prefab);
	}

	private Block placeChestBlock(Location location, BlockFace facing) {
		BlockType blockType = BlockType.get(
				String.format("CHEST[facing=%s]", facing.name()),
				"CHEST:" + legacyDirIds.get(facing));
		BlockLocType chestBlock = new BlockLocType(location, blockType);
		chestBlock.updateBlock(false);
		return location.getBlock();
	}

	private Lootchest registerLootChest(String name, Block chest) {
		Lootchest loot = new Lootchest(chest, name);
		lootChestPlugin.getLootChest().put(name, loot);
		lootChestPlugin.getLootChest().get(name).spawn(true);
		lootChestPlugin.getUtils().updateData(Main.getInstance().getLootChest().get(name));
		return loot;
	}
}
