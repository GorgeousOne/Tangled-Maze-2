package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.MazeSegment;
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
	
	public static List<PathTree> generatePaths(PathMap pathMap, int curliness) {
		List<PathTree> pathTrees = createPathTrees(pathMap.getPathStarts());
		List<PathTree> openPathTrees = new ArrayList<>(pathTrees);
		
		PathTree currentTree = openPathTrees.get(0);
		int linkedSegmentCount = 1;
		boolean lastSegmentWasExtended = false;
		
		while (!openPathTrees.isEmpty()) {
			MazeSegment currentPathEnd;
			//continue last end or choose random after n connected ones
			if (linkedSegmentCount <= maxLinkedSegmentCount) {
				currentPathEnd = currentTree.getLastEnd();
				linkedSegmentCount++;
			} else {
				currentTree = getSmallestTree(openPathTrees);
				currentPathEnd = currentTree.getRndEnd();
				linkedSegmentCount = 1;
			}
			List<Direction> availableDirs = getAvailableDirs(currentPathEnd, pathMap);
			
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
			MazeSegment newPathEnd = pavePath(currentPathEnd, rndFacing, pathMap);
			//to make longer straight segments which will look more fancy
			if (!lastSegmentWasExtended) {
				lastSegmentWasExtended = extendPath(newPathEnd, rndFacing, RANDOM.nextInt(curliness - 1) + 1, pathMap);
			} else {
				lastSegmentWasExtended = false;
			}
		}
		linkPathTrees2(pathMap, pathTrees);
		return pathTrees;
	}
	
	private static List<PathTree> createPathTrees(List<MazeSegment> pathStarts) {
		List<PathTree> pathTrees = new ArrayList<>();
		
		for (MazeSegment pathStart : pathStarts) {
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
	private static List<Direction> getAvailableDirs(MazeSegment pathEnd, PathMap pathMap) {
		List<Direction> branches = new ArrayList<>();
		
		for (Direction facing : Direction.fourCardinals()) {
			Vec2 facingVec = facing.getVec2();
			Vec2 newSeg1 = pathEnd.getGridPos().add(facingVec);
			Vec2 newSeg2 = newSeg1.clone().add(facingVec);
			
			if (pathMap.getSegmentType(newSeg1) == PathType.FREE &&
			    pathMap.getSegmentType(newSeg2) == PathType.FREE) {
				branches.add(facing);
			}
		}
		return branches;
	}
	
	private static MazeSegment pavePath(MazeSegment pathEnd, Direction facing, PathMap pathMap) {
		Vec2 facingVec = facing.getVec2();
		Vec2 newPath1 = pathEnd.getGridPos().add(facingVec);
		Vec2 newPath2 = newPath1.clone().add(facingVec);
		
		pathMap.setSegmentType(newPath1, PathType.PAVED);
		pathMap.setSegmentType(newPath2, PathType.PAVED);
		
		PathTree tree = pathEnd.getTree();
		MazeSegment newSegment1 = pathMap.getSegment(newPath1);
		MazeSegment newSegment2 = pathMap.getSegment(newPath2);
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
	private static boolean extendPath(MazeSegment pathEnd, Direction facing, int maxExtensions, PathMap pathMap) {
		Vec2 facingVec = facing.getVec2();
		
		for (int i = 0; i < maxExtensions; ++i) {
			Vec2 extension1 = pathEnd.getGridPos().add(facingVec);
			Vec2 extension2 = extension1.clone().add(facingVec);
			
			if (pathMap.getSegmentType(extension1) == PathType.FREE &&
			    pathMap.getSegmentType(extension2) == PathType.FREE) {
				pathEnd = pavePath(pathEnd, facing, pathMap);
			} else {
				return i != 0;
			}
		}
		return true;
	}
	
	/**
	 * Connects the paths of the given path trees to each other trying to always find the longest path
	 * between the exit of one tree to the exit of the other one.
	 * @param pathMap to look up maze segments on
	 * @param pathTrees to connect to each other
	 */
	private static void linkPathTrees2(PathMap pathMap, List<PathTree> pathTrees) {
		while (true) {
			Set<Map.Entry<MazeSegment, MazeSegment>> treeLinks = new HashSet<>();
			
			for (PathTree tree : pathTrees) {
				addTreeLinks(pathMap, tree.getIntersections(), treeLinks);
			}
			Map.Entry<MazeSegment, MazeSegment> maxLengthLink = getMaxLengthLink(treeLinks);
			
			if (maxLengthLink == null) {
				Bukkit.broadcastMessage("oh no no");
				break;
			}
			MazeSegment keySegment = maxLengthLink.getKey();
			MazeSegment valueSegment = maxLengthLink.getValue();
			
			Vec2 linkingGridPos = keySegment.getGridPos().add(valueSegment.getGridPos()).floorDiv(2);
			MazeSegment linkSegment = pathMap.getSegment(linkingGridPos);
			pathMap.setSegmentType(linkSegment.getGridPos(), PathType.PAVED);
			
			PathTree keyTree = keySegment.getTree();
			PathTree valueTree = valueSegment.getTree();
			
			Bukkit.broadcastMessage("Join " + valueTree + ", " + linkSegment.getMin());
			
			keyTree.mergeTree(valueTree, keySegment, valueSegment, linkSegment);
			pathTrees.remove(valueTree);
		}
	}
	
	/**
	 * Finds pairs of maze segments next to each other where one segment is from one path tree and the other segment from a different one.
	 * @param pathMap to look up maze segments on
	 * @param segments all possible keys for pairs
	 * @param treeLinks collection to add the pairs to
	 */
	private static Set<Map.Entry<MazeSegment, MazeSegment>> addTreeLinks(
			PathMap pathMap,
			Set<MazeSegment> segments,
			Set<Map.Entry<MazeSegment, MazeSegment>> treeLinks) {
		
		Set<Vec2> facings = Arrays.stream(Direction.fourCardinals()).map(facing -> facing.getVec2().mult(2)).collect(Collectors.toSet());
		
		for (MazeSegment segment : segments) {
			for (Vec2 facing : facings) {
				MazeSegment neighbor = pathMap.getSegment(segment.getGridPos().add(facing));
				
				if (neighbor != null && neighbor.getTree() != null && neighbor.getTree() != segment.getTree()) {
					treeLinks.add(new AbstractMap.SimpleEntry<>(segment, neighbor));
				}
			}
		}
		return treeLinks;
	}
	
	/**
	 * Returns the pair of maze segments that cover the greatest distance between two exits if they were connected.
	 * @param treeLinks pairs of segments to look up in
	 */
	private static Map.Entry<MazeSegment, MazeSegment> getMaxLengthLink(Set<Map.Entry<MazeSegment, MazeSegment>> treeLinks) {
		int maxDist = -1;
		Map.Entry<MazeSegment, MazeSegment> maxEntry = null;
		
		for (Map.Entry<MazeSegment, MazeSegment> entry : treeLinks) {
			MazeSegment seg1 = entry.getKey();
			MazeSegment seg2 = entry.getValue();
			int dist = seg1.getTree().getExitDist(seg1) + seg2.getTree().getExitDist(seg2);
			
			if (dist > maxDist) {
				maxEntry = entry;
				maxDist = dist;
			}
		}
		return maxEntry;
	}
}
