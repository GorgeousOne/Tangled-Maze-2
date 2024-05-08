package me.gorgeousone.tangledmaze.loot;

import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.paving.Room;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootChestLocator {

	/**
	 * @return map of non-junction grid cells in all rooms mapped to facing direction
	 */
	public static Map<Vec2, Direction> findRoomWallCells(MazeMap mazeMap) {
		GridMap gridMap = mazeMap.getPathMap();
		List<Room> rooms = gridMap.getRooms();
		Map<Vec2, Direction> blocks = new HashMap<>();

		for (Room room : rooms) {
			for (Vec2 gridPos : room.getBorderCells()) {
				if (isJunction(gridPos)) {
					continue;
				}
				GridCell cell = gridMap.getCell(gridPos);
				Direction facing = room.getWallFacing(gridPos);
				blocks.putAll(getWallPositions(cell, facing.getOpposite(), mazeMap));
			}
		}
		return blocks;
	}

	private static boolean isJunction(Vec2 gridPos) {
		return gridPos.getX() % 2 == 0 && gridPos.getZ() % 2 == 0;
	}

	private static Map<Vec2, Direction> getWallPositions(GridCell cell, Direction direction, MazeMap mazeMap) {
		Map<Vec2, Direction> wallBlocks = new HashMap<>();

		for (Vec2 pos : cell.getWalls(direction)) {
			wallBlocks.put(pos, direction);
		}
		return wallBlocks;
	}
}
