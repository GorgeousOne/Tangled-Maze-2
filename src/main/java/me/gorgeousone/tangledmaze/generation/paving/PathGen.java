package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathGen {
	
	private static final Random random = new Random();
	private static final int maxLinkedSegmentCount = 3;
	
	public static void generatePaths(PathMap pathMap, int curliness) {
		List<PathTree> pathTrees = createPathTrees(pathMap.getPathStarts());
		int linkedSegmentCount = 1;
		
		int treeIndex = 0;
		PathTree currentTree = pathTrees.get(treeIndex);
		
		while (!pathTrees.isEmpty()) {
			PathTree.Leave currentPathEnd;
			//continue last end or choose random after n connected ones
			if (linkedSegmentCount <= maxLinkedSegmentCount) {
				currentPathEnd = currentTree.getLastEnd();
				linkedSegmentCount++;
			} else {
				treeIndex = (treeIndex + 1) % pathTrees.size();
				currentTree = pathTrees.get(treeIndex);
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
			Direction rndFacing = availableDirs.get(random.nextInt(availableDirs.size()));
			currentTree.addEnd(pavePath(currentPathEnd, pathMap, rndFacing));
			
			//to make longer straight segments which will look more fancy
			List<PathTree.Leave> newPathEnds = extendPath(currentPathEnd, pathMap, rndFacing, random.nextInt(curliness) + 1);
			currentTree.addAllEnds(newPathEnds);
		}
	}
	
	private static List<PathTree> createPathTrees(List<Vec2> pathStarts) {
		List<PathTree> pathTrees = new ArrayList<>();
		
		for (int i = 0; i < pathStarts.size(); i++) {
			PathTree tree = new PathTree(i);
			tree.addEnd(new PathTree.Leave(pathStarts.get(i), tree.getId()));
			pathTrees.add(tree);
		}
		return pathTrees;
	}
	
	/**
	 * Checks the 4 surrounding paths for available directions to create paths towards
	 * @return a list of available directions
	 */
	private static List<Direction> getAvailableDirs(PathTree.Leave pathEnd, PathMap pathMap) {
		List<Direction> branches = new ArrayList<>();
		
		for (Direction facing : Direction.fourCardinals()) {
			Vec2 facingVec = facing.getVec2();
			Vec2 gridPos = pathEnd.getGridPos();
			Vec2 newSeg1 = gridPos.clone().add(facingVec);
			Vec2 newSeg2 = newSeg1.clone().add(facingVec);
			
			if (pathMap.getSegmentType(newSeg1) == PathType.FREE &&
			    pathMap.getSegmentType(newSeg2) == PathType.FREE) {
				branches.add(facing);
			}
		}
		return branches;
	}
	
	private static PathTree.Leave pavePath(PathTree.Leave pathEnd, PathMap pathMap, Direction facing) {
		Vec2 facingVec = facing.getVec2();
		Vec2 newPath1 = pathEnd.getGridPos().clone().add(facingVec);
		Vec2 newPath2 = newPath1.clone().add(facingVec);
		
		pathMap.setSegmentType(newPath1, PathType.PAVED);
		pathMap.setSegmentType(newPath2, PathType.PAVED);
		return new PathTree.Leave(newPath2, new PathTree.Leave(newPath1, pathEnd));
	}
	
	/**
	 * Tries to extend the end segment of a path n times into a given direction
	 * @param pathEnd segment to extend
	 * @param facing direction to extend towards
	 * @param maxLength maximum times to extend the path
	 * @return
	 */
	private static List<PathTree.Leave> extendPath(PathTree.Leave pathEnd, PathMap pathMap, Direction facing, int maxLength) {
		List<PathTree.Leave> newPathEnds = new ArrayList<>();
		Vec2 facingVec = facing.getVec2();
		Vec2 gridPos = pathEnd.getGridPos();
		
		for (int i = 1; i < maxLength; i++) {
			Vec2 extension1 = gridPos.clone().add(facingVec);
			Vec2 extension2 = extension1.clone().add(facingVec);
			
			if (pathMap.getSegmentType(extension1) == PathType.FREE &&
			    pathMap.getSegmentType(extension2) == PathType.FREE) {
				newPathEnds.add(0, pavePath(pathEnd, pathMap, facing));
			}else {
				break;
			}
		}
		return newPathEnds;
	}
}
