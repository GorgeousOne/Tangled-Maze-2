package me.gorgeousone.tangledmaze.loot;

import fr.black_eyes.lootchest.Lootchest;
import fr.black_eyes.lootchest.Main;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.Room;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LootHandler {

	private final Map<BlockFace, Integer> legacyDirIds;
	private final Main lootChestPlugin;

	public LootHandler(Main lootChestPlugin) {
		this.lootChestPlugin = lootChestPlugin;
		this.legacyDirIds = new HashMap<>();

		legacyDirIds.put(BlockFace.NORTH, 2);
		legacyDirIds.put(BlockFace.SOUTH, 3);
		legacyDirIds.put(BlockFace.WEST, 4);
		legacyDirIds.put(BlockFace.EAST, 5);
	}

	public Map<BlockVec, String> placeChests(MazeMap mazeMap, Map<String, Integer> chestAmounts) throws TextException {
		for (String chestName : chestAmounts.keySet()) {
			if (!lootChestPlugin.getLootChest().containsKey(chestName)) {
				//TODO list error in lang
				throw new TextException(Message.ERROR_INVALID_SETTING, new Placeholder("setting", "Chest does not exits" + chestName));
			}
		}
		List<String> chestList = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : chestAmounts.entrySet()) {
			String item = entry.getKey();
			int amount = entry.getValue();
			for (int i = 0; i < amount; i++) {
				chestList.add(item);
			}
		}
		Collections.shuffle(chestList);
		Map<GridCell, BlockFace> wallCells = findRoomWallCells(mazeMap);
		int spawnAttempts = 1000;

		for (String item : chestList) {

		}
		return null;
	}

	/**
	 * @return map of non-junction grid cells in all rooms mapped to facing direction
	 */
	private Map<BlockVec, Direction> findRoomWallCells(MazeMap mazeMap) {
		GridMap gridMap = mazeMap.getPathMap();
		List<Room> rooms = gridMap.getRooms();
		Map<BlockVec, Direction> blocks = new HashMap<>();

		for (Room room : rooms) {
			for (Vec2 gridPos : room.getBorderCells()) {
				if (isJunction(gridPos)) {
					continue;
				}
				GridCell cell = gridMap.getCell(gridPos);
				Direction facing = room.getWallFacing(gridPos);
				blocks.putAll(getWallBlocks(cell, facing, mazeMap));
			}
		}
		return blocks;
	}

	private boolean isJunction(Vec2 gridPos) {
		return gridPos.getX() % 2 == 0 && gridPos.getZ() % 2 == 0;
	}

	private Map<BlockVec, Direction> getWallBlocks(GridCell cell, Direction direction, MazeMap mazeMap) {
		Map<BlockVec, Direction> wallBlocks = new HashMap<>();

		for (Vec2 pos : cell.getWalls(direction)) {
			wallBlocks.put(new BlockVec(pos, mazeMap.getY(pos) + 1), direction);
		}
		return wallBlocks;
	}

	private void spawnLootChest(String chestPrefabName, Location location, BlockFace facing) {
		if (!lootChestPlugin.getLootChest().containsKey(chestPrefabName)) {
			throw new IllegalArgumentException("Could not find loot chest \"" + chestPrefabName + "\".");
		}
		Block chestBlock = placeChestBlock(location, facing);
		String chestName = UUID.randomUUID().toString();
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
