package me.gorgeousone.tangledmaze.loot;

import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.generation.paving.Room;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LootChestLocator {

	private static final Random RND = new Random();

	public static List<GridCell> getAvailableCells(
			MazeMap mazeMap,
			boolean isLootInHallways,
			boolean isLootInDeadEnds,
			boolean isLootInRooms) {
		GridMap gridMap = mazeMap.getPathMap();
		List<GridCell> availableCells = new ArrayList<>();
		List<Room> rooms = gridMap.getRooms();

		for (int x = 0; x < gridMap.getWidth(); ++x) {
			for (int z = 0; z < gridMap.getHeight(); ++z) {
				if (isWall(x, z) || gridMap.getPathType(x, z) != PathType.PAVED) {
					continue;
				}
				boolean isRoom = isAnyRoom(rooms, x, z);

				if (isRoom && (!isLootInRooms || !isAnyRoomBorder(rooms, x, z))) {
					continue;
				}
				boolean isDeadEnd = !isRoom && gridMap.isDeadEnd(x, z);

				if (!isLootInDeadEnds && isDeadEnd) {
					continue;
				}
				if (!isLootInHallways && !isRoom && !isDeadEnd) {
					continue;
				}
				availableCells.add(gridMap.getCell(x, z));
			}
		}
		return availableCells;
	}

	public static Map<Vec2, Direction> findChestSpawns(int chestCount, List<GridCell> availableCells, GridMap gridMap, Set<Vec2> existingSpawns) {
		Set<Vec2> occupiedBlocks = new HashSet<>();
		existingSpawns.forEach(v -> markOccupiedSpawns(occupiedBlocks, v));
		Map<Vec2, Direction> spawns = new HashMap<>();
		int spawnAttempts = 1000;

		while (spawnAttempts > 0 && spawns.size() < chestCount && !availableCells.isEmpty()) {
			--spawnAttempts;
			GridCell cell = availableCells.get(RND.nextInt(availableCells.size()));
			List<Direction> wallDirs = gridMap.getWallDirs(cell);
			Map<Vec2, Direction> blocks = new HashMap<>();

			for (Direction dir : wallDirs) {
				for (Vec2 block : cell.getWalls(dir)) {
					blocks.put(block, dir);
				}
			}
			blocks.keySet().removeAll(occupiedBlocks);

			if (blocks.isEmpty()) {
				availableCells.remove(cell);
				continue;
			}
			Vec2 rndBlock = blocks.keySet().stream().skip(RND.nextInt(blocks.size())).findFirst().orElse(null);
			spawns.put(rndBlock, blocks.get(rndBlock));
			markOccupiedSpawns(occupiedBlocks, rndBlock);
		}
		return spawns;
	}

	private static boolean isWall(int gridX, int gridZ) {
		return gridX % 2 == 1 && gridZ % 2 == 1;
	}

	private static boolean isAnyRoom(List<Room> rooms, int gridX, int gridZ) {
		return rooms.stream().anyMatch(r -> r.contains(gridX, gridZ));
	}

	private static boolean isAnyRoomBorder(List<Room> rooms, int gridX, int gridZ) {
		return rooms.stream().anyMatch(r -> r.borderContains(gridX, gridZ));
	}

	private static void markOccupiedSpawns(Set<Vec2> occupied, Vec2 spawn) {
		for (Direction dir : Direction.CARDINALS) {
			occupied.add(spawn.clone().add(dir.getVec2()));
		}
	}
}
