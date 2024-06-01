package me.gorgeousone.badapplemaze.generation.paving;

import me.gorgeousone.badapplemaze.generation.GridCell;
import me.gorgeousone.badapplemaze.generation.GridMap;
import me.gorgeousone.badapplemaze.util.Direction;
import me.gorgeousone.badapplemaze.util.Vec2;
import org.bukkit.Bukkit;

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
 * The tree like path structures originating from each start are then being connected
 * at points where the paths next to each other can be linked, forming one big tree,
 * until all trees are connected. The connections are made so that the path from
 * one start to another is as long as possible.
 */
public class PathGen {
	
	public static Random RANDOM;
	private static final int maxLinkedSegmentCount = 4;
	private static int xyz = 0;

	public static void genPaths(GridMap gridMap, int curliness) {
		RANDOM = new Random(0);
		xyz = 0;

		List<PathTree> pathTrees = createPathTrees(gridMap.getPathStarts());
		List<PathTree> openPathTrees = new ArrayList<>(pathTrees);
		
		PathTree currentTree = openPathTrees.get(0);
		int linkedSegmentCount = 1;
		//keeps track of which path segments were longer than one piece
		boolean lastSegmentWasExtended = false;
		GridCell currentPathEnd;

		//adds path segments as long as free space is available
		while (!openPathTrees.isEmpty()) {
			//continues adding segments to the last path end
			if (linkedSegmentCount <= maxLinkedSegmentCount) {
				currentPathEnd = currentTree.getLastEnd();
				linkedSegmentCount++;
			//picks new random path end after n connected segments
			} else {
				currentTree = getSmallestTree(openPathTrees);
				currentPathEnd = currentTree.getRndEnd();
				linkedSegmentCount = 1;
			}
			List<Direction> availableDirs = getAvailableDirs(currentPathEnd, gridMap);
			
			if (availableDirs.size() < 2) {
				currentTree.removeEnd(currentPathEnd);
				linkedSegmentCount = 1;

				//removes a path tree if no path segments can be added anymore
				if (currentTree.isComplete()) {
					openPathTrees.remove(currentTree);
					//makes linked segment count restart next iteration
					linkedSegmentCount = maxLinkedSegmentCount + 1;
				}
				//skips path generation if no direction is available
				if (availableDirs.isEmpty()) {
					continue;
				}
			}
			//alternates between generating long and short segments
			lastSegmentWasExtended = generatePathSegment(currentPathEnd, availableDirs, gridMap, curliness, !lastSegmentWasExtended);
		}
		//connects trees from individual exits to one big maze
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
		
		for (Direction facing : Direction.CARDINALS) {
			Vec2 facingVec = facing.getVec2();
			Vec2 newSegment1 = pathEnd.getGridPos().add(facingVec);
			Vec2 newSegment2 = newSegment1.clone().add(facingVec);
			PathType pathType2 = gridMap.getPathType(newSegment2);

			if (pathType2 != PathType.FREE && pathType2 != PathType.ROOM) {
				continue;
			}
			if (gridMap.getPathType(newSegment1) == PathType.FREE) {
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
	                                           boolean tryExtendSegment) {
		Direction rndFacing = availableDirs.get(RANDOM.nextInt(availableDirs.size()));
		++xyz;
		if (xyz < 10) {
			System.out.println(currentPathEnd.getGridPos().toString() + " " + rndFacing.name());
		}
		GridCell pathConnection = gridMap.getCell(currentPathEnd, rndFacing);
		GridCell newPathEnd = gridMap.getCell(pathConnection, rndFacing);
		pavePath(currentPathEnd, pathConnection, newPathEnd, gridMap);

		if (!tryExtendSegment && curliness == 1) {
			return false;
		}
		GridCell extendedPathEnd = extendPath(newPathEnd, rndFacing, curliness, gridMap);
		boolean wasExtended = !newPathEnd.equals(extendedPathEnd);

		if (gridMap.getPathType(extendedPathEnd) != PathType.ROOM) {
			return wasExtended;
		}
		Room room = gridMap.findRoom(extendedPathEnd.getGridPos());
		if (room != null) {
			room.floodFillRoom(extendedPathEnd, gridMap);
		}
		return wasExtended;
	}
	
	/**
	 * Tries to extend the end segment of a path n times into a given direction.
	 * Stops extending before the path hits a wall or another path.
	 * Also stops extending after a paths enters a room.
	 * @param pathEnd       segment to extend
	 * @param facing        direction to extend towards
	 * @param maxExtensions maximum times to extend the path
	 * @return the extended or unextended path segment
	 */
	private static GridCell extendPath(GridCell pathEnd, Direction facing, int maxExtensions, GridMap gridMap) {
		GridCell newEnd = pathEnd;

		for (int i = 0; i < maxExtensions; ++i) {
			GridCell extension1 = gridMap.getCell(newEnd, facing);

			if (extension1 == null) {
				return newEnd;
			}
			GridCell extension2 = gridMap.getCell(extension1, facing);

			if (extension2 == null) {
				return newEnd;
			}
			PathType pathType2 = gridMap.getPathType(extension2);

			if (pathType2 != PathType.FREE && pathType2 != PathType.ROOM || gridMap.getPathType(extension1) != PathType.FREE) {
				return newEnd;
			}
			pavePath(newEnd, extension1, extension2, gridMap);
			newEnd = extension2;

			if (pathType2 == PathType.ROOM) {
				break;
			}
		}
		return newEnd;
	}

	/**
	 * Sets the path type of the next 2 grid cells to PAVED in the given direction
	 * @return the latter new path segment
	 */
	private static void pavePath(GridCell pathEnd, GridCell newPath1, GridCell newPath2, GridMap gridMap) {
		gridMap.setPathType(newPath1, PathType.PAVED);

		if (gridMap.getPathType(newPath2) != PathType.ROOM) {
			gridMap.setPathType(newPath2, PathType.PAVED);
		}
		PathTree tree = pathEnd.getTree();
		tree.addSegment(newPath1, pathEnd);
		tree.addSegment(newPath2, newPath1);
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
		
		Set<Vec2> facings = Arrays.stream(Direction.CARDINALS).map(facing -> facing.getVec2().mult(2)).collect(Collectors.toSet());
		
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
