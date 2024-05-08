package me.gorgeousone.tangledmaze.loot;

import fr.black_eyes.lootchest.Lootchest;
import fr.black_eyes.lootchest.Main;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
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

	public void spawnLootChest(String chestPrefabName, Location location, BlockFace facing) {
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
