package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.MazeSegment;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathGen {
	
	private static final Random RANDOM = new Random();
	private static final int maxLinkedSegmentCount = 4;
	
	public static void generatePaths(PathMap pathMap, int curliness) {
		List<PathTree> pathTrees = createPathTrees(pathMap.getPathStarts());
		PathTree currentTree = pathTrees.get(0);
		int linkedSegmentCount = 1;
		boolean lastSegmentWasExtended = false;
		
		while (!pathTrees.isEmpty()) {
			MazeSegment currentPathEnd;
			//continue last end or choose random after n connected ones
			if (linkedSegmentCount <= maxLinkedSegmentCount) {
				currentPathEnd = currentTree.getLastEnd();
				linkedSegmentCount++;
			} else {
				currentTree = getSmallestTree(pathTrees);
				currentPathEnd = currentTree.getRndEnd();
				linkedSegmentCount = 1;
			}
			List<Direction> availableDirs = getAvailableDirs(currentPathEnd, pathMap);
			
			if (availableDirs.size() < 2) {
				currentTree.removeEnd(currentPathEnd);
				linkedSegmentCount = 1;
				
				if (currentTree.isComplete()) {
					pathTrees.remove(currentTree);
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
			}else {
				lastSegmentWasExtended = false;
			}
		}
	}
	
	private static List<PathTree> createPathTrees(List<MazeSegment> pathStarts) {
		List<PathTree> pathTrees = new ArrayList<>();
		
		for (MazeSegment pathStart : pathStarts) {
			PathTree tree = new PathTree();
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
	 * @param pathEnd segment to extend
	 * @param facing direction to extend towards
	 * @param maxExtensions maximum times to extend the path
	 */
	private static boolean extendPath(MazeSegment pathEnd, Direction facing, int maxExtensions, PathMap pathMap) {
		Vec2 facingVec = facing.getVec2();
		
		for (int i = 0; i < maxExtensions; i++) {
			Vec2 extension1 = pathEnd.getGridPos().add(facingVec);
			Vec2 extension2 = extension1.clone().add(facingVec);

			if (pathMap.getSegmentType(extension1) == PathType.FREE &&
			    pathMap.getSegmentType(extension2) == PathType.FREE) {
				pathEnd = pavePath(pathEnd, facing, pathMap);
			}else {
				return i != 0;
			}
		}
		return true;
	}
}
