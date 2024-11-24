package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

		GridCell start = goals.remove(0);
		solutionPath = iterativeSearch(gridMap, start, new HashSet<>(goals));
		solutionPath.addAll(goals);

		//add exit segments to path solution for rendering the blocks
		int i = 0;
		for (ExitSegment exit : exits) {
			--i;
			//create any negative grid pos to prevent hash clash
			solutionPath.add(new GridCell(exit.getMin(), exit.getMax().clone().sub(exit.getMin()), new Vec2(-1, i)));
		}
		return solutionPath;
	}
	
	private static Set<GridCell> iterativeSearch(
			GridMap gridMap,
			GridCell start,
			Set<GridCell> goals) {
		Set<GridCell> visited = new HashSet<>();
		Deque<GridCell> unvisited = new ArrayDeque<>();
		Map<GridCell, GridCell> parents = new HashMap<>();
		Map<GridCell, Direction> visitDir = new HashMap<>();
		
		visited.add(start);
		parents.put(start, null);
		
		for (Direction dir : Direction.CARDINALS) {
			GridCell neighbor = gridMap.getCell(start, dir);
			if (gridMap.getPathType(neighbor) == PathType.PAVED) {
				unvisited.add(neighbor);
				parents.put(neighbor, start);
				visitDir.put(neighbor, dir);
			}
		}
		//yeah, explore the entire fucking maze while generating same thing as a path tree
		//idk i think i'm stupid for doing it this way
		while (!unvisited.isEmpty()) {
			GridCell currentCell = unvisited.pop();
			Direction lastDir = visitDir.get(currentCell);
			Direction[] dirs = new Direction[] {lastDir, lastDir.getLeft(), lastDir.getRight()};
			
			for (Direction dir : dirs) {
				GridCell neighbor = gridMap.getCell(currentCell, dir);
				PathType pathType = gridMap.getPathType(neighbor);
				
				if (visited.contains(neighbor) || pathType != PathType.PAVED && pathType != PathType.EXIT) {
					continue;
				}
				unvisited.add(neighbor);
				visited.add(neighbor);
				parents.put(neighbor, currentCell);
				visitDir.put(neighbor, dir);
			}
		}
		Set<GridCell> solutionPath = new HashSet<>();
		
		for (GridCell goal : goals) {
			GridCell currentCell = goal;
			
			while (currentCell != null && !solutionPath.contains(currentCell)) {
				solutionPath.add(currentCell);
				currentCell = parents.get(currentCell);
			}
		}
		return solutionPath;
	}
}
