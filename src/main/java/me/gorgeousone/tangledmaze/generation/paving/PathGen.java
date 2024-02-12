package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class to generate paths in a grid map with a variation of the Prim's algorithm.
 * The algorithm starts at the user defined maze exits/starts. It picks a random exit/start
 * and extends the path 1 to n times in a random direction. Each extension has a length
 * between 1 and curliness.
 * Then a new random start/open end is picked and the process is repeated until no more
 * paths can be placed.
 * The tree like path structures originating from each start are then connected to
 * each other at points where they paths next to each other can be linked, forming one big tree,
 * until all trees are connected. The connections are made so that the path from
 * one start to another is as long as possible.
 */
public class PathGen {
	
	private static final Random RANDOM = new Random();
	private static final int maxLinkedSegmentCount = 4;

	public static void genPaths(GridMap gridMap, int curliness) {
		List<PathTree> pathTrees = createPathTrees(gridMap.getPathStarts());
		List<PathTree> openPathTrees = new ArrayList<>(pathTrees);
		
		PathTree currentTree = openPathTrees.get(0);
		int linkedSegmentCount = 1;
		boolean lastSegmentWasExtended = false;
		GridCell currentPathEnd;
		
		while (!openPathTrees.isEmpty()) {
			//continues generating at last path end or picks random after n connected segments
			if (linkedSegmentCount <= maxLinkedSegmentCount) {
				currentPathEnd = currentTree.getLastEnd();
				linkedSegmentCount++;
			} else {
				currentTree = getSmallestTree(openPathTrees);
				currentPathEnd = currentTree.getRndEnd();
				linkedSegmentCount = 1;
			}
			List<Direction> availableDirs = getAvailableDirs(currentPathEnd, gridMap);
			
			if (availableDirs.size() < 2) {
				currentTree.removeEnd(currentPathEnd);
				linkedSegmentCount = 1;
				
				if (currentTree.isComplete()) {
					openPathTrees.remove(currentTree);
					linkedSegmentCount = maxLinkedSegmentCount + 1;
				}
				if (availableDirs.isEmpty()) {
					continue;
				}
			}
			lastSegmentWasExtended = generatePathSegment(currentPathEnd, availableDirs, gridMap, curliness, lastSegmentWasExtended);
		}
		linkPathTrees(gridMap, pathTrees);
	}
	
	/**
	 * Returns a list of PathTrees each starting at a different path start
	 */
	private static List<PathTree> createPathTrees(List<GridCell> pathStarts) {
		List<PathTree> pathTrees = new ArrayList<>();
		
		for (GridCell pathStart : pathStarts) {
			PathTree tree = new PathTree();
			tree.addSegment(pathStart, null);
			pathTrees.add(tree);
		}
		return pathTrees;
	}
	
	private static PathTree getSmallestTree(List<PathTree> pathTrees) {
		return pathTrees.stream().min(Comparator.comparingInt(PathTree::size)).orElse(null);
	}
	
	/**
	 * Checks the 4 surrounding paths for available directions to create paths towards
	 * @return a list of available directions
	 */
	private static List<Direction> getAvailableDirs(GridCell pathEnd, GridMap gridMap) {
		List<Direction> branches = new ArrayList<>();
		
		for (Direction facing : Direction.fourCardinals()) {
			Vec2 facingVec = facing.getVec2();
			Vec2 newSeg1 = pathEnd.getGridPos().add(facingVec);
			Vec2 newSeg2 = newSeg1.clone().add(facingVec);
			
			if (gridMap.getPathType(newSeg1) == PathType.FREE &&
			    gridMap.getPathType(newSeg2) == PathType.FREE) {
				branches.add(facing);
			}
		}
		return branches;
	}
	
	/**
	 * Creates path segment on the grid map and extends it if possible
	 * @return true if the path segment extended at least once
	 */
	private static boolean generatePathSegment(GridCell currentPathEnd,
	                                           List<Direction> availableDirs,
	                                           GridMap gridMap,
	                                           int curliness,
	                                           boolean lastSegmentWasExtended) {
		Direction rndFacing = availableDirs.get(RANDOM.nextInt(availableDirs.size()));
		GridCell newPathEnd = pavePath(currentPathEnd, rndFacing, gridMap);
		
		if (!lastSegmentWasExtended && curliness > 1) {
			return extendPath(newPathEnd, rndFacing, RANDOM.nextInt(curliness - 1) + 1, gridMap);
		}
		return false;
	}
	
