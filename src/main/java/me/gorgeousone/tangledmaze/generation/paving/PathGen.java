package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.GridMap;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Bukkit;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class PathGen {
	
	private static final Random RANDOM = new Random();
	private static final int maxLinkedSegmentCount = 4;
	
	public static List<PathTree> genPaths(GridMap gridMap, int curliness) {
		List<PathTree> pathTrees = createPathTrees(gridMap.getPathStarts());
		List<PathTree> openPathTrees = new ArrayList<>(pathTrees);
		
		PathTree currentTree = openPathTrees.get(0);
		int linkedSegmentCount = 1;
		boolean lastSegmentWasExtended = false;
		
		while (!openPathTrees.isEmpty()) {
			GridCell currentPathEnd;
			//continue last end or choose random after n connected ones
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
			//choose one random available direction and create new path towards that
			Direction rndFacing = availableDirs.get(RANDOM.nextInt(availableDirs.size()));
			GridCell newPathEnd = pavePath(currentPathEnd, rndFacing, gridMap);
			//to make longer straight segments which will look more fancy
			if (!lastSegmentWasExtended) {
				lastSegmentWasExtended = extendPath(newPathEnd, rndFacing, RANDOM.nextInt(curliness - 1) + 1, gridMap);
			} else {
				lastSegmentWasExtended = false;
			}
		}
		linkPathTrees2(gridMap, pathTrees);
		return pathTrees;
	}
	
	private static List<PathTree> createPathTrees(List<GridCell> pathStarts) {
		List<PathTree> pathTrees = new ArrayList<>();
		
		for (GridCell pathStart : pathStarts) {
			PathTree tree = new PathTree(pathStarts.indexOf(pathStart));
			tree.addSegment(pathStart, null);
			pathTrees.add(tree);
		}
		return pathTrees;
	}
	
	private static PathTree getSmallestTree(List<PathTree> pathTrees) {
		int min = Integer.MAX_VALUE;
		PathTree minTree = null;
		
		for (PathTree tree : pathTrees) {
			if (tree.size() < min) {
				minTree = tree;
				min = tree.size();
			}
		}
		return minTree;
	}
	
	/**
	 * Checks the 4 surrounding paths for available directions to create paths towards
	 *
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
	 * Tries to extend the end segment of a path n times into a given direction
	 *
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
	 * Connects the paths of the given path trees to each other trying to always find the longest path
	 * between the exit of one tree to the exit of the other one.
	 *
	 * @param gridMap   to look up maze segments on
	 * @param pathTrees to connect to each other
	 */
	private static void linkPathTrees2(GridMap gridMap, List<PathTree> pathTrees) {
		while (true) {
			Set<Map.Entry<GridCell, GridCell>> treeLinks = new HashSet<>();
			
			for (PathTree tree : pathTrees) {
				addTreeLinks(gridMap, tree.getIntersections(), treeLinks);
			}
			Map.Entry<GridCell, GridCell> maxLengthLink = getMaxLengthLink(treeLinks);
			
			if (maxLengthLink == null) {
				break;
			}
			GridCell keySegment = maxLengthLink.getKey();
			GridCell valueSegment = maxLengthLink.getValue();
			
			Vec2 linkingGridPos = keySegment.getGridPos().add(valueSegment.getGridPos()).floorDiv(2);
			GridCell linkSegment = gridMap.getCell(linkingGridPos);
			gridMap.setPathType(linkSegment.getGridPos(), PathType.PAVED);
			
			PathTree keyTree = keySegment.getTree();
			PathTree valueTree = valueSegment.getTree();
			
			Bukkit.broadcastMessage("Join " + valueTree + ", " + linkSegment.getMin());
			
			keyTree.mergeTree(valueTree, keySegment, valueSegment, linkSegment);
			pathTrees.remove(valueTree);
		}
	}
	
	/**
	 * Finds pairs of maze segments next to each other where one segment is from one path tree and the other segment from a different one.
	 *
	 * @param gridMap   to look up maze segments on
	 * @param cells  all possible keys for pairs
	 * @param treeLinks collection to add the pairs to
	 */
	private static void addTreeLinks(
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
	 *
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
