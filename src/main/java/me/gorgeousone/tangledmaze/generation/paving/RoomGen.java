package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RoomGen {

	private static Random RANDOM;

	public static void genRooms(GridMap gridMap, MazeSettings settings) {
		int roomCount = settings.getValue(MazeProperty.ROOM_COUNT);
		int pathWidth = settings.getValue(MazeProperty.PATH_WIDTH);
		int wallWidth = settings.getValue(MazeProperty.WALL_WIDTH);
		int maxRoomSize = settings.getValue(MazeProperty.ROOM_SIZE);
		int seed = settings.getValue(MazeProperty.SEED);
		
		RANDOM = seed == 0 ? new Random() : new Random(seed);
		//make rooms at lease 7 blocks wide, but smaller if requested by player's settings
		//absolut minimum 3x3 grid cells
		int cellsMin = calcCellsMin(Math.min(7, maxRoomSize), pathWidth, wallWidth);
		int cellsMax = Math.max(cellsMin, maxRoomSize);

		//list free conjunctions to start at
		Set<Vec2> freeCells = new HashSet<>();

		for (int gridX = 0; gridX < gridMap.getWidth() - cellsMin; gridX += 2) {
			for (int gridZ = 0; gridZ < gridMap.getHeight() - cellsMin; gridZ += 2) {
				Vec2 gridPos = new Vec2(gridX, gridZ);
				if (gridMap.getPathType(gridX, gridZ) == PathType.FREE) {
					freeCells.add(gridPos);
				//remove cells that cant fit a 3x3 room
				} else {
					//TODO adapt to cellsMin
					freeCells.remove(gridPos.add(0, -2));
					freeCells.remove(gridPos.add(-2, 0));
					freeCells.remove(gridPos.add(0, 2));
				}
			}
		}
		if (freeCells.isEmpty()) {
			return;
		}
		int spawnedRooms = 0;
		int spawnAttempts = 1000;
		List<Vec2> freeCellsList = new ArrayList<>(freeCells);

		while (!freeCellsList.isEmpty() && spawnedRooms < roomCount && spawnAttempts > 0) {
			--spawnAttempts;
			Room room = spawnRoom(gridMap, freeCellsList, cellsMin, cellsMax);

			if (room != null) {
				gridMap.addRoom(room);
				spawnedRooms += 1;
			}
		}
	}

	/**
	 * Calculates the minimum amount of cells a room should have to be considered a room.
	 * @param blocksMin minimum length of a room in blocks ingame, like a 3x3 blocks room would be quite small
	 */
	private static int calcCellsMin(int blocksMin, int pathWidth, int wallWidth) {
		int cellSize = 3;
		int blockSize = 2 * pathWidth + wallWidth;

		while(blockSize < blocksMin) {
			blockSize += pathWidth + wallWidth;
			cellSize += 2;
		}
		return cellSize;
	}

	/**
	 * Attempts to spawn a room at a random free conjunction with random size.
	 * @return Room instance if successful, null if the random spot was not free.
	 */
	private static Room spawnRoom(GridMap gridMap, List<Vec2> freeCells, int cellsMin, int cellsMax) {
		//create first random dimension
		int size1 = rndRoomSize(cellsMin, cellsMax);
		//create second random size so that rooms are never large in both dimensions
		//cellsMin + (cellsMax - cellsMin) - (size1 - cellsMin)
		int size2 = rndRoomSize(cellsMin, cellsMin + cellsMax - size1);

		boolean rndOrientation = RANDOM.nextBoolean();
		Vec2 cellSize = new Vec2(
				rndOrientation ? size1 : size2,
				rndOrientation ? size2 : size1);

		Vec2 rndCell = freeCells.get(RANDOM.nextInt(freeCells.size()));
		boolean isFree = isRoomFree(gridMap, rndCell.getX(), rndCell.getZ(), cellSize.getX(), cellSize.getZ());

		if (!isFree) {
			return null;
		}
		Room room = new Room(rndCell, cellSize);
		room.markRoom(gridMap, PathType.ROOM);
		freeCells.removeAll(room.getPathCells());
		return room;
	}

	/**
	 * Returns random integer in the range of cellsMin to cellsMax, that is an odd number.
	 */
	private static int rndRoomSize(int cellsMin, int cellsMax) {
		int twoStep = (cellsMax - cellsMin) / 2;
		return cellsMin + RANDOM.nextInt( twoStep + 1) * 2;
	}

	/**
	 * Check if all cells of a room are FREE on the grid map.
	 */
	private static boolean isRoomFree(GridMap gridMap, int startX, int startZ, int sizeX, int sizeZ) {
		for (int x = startX; x < startX + sizeX; ++x) {
			for (int z = startZ; z < startZ + sizeZ; ++z) {
				if (gridMap.getPathType(x, z) != PathType.FREE) {
					return false;
				}
			}
		}
		return true;
	}

}
