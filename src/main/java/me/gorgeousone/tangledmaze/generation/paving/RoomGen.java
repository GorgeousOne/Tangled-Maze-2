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

	private static Random rnd;

	public static List<Room> genRooms(GridMap gridMap, int roomCount, MazeSettings settings) {

		int pathWidth = settings.getValue(MazeProperty.PATH_WIDTH);
		int wallWidth = settings.getValue(MazeProperty.WALL_WIDTH);
		int cellsMin = calcCellsMin(8, pathWidth, wallWidth);
		int cellsMax = calcCellsMin(40, pathWidth, wallWidth);

		//list free conjunctions to start at
		Set<Vec2> freeCells = new HashSet<>();

		for (int gridX = 0; gridX < gridMap.getWidth() - cellsMin; gridX += 2) {
			for (int gridZ = 0; gridZ < gridMap.getHeight() - cellsMin; gridZ += 2) {
				Vec2 gridPos = new Vec2(gridX, gridZ);
				if (gridMap.getPathType(gridX, gridZ) == PathType.FREE) {
					freeCells.add(gridPos);
				//remove cells that cant fit 2x2 room
				} else {
					//TODO adapt to cellsMin
					freeCells.remove(gridPos.add(0, -2));
					freeCells.remove(gridPos.add(-2, 0));
					freeCells.remove(gridPos.add(0, 2));
				}
			}
		}
		int spawnedRooms = 0;
		int spawnAttempts = 1000;
		List<Vec2> freeCellsList = new ArrayList<>(freeCells);
		List<Room> rooms = new ArrayList<>();

		while (spawnedRooms < roomCount && spawnAttempts > 0) {
			--spawnAttempts;
			Room room = spawnRoom(gridMap, freeCellsList, cellsMin, cellsMax);

			if (room != null) {
				rooms.add(room);
				spawnedRooms += 1;
			}
		}
		return rooms;
	}

	/**
	 * Calculates how many cells a room must span to reach a minimum length of blocks.
	 * @return amount of cells, minimum 3 (path | wall | path)
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

	private static Room spawnRoom(GridMap gridMap, List<Vec2> freeCells, int cellsMin, int cellsMax) {
		int sizeX = rndRoomSize(cellsMin, cellsMax);
		int sizeZ = rndRoomSize(cellsMin, cellsMax);
		Vec2 rndCell = freeCells.get(rnd.nextInt(freeCells.size()));
		boolean isFree = isRoomFree(gridMap, rndCell.getX(), rndCell.getZ(), sizeX, sizeZ);

		if (!isFree) {
			return null;
		}
		markRoom(gridMap, rndCell.getX(), rndCell.getZ(), sizeX, sizeZ);
		return new Room(rndCell, new Vec2(sizeX, sizeZ));
	}

	private static int rndRoomSize(int cellsMin, int cellsMax) {
		return cellsMin + rnd.nextInt((cellsMax - cellsMin) / 2) * 2;
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

	/**
	 * Mark the cells of a room as ROOM type on the grid map.
	 */
	private static void markRoom(GridMap gridMap, int startX, int startZ, int sizeX, int sizeZ) {
		for (int x = startX; x < startX + sizeX; ++x) {
			for (int z = startZ; z < startZ + sizeZ; ++z) {
				gridMap.setPathType(x, z, PathType.ROOM);
			}
		}
	}
}
