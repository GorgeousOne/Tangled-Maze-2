package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.block.BlockFace;

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
		return gridPos.getX() >= gridMin.getX() &&
		       gridPos.getZ() >= gridMin.getZ() &&
		       gridPos.getX() < gridMax.getX() &&
		       gridPos.getZ() < gridMax.getZ();
	}

	public Direction getWallFacing(Vec2 gridPos) {
		if (gridPos.getZ() == gridMin.getZ()) {
			return Direction.NORTH;
		} else if (gridPos.getZ() == gridMax.getZ() - 1) {
			return Direction.SOUTH;
		} else if (gridPos.getX() == gridMin.getX()) {
			return Direction.WEST;
		} else if (gridPos.getX() == gridMax.getX() - 1) {
			return Direction.EAST;
		}
		return null;
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
