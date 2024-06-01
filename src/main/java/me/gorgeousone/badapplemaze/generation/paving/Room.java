package me.gorgeousone.badapplemaze.generation.paving;

import me.gorgeousone.badapplemaze.generation.GridCell;
import me.gorgeousone.badapplemaze.generation.GridMap;
import me.gorgeousone.badapplemaze.util.Direction;
import me.gorgeousone.badapplemaze.util.Vec2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Room {

	private final Vec2 gridMin;
	private final Vec2 gridMax;
	private final Set<Vec2> pathCells;
	private final Set<Vec2> borderCells;

	public Room(Vec2 gridPos, Vec2 gridSize) {
		this.gridMin = gridPos.clone();
		this.gridMax = gridPos.clone().add(gridSize);
		pathCells = new HashSet<>();
		borderCells = new HashSet<>();
		listCells();
	}

	private void listCells() {
		for (int x = gridMin.getX(); x < gridMax.getX(); ++x) {
			for (int z = gridMin.getZ(); z < gridMax.getZ(); ++z) {
				Vec2 gridPos = new Vec2(x, z);
				pathCells.add(gridPos);

				if (x == gridMin.getX() || x == gridMax.getX() - 1 ||
					z == gridMin.getZ() || z == gridMax.getZ() - 1) {
					borderCells.add(gridPos);
				}
			}
		}
	}

	public Set<Vec2> getPathCells() {
		return new HashSet<>(pathCells);
	}

	public Set<Vec2> getBorderCells() {
		return new HashSet<>(borderCells);
	}

	public Vec2 getGridMin() {
		return gridMin;
	}

	public Vec2 getGridMax() {
		return gridMax;
	}

	public boolean contains(Vec2 gridPos) {
		return contains(gridPos.getX(), gridPos.getZ());
	}

	public boolean contains(int gridX, int gridZ) {
		return gridX >= gridMin.getX() && gridX < gridMax.getX() &&
				gridZ >= gridMin.getZ() && gridZ < gridMax.getZ();
	}

	public boolean borderContains(int gridX, int gridZ) {
		return gridX == gridMin.getX() || gridX == gridMax.getX() - 1 ||
				gridZ == gridMin.getZ() || gridZ == gridMax.getZ() - 1;
	}

	public void floodFillRoom(GridCell startCell, GridMap gridMap) {
		PathTree pathTree = startCell.getTree();
		List<Vec2> openEnds = new ArrayList<>();
		Set<Vec2> unvisitedCells = new HashSet<>(borderCells);
		Vec2 startPos = startCell.getGridPos();
		unvisitedCells.remove(startPos);
		openEnds.add(startPos);

		while (!unvisitedCells.isEmpty() && !openEnds.isEmpty()) {
			openEnds.sort(Comparator.comparingInt(pos -> pos.sqrDist(startPos)));
			Vec2 gridPos = openEnds.remove(0);
			GridCell cell = gridMap.getCell(gridPos);

			for (Direction facing : Direction.CARDINALS) {
				Vec2 facingVec = facing.getVec2();
				Vec2 newPos1 = gridPos.clone().add(facingVec);
				Vec2 newPos2 = newPos1.clone().add(facingVec);

				if (!unvisitedCells.contains(newPos2)) {
					continue;
				}
				GridCell roomCell = gridMap.getCell(newPos1);
				pathTree.addSegment(roomCell, cell, false, false);
				pathTree.addSegment(gridMap.getCell(newPos2), roomCell, true, false);
				openEnds.add(newPos2);
				unvisitedCells.remove(newPos2);
			}
		}
		markRoom(gridMap, PathType.PAVED);
	}

	/**
	 * Mark the cells of a room as ROOM type on the grid map.
	 */
	public void markRoom(GridMap gridMap, PathType pathType) {
		for (int x = gridMin.getX(); x < gridMax.getX(); ++x) {
			for (int z = gridMin.getZ(); z < gridMax.getZ(); ++z) {
				gridMap.setPathType(x, z, pathType);
			}
		}
	}
}
