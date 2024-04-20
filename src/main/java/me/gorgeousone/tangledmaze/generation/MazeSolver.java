package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MazeSolver {
	
	public static List<GridCell> findSolvingPath(GridMap gridMap) {
		List<GridCell> solutionPath = new LinkedList<>();
		List<ExitSegment> exits = gridMap.getExits();

		if (exits.size() < 2) {
			return solutionPath;
		}
		List<GridCell> goals = exits.stream()
				.map(e -> gridMap.getCell(gridMap.getGridPos(e.getEnd())))
				.collect(Collectors.toList());
		
		solutionPath.addAll(goals);
		GridCell start = goals.remove(0);
		List<Vec2> visited = new LinkedList<>(Arrays.asList(start.getGridPos()));
		recursiveSearch(gridMap, start.getGridPos(), null, goals, visited, solutionPath);
		
		//add exit segments to path solution for rendering the blocks
		for (ExitSegment exit : exits) {
			solutionPath.add(new GridCell(exit.getMin(), exit.getMax().clone().sub(exit.getMin()), null));
		}
		return solutionPath;
	}
	
	private static boolean recursiveSearch(
			GridMap gridMap,
			Vec2 currentPos,
			Direction backDir,
			List<GridCell> goals,
			List<Vec2> visited,
			List<GridCell> solutionPath) {
		boolean isSolution = false;
		
		for (Direction dir : Direction.CARDINALS) {
			if (dir == backDir) {
				continue;
			}
			Vec2 neighborPos = currentPos.clone().add(dir.getVec2());
			
			if (gridMap.getPathType(neighborPos) != PathType.PAVED || visited.contains(neighborPos)) {
				continue;
			}
			visited.add(neighborPos);
			GridCell neighbor = gridMap.getCell(neighborPos);
			boolean isNeighborSolution = recursiveSearch(gridMap, neighborPos, dir.getOpposite(), goals, visited, solutionPath);
			
			if (goals.contains(neighbor) || isNeighborSolution) {
				solutionPath.add(gridMap.getCell(currentPos));
				isSolution = true;
			}
		}
		return isSolution;
	}
}
