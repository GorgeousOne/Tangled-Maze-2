package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MazeSolver {

	/**
	 * Returns Set of grid ells forming a path between all exits of a maze
	 */
	public static Set<GridCell> findSolvingPath(GridMap gridMap) {
		Set<GridCell> solutionPath = new HashSet<>();
		List<ExitSegment> exits = gridMap.getExits();

		if (exits.size() < 2) {
			return solutionPath;
		}
		List<GridCell> goals = exits.stream()
				.map(e -> gridMap.getCell(gridMap.getGridPos(e.getEnd())))
				.collect(Collectors.toList());

		solutionPath.addAll(goals);
		GridCell start = goals.remove(0);
		Set<GridCell> visited = new HashSet<>(Arrays.asList(start));
		recursiveSearch(gridMap, start, null, new HashSet<>(goals), visited, solutionPath);

		//add exit segments to path solution for rendering the blocks
		int i = 0;
		for (ExitSegment exit : exits) {
			--i;
			solutionPath.add(new GridCell(exit.getMin(), exit.getMax().clone().sub(exit.getMin()), new Vec2(0, i)));
		}
		return solutionPath;
	}

	/**
	 * Recursively walks path tree to reach all goals
	 * and collects the path when returning from the recursive calls.
	 */
	private static boolean recursiveSearch(
			GridMap gridMap,
			GridCell currentCell,
			Direction lastDir,
			Set<GridCell> goals,
			Set<GridCell> visited,
			Set<GridCell> solutionPath) {
		boolean isSolution = false;
		//iterate same direction as before if possible, to avoid zigzagging inside rooms
		Direction[] dirs = lastDir == null ? Direction.CARDINALS : new Direction[] {lastDir, lastDir.getLeft(), lastDir.getRight()};

		for (Direction dir : dirs) {
			GridCell neighbor = gridMap.getCell(currentCell, dir);

			if (visited.contains(neighbor) || gridMap.getPathType(neighbor) != PathType.PAVED) {
				continue;
			}
			visited.add(neighbor);
			boolean isNeighborSolution = recursiveSearch(gridMap, neighbor, dir, goals, visited, solutionPath);
			
			if (isNeighborSolution || goals.contains(neighbor)) {
				goals.remove(neighbor);
				solutionPath.add(currentCell);
				isSolution = true;
			}
		}
		return isSolution;
	}
}
