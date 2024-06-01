package me.gorgeousone.badapplemaze.generation.generator;

import me.gorgeousone.badapplemaze.generation.AreaType;
import me.gorgeousone.badapplemaze.generation.BlockCollection;
import me.gorgeousone.badapplemaze.generation.GridCell;
import me.gorgeousone.badapplemaze.generation.GridMap;
import me.gorgeousone.badapplemaze.generation.MazeMap;
import me.gorgeousone.badapplemaze.generation.paving.PathType;
import me.gorgeousone.badapplemaze.maze.MazeProperty;
import me.gorgeousone.badapplemaze.maze.MazeSettings;
import me.gorgeousone.badapplemaze.util.BlockUtil;
import me.gorgeousone.badapplemaze.util.BlockVec;
import me.gorgeousone.badapplemaze.util.Direction;
import me.gorgeousone.badapplemaze.util.Vec2;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WallBlockGen extends BlockGen {

	public static BlockCollection genWalls(MazeMap mazeMap) {
		GridMap gridMap = mazeMap.getPathMap();
		BlockCollection walls = new BlockCollection();

		for (int gridX = 0; gridX < gridMap.getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < gridMap.getHeight(); ++gridZ) {

				if (gridMap.getPathType(gridX, gridZ) != PathType.PAVED) {
					GridCell cell = gridMap.getCell(gridX, gridZ);
					addWallSegment(walls, mazeMap, gridMap, cell);
				}
			}
		}
		return walls;
	}

	/**
	 * Creates a piece of wall on the given grid segment. The height of each wall column is the same,
	 * dependent on the highest piece of path surrounding all columns together
	 *
	 * @param mazeMap for looking for wall coordinates
	 * @param cell    segment to build the wall in
	 */
	private static void addWallSegment(BlockCollection walls, MazeMap mazeMap, GridMap gridMap, GridCell cell) {
		Set<Vec2> columns = getColumns(cell, mazeMap, AreaType.WALL);
		Vec2 gridPos = cell.getGridPos();

		for (Vec2 column : columns) {
			for (int y = mazeMap.getY(column) + 1; y <= gridMap.getWallY(gridPos); ++y) {
				walls.addBlock(column.getX(), y, column.getZ());
			}
		}
	}
}