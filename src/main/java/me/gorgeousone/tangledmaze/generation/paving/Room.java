package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashSet;
import java.util.Set;

public class Room {

	private final Vec2 cellStart;
	private final Vec2 cellSize;
	private final Set<Vec2> exits;


	public Room(Vec2 cellStart, Vec2 cellSize) {
		this.cellStart = cellStart;
		this.cellSize = cellSize;
		this.exits = new HashSet<>();
	}

	private void listExits() {
		for (int x = 0; x < cellSize.getX(); x++) {
			for (int z = 0; z < cellSize.getZ(); z++) {

				if (x == 0 || x == cellSize.getX() - 1 || z == 0 || z == cellSize.getZ() - 1) {
					exits.add(cellStart.clone().add(x, z));
				}
			}
		}
	}

	public Vec2 getCellStart() {
		return cellStart;
	}

	public Vec2 getCellSize() {
		return cellSize;
	}

	public boolean contains(Vec2 gridPos) {
		return gridPos.getX() >= cellStart.getX() &&
		       gridPos.getZ() >= cellStart.getZ() &&
		       gridPos.getX() < cellStart.getX() + cellSize.getX() &&
		       gridPos.getZ() < cellStart.getZ() + cellSize.getZ();
	}

	public Set<Vec2> getExits(Vec2 excludeExit) {
		Set<Vec2> remainingExits = new HashSet<>(exits);
		remainingExits.remove(excludeExit);
		return remainingExits;
	}
}
