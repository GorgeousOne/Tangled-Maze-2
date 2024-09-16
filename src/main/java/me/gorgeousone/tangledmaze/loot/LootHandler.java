package me.gorgeousone.tangledmaze.loot;

import fr.black_eyes.api.LootChestAPI;
import fr.black_eyes.lootchest.Lootchest;
import fr.black_eyes.lootchest.Main;
import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LootHandler {

	private final SessionHandler sessionHandler;
	private final Main lootChestPlugin;
	private final Map<BlockFace, Integer> legacyDirIds;
	private final Logger logger;

	public LootHandler(SessionHandler sessionHandler, Main lootChestPlugin, Logger logger) {
		this.sessionHandler = sessionHandler;
		this.lootChestPlugin = lootChestPlugin;
		this.legacyDirIds = new HashMap<>();
		this.logger = logger;

		legacyDirIds.put(BlockFace.NORTH, 2);
		legacyDirIds.put(BlockFace.SOUTH, 3);
		legacyDirIds.put(BlockFace.WEST, 4);
		legacyDirIds.put(BlockFace.EAST, 5);
	}
	
	/**
	 * Returns list of all chest names that are not copies (do not end with "-###")
	 */
	public List<String> getOgChestNames() {
		return getChestNames().stream()
				.filter(s -> !s.matches("-\\d+$"))
				.collect(Collectors.toList());
	}
	
	public List<String> getChestNames() {
		try {
			return new ArrayList<>(LootChestAPI.getAllLootChests().keySet());
		} catch (NoSuchMethodError e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return new ArrayList<>();
		}
	}

	//TODO replace once LootChestApi.checkNameAvalability is public
	public boolean chestExists(String chestName) {
		try {
			return lootChestPlugin.getLootChest().containsKey(chestName);
		} catch (NoSuchMethodError e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return false;
		}
	}

	public Map<String, BlockVec> spawnChests(
			Clip maze,
			Map<String, Integer> chestAmounts,
			boolean isLootInHallways,
			boolean isLootInDeadEnds,
			boolean isLootInRooms) throws TextException {
		try {
			return spawnChestsUnsafe(
					maze,
					sanitizeNames(chestAmounts),
					isLootInHallways,
					isLootInDeadEnds,
					isLootInRooms);
		} catch (NoSuchMethodError e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return new HashMap<>();
		}
	}

		private Map<String, BlockVec> spawnChestsUnsafe(
			Clip maze,
			Map<String, Integer> chestAmounts,
			boolean isLootInHallways,
			boolean isLootInDeadEnds,
			boolean isLootInRooms) throws TextException {

		if (!sessionHandler.isBuilt(maze)) {
			throw new TextException(Message.INFO_MAZE_NOT_BUILT);
		}
		MazeBackup backup = sessionHandler.getBackup(maze);
		MazeMap mazeMap = backup.getMazeMap();
		GridMap gridMap = mazeMap.getPathMap();

		//list all locations blocked by already placed chests
		Set<Vec2> existingSpawns = backup.getLootLocations().values().stream()
				.map(BlockVec::toVec2)
				.collect(Collectors.toSet());

		List<String> chestPrefabList = listChests(chestAmounts);
		Collections.shuffle(chestPrefabList);

		List<GridCell> availableCells = LootChestLocator.getAvailableCells(
				mazeMap,
				isLootInHallways,
				isLootInDeadEnds,
				isLootInRooms);

		Map<Vec2, Direction> chestSpawns = LootChestLocator.findChestSpawns(
				chestPrefabList.size(),
				availableCells,
				gridMap,
				existingSpawns);

		Map<String, BlockVec> addedChests = new HashMap<>();

		for (Vec2 pos : chestSpawns.keySet()) {
			String prefabName = chestPrefabList.remove(0);
			int blockY = mazeMap.getY(pos) + 1;
			Direction dir = chestSpawns.get(pos);
			String copyName = spawnLootChest(prefabName, pos.toLocation(mazeMap.getWorld(), blockY), dir.getFace());
			addedChests.put(copyName, new BlockVec(pos, blockY));
		}
		LootChestAPI.saveAllLootChests();
		backup.addLootLocations(addedChests);
		Bukkit.getPluginManager().callEvent(new LootChangeEvent(maze));
		return addedChests;
	}

	public void respawnChests(Set<String> chestNames) {
		try {
			respawnChestsUnsafe(chestNames);
		} catch (NoSuchMethodError e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	public void respawnChestsUnsafe(Set<String> chestNames) {
		for (String chestName : chestNames) {
			Lootchest chest = LootChestAPI.getLootChest(chestName);

			if (chest != null) {
				chest.spawn(true);
			}
		}
	}

	public int removeChests(Clip maze) {
		try {
			return removeChestsUnsafe(maze);
		} catch (NoSuchMethodError e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return 0;
		}
	}

	public int removeChestsUnsafe(Clip maze) {
		MazeBackup backup = sessionHandler.getBackup(maze);
		Collection<String> chestNames = backup.getLootLocations().keySet();
		int removedChestCount = 0;

		for (String chestName : chestNames) {
			//TODO remove try once removeLootChest can't throw NPEs anymore
			try {
				LootChestAPI.removeLootChest(chestName);
				++removedChestCount;
			} catch (NullPointerException ignored) {}
		}
		if (removedChestCount == 0) {
			return 0;
		}
		LootChestAPI.saveAllLootChests();
		backup.clearLootLocations();
		Bukkit.getPluginManager().callEvent(new LootChangeEvent(maze));
		return removedChestCount;
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

	private String spawnLootChest(String chestPrefabName, Location location, BlockFace facing) throws TextException {
		if (!chestExists(chestPrefabName)) {
			throw new TextException(Message.ERROR_LOOT_CHEST_NAME_NOT_FOUND, new Placeholder("name", chestPrefabName));
		}
		Block chestBlock = placeChestBlock(location, facing);
		String chestName = findFreeNameIndex(chestPrefabName, getChestNames());
		Lootchest prefab = LootChestAPI.getLootChest(chestPrefabName);
		Lootchest newChest = new Lootchest(chestBlock, chestName);
		LootChestAPI.addLootChest(chestName, newChest);
		LootChestAPI.copyToExistingChest(prefab, newChest);
		return chestName;
	}
	
	/**
	 * Returns the first String with pattern "name-#" which is not used as chest name yet
	 */
	private static String findFreeNameIndex(String name, List<String> nameList) {
		String result;
		int i = 1;

		while (true) {
			String candidate = name + "-" + i;

			if (!nameList.contains(candidate)) {
				result = candidate;
				break;
			}
			++i;
		}
		return result;
	}

	private Block placeChestBlock(Location location, BlockFace facing) {
		BlockType blockType = BlockType.get(
				String.format("minecraft:chest[facing=%s]", facing.name().toLowerCase()),
				"CHEST:" + legacyDirIds.get(facing));
		BlockLocType chestBlock = new BlockLocType(location, blockType);
		chestBlock.updateBlock(false);
		return location.getBlock();
	}
	
	private static Map<String, Integer> sanitizeNames(Map<String, Integer> dirtyMap) {
		HashMap<String, Integer> cleanMap = new HashMap<>();

		for (String key : dirtyMap.keySet()) {
			cleanMap.put(sanitize(key), dirtyMap.get(key));
		}
		return cleanMap;
	}

	private static String sanitize(String input) {
		return input.replaceAll("[^a-zA-Z0-9\\-_]", "");
	}
}