	/**
	 * Tries to extend the end segment of a path n times into a given direction
	 * @param pathEnd       segment to extend
	 * @param facing        direction to extend towards
	 * @param maxExtensions maximum times to extend the path
	 */
	private static boolean extendPath(GridCell pathEnd, Direction facing, int maxExtensions, GridMap gridMap) {
		Vec2 facingVec = facing.getVec2();
		
		for (int i = 0; i < maxExtensions; ++i) {
			Vec2 extension1 = pathEnd.getGridPos().add(facingVec);
			Vec2 extension2 = extension1.clone().add(facingVec);
			
			if (gridMap.getPathType(extension1) == PathType.FREE &&
			    gridMap.getPathType(extension2) == PathType.FREE) {
				pathEnd = pavePath(pathEnd, facing, gridMap);
			} else {
				return i != 0;
			}
		}
		return true;
	}
	
	/**
	 * Sets the path type of the next 2 grid cells to PAVED in the given direction
	 * @return the latter new path segment
	 */
	private static GridCell pavePath(GridCell pathEnd, Direction facing, GridMap gridMap) {
		Vec2 facingVec = facing.getVec2();
		Vec2 newPath1 = pathEnd.getGridPos().add(facingVec);
		Vec2 newPath2 = newPath1.clone().add(facingVec);
		
		gridMap.setPathType(newPath1, PathType.PAVED);
		gridMap.setPathType(newPath2, PathType.PAVED);
		
		PathTree tree = pathEnd.getTree();
		GridCell newSegment1 = gridMap.getCell(newPath1);
		GridCell newSegment2 = gridMap.getCell(newPath2);
		tree.addSegment(newSegment1, pathEnd);
		tree.addSegment(newSegment2, newSegment1);
		return newSegment2;
	}
	
	/**
	 * Connects the paths of the given path trees to each other trying to always find the longest path
	 * between the exit of one tree to the exit of the other one.
	 * @param gridMap   to look up maze segments on
	 * @param pathTrees to connect to each other
	 */
	private static void linkPathTrees(GridMap gridMap, List<PathTree> pathTrees) {
		while (true) {
			Set<Map.Entry<GridCell, GridCell>> treeLinks = new HashSet<>();
			
			for (PathTree tree : pathTrees) {
				listTreeLinks(gridMap, tree.getJunctions(), treeLinks);
			}
			Map.Entry<GridCell, GridCell> maxLengthLink = getMaxLengthLink(treeLinks);
			
			if (maxLengthLink == null) {
				break;
			}
			GridCell keySegment = maxLengthLink.getKey();
			GridCell valueSegment = maxLengthLink.getValue();
			
			//get the segment between the two segments
			Vec2 linkingGridPos = keySegment.getGridPos().add(valueSegment.getGridPos()).floorDiv(2);
			GridCell linkSegment = gridMap.getCell(linkingGridPos);
			//set the linking segment to PAVED
			gridMap.setPathType(linkSegment.getGridPos(), PathType.PAVED);
			
			PathTree keyTree = keySegment.getTree();
			PathTree valueTree = valueSegment.getTree();
			
			keyTree.mergeTree(valueTree, keySegment, valueSegment, linkSegment);
			pathTrees.remove(valueTree);
		}
	}
	
	/**
	 * Finds pairs of maze segments next to each other where one segment is from one path tree
	 * and the other segment from a different one.
	 * @param gridMap   to look up maze segments on
	 * @param cells     all possible keys for pairs
	 * @param treeLinks collection to add the pairs to
	 */
	private static void listTreeLinks(
			GridMap gridMap,
			Set<GridCell> cells,
			Set<Map.Entry<GridCell, GridCell>> treeLinks) {
		
		Set<Vec2> facings = Arrays.stream(Direction.fourCardinals()).map(facing -> facing.getVec2().mult(2)).collect(Collectors.toSet());
		
		for (GridCell cell : cells) {
			for (Vec2 facing : facings) {
				GridCell neighbor = gridMap.getCell(cell.getGridPos().add(facing));
				
				if (neighbor != null && neighbor.getTree() != null && neighbor.getTree() != cell.getTree()) {
					treeLinks.add(new AbstractMap.SimpleEntry<>(cell, neighbor));
				}
			}
		}
	}
	
	/**
	 * Returns the pair of maze segments that cover the greatest distance between two exits if they were connected.
	 * @param treeLinks pairs of segments to look up in
	 */
	private static Map.Entry<GridCell, GridCell> getMaxLengthLink(Set<Map.Entry<GridCell, GridCell>> treeLinks) {
		int maxDist = -1;
		Map.Entry<GridCell, GridCell> maxEntry = null;
		
		for (Map.Entry<GridCell, GridCell> entry : treeLinks) {
			GridCell seg1 = entry.getKey();
			GridCell seg2 = entry.getValue();
			int dist = seg1.getTree().getExitDist(seg1) + seg2.getTree().getExitDist(seg2);
			
			if (dist > maxDist) {
				maxEntry = entry;
				maxDist = dist;
			}
		}
		return maxEntry;
	}
}
