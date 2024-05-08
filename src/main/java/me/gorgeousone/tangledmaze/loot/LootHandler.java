package me.gorgeousone.tangledmaze.loot;

import fr.black_eyes.lootchest.Config;
import fr.black_eyes.lootchest.Lootchest;
import fr.black_eyes.lootchest.Main;
import fr.black_eyes.lootchest.Utils;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LootHandler {

	private final Main lootChestPlugin;
	private final Map<BlockFace, Integer> legacyDirIds;
	private Method lootChestCreateChest;

	public LootHandler(Main lootChestPlugin) {
		this.lootChestPlugin = lootChestPlugin;
		this.legacyDirIds = new HashMap<>();

		legacyDirIds.put(BlockFace.NORTH, 2);
		legacyDirIds.put(BlockFace.SOUTH, 3);
		legacyDirIds.put(BlockFace.WEST, 4);
		legacyDirIds.put(BlockFace.EAST, 5);

		hackyHackLootChest();
	}

	private void hackyHackLootChest() {
		try {
			lootChestCreateChest = Lootchest.class.getDeclaredMethod("createChest", Block.class, Location.class);
			lootChestCreateChest.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean chestExists(String chestName) {
		return lootChestPlugin.getLootChest().containsKey(chestName);
	}

	public Map<String, BlockVec> spawnChests(MazeMap mazeMap, Map<String, Integer> chestAmounts, Collection<BlockVec> existingChests) throws TextException {
		List<String> chestPrefabList = listChests(chestAmounts);
		Map<Vec2, Direction> wallDirs = LootChestLocator.findRoomWallCells(mazeMap);
		List<Vec2> walls = new ArrayList<>(wallDirs.keySet());

		Collections.shuffle(chestPrefabList);
		Collections.shuffle(walls);
		Map<String, BlockVec> lootLocations = new HashMap<>();

		while (!chestPrefabList.isEmpty() && !walls.isEmpty()) {
			String prefabName = chestPrefabList.remove(0);
			Vec2 wall = walls.remove(0);

			Direction dir = wallDirs.get(wall);
			removeNeighbors(walls, wall);
			spawnLootChest(prefabName, wall.toLocation(mazeMap.getWorld(), mazeMap.getY(wall) + 1), dir.getFace());

		}
		//write all chests to the config and save the file
		lootChestPlugin.getConfigFiles().saveData();
		return lootLocations;
	}

	public void respawnChests(Set<String> chestNames) {
		Config lootChestConfig = Config.getInstance();
		Boolean saveSetting = lootChestConfig.save_Chest_Locations_At_Every_Spawn;
		lootChestConfig.save_Chest_Locations_At_Every_Spawn = false;

		try {
			for (String chestName : chestNames) {
				Lootchest chest = lootChestPlugin.getLootChest().get(chestName);

				if (chest != null) {
					Location chestPos = chest.getActualLocation();
					lootChestCreateChest.invoke(chest, chestPos.getBlock(), chestPos);
				}
			}
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		lootChestConfig.save_Chest_Locations_At_Every_Spawn = saveSetting;

		if (lootChestConfig.save_Chest_Locations_At_Every_Spawn) {
			lootChestPlugin.getUtils().updateData();
		}
	}

	public void removeChests(Set<String> chestNames) {
		FileConfiguration dataConfig = lootChestPlugin.getConfigFiles().getData();

		for (String chestName : chestNames) {
			Lootchest lc = lootChestPlugin.getLootChest().get(chestName);
			dataConfig.set("chests." + chestName, null);
			Utils.deleteChest(lc);
		}
		lootChestPlugin.getConfigFiles().saveData();
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
		if (!chestExists(chestPrefabName)) {
			throw new IllegalArgumentException("Could not find loot chest \"" + chestPrefabName + "\".");
		}
		Block chestBlock = placeChestBlock(location, facing);
		String chestName = String.format("zz-%s-%s", chestPrefabName, UUID.randomUUID());
		Lootchest prefab = lootChestPlugin.getLootChest().get(chestPrefabName);
		Lootchest newChest = new Lootchest(chestBlock, chestName);
		lootChestPlugin.getLootChest().put(chestName, newChest);
		copyChestPrefab(newChest, prefab);
	}

	private Block placeChestBlock(Location location, BlockFace facing) {
		BlockType blockType = BlockType.get(
				String.format("CHEST[facing=%s]", facing.name()),
				"CHEST:" + legacyDirIds.get(facing));
		BlockLocType chestBlock = new BlockLocType(location, blockType);
		chestBlock.updateBlock(false);
		return location.getBlock();
	}

	/**
	 * Function to mimic LootChest's Utils#copychest, only skipping a few things to improve performance
	 */
	private void copyChestPrefab(Lootchest newChest, Lootchest prefab) {
		//creates the most lag
		//newChest.setHolo(prefab.getHolo());

		for (int i = 0; i < prefab.getChances().length; ++i) {
			newChest.setChance(i, prefab.getChances()[i]);
		}
		//disable fall animation (also creates lag idk)
		newChest.setFall(false);

		newChest.getInv().setContents(prefab.getInv().getContents());
		newChest.setTime(prefab.getTime());
		newChest.setParticle(prefab.getParticle());
		newChest.setRespawn_cmd(prefab.getRespawn_cmd());
		newChest.setRespawn_natural(prefab.getRespawn_natural());
		newChest.setTake_msg(prefab.getTake_msg());
		newChest.setRadius(prefab.getRadius());
		newChest.spawn(true);
		//skip saving config file, save after all chests are spawned
	}
}
