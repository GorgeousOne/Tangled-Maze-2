package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathGen {
	
	private static final Random random = new Random();
	private static final int maxLinkedSegmentCount = 4;
	
	public static void generatePaths(PathMap pathMap, int curliness) {
		List<PathSegment> openPathEnds = createStartSegments(pathMap.getPathStarts());
		int linkedPathSegmentCount = 0;
//		boolean lastPathGotExtended = false;
		//build maze paths until maze space is used up
		while (!openPathEnds.isEmpty()) {
			PathSegment currentPathEnd;
			
			//continue last end or choose random after n connected ones
			if (linkedPathSegmentCount < maxLinkedSegmentCount) {
				currentPathEnd = openPathEnds.get(0);
				linkedPathSegmentCount++;
			} else {
				currentPathEnd = openPathEnds.get(random.nextInt(openPathEnds.size()));
				linkedPathSegmentCount = 0;
			}
			
			List<Direction> availableDirs = getAvailableDirs(currentPathEnd, pathMap);
			
			if (availableDirs.size() < 2) {
				openPathEnds.remove(currentPathEnd);
				linkedPathSegmentCount = 0;
				
				if (availableDirs.isEmpty()) {
					continue;
				}
			}
			//choose one random available direction and create new path towards that
			Direction rndFacing = availableDirs.get(random.nextInt(availableDirs.size()));
			openPathEnds.add(pavePath(currentPathEnd, pathMap, rndFacing));
			//to make longer straight segments which will look more fancy
			List<PathSegment> newPathEnds = extendPath(currentPathEnd, pathMap, rndFacing, random.nextInt(curliness) + 1);
			openPathEnds.addAll(newPathEnds);
		}
	}
	
	private static List<PathSegment> createStartSegments(List<Vec2> pathStarts) {
		List<PathSegment> startSegments = new ArrayList<>();
		
		for (int i = 0; i < pathStarts.size(); i++) {
			startSegments.add(new PathSegment(pathStarts.get(i).clone(), i));
		}
		return startSegments;
	}
	
	/**
	 * Checks the 4 surrounding paths for available directions to create paths towards
	 * @return a list of available directions
	 */
	private static List<Direction> getAvailableDirs(PathSegment pathEnd, PathMap pathMap) {
		List<Direction> branches = new ArrayList<>();
		
		for (Direction facing : Direction.fourCardinals()) {
			Vec2 facingVec = facing.getVec2();
			Vec2 gridPos = pathEnd.getGridPos();
			
			if (pathMap.getSegmentType(gridPos.add(facingVec)) == PathType.FREE &&
			    pathMap.getSegmentType(gridPos.add(facingVec)) == PathType.FREE) {
				branches.add(facing);
			}
		}
		return branches;
	}
	
	private static PathSegment pavePath(PathSegment pathEnd, PathMap pathMap, Direction facing) {
		Vec2 newPath1 = pathEnd.getGridPos().clone().add(facing.getVec2());
		Vec2 newPath2 = newPath1.clone().add(facing.getVec2());
		
		pathMap.setSegmentType(newPath1, PathType.PAVED);
		pathMap.setSegmentType(newPath2, PathType.PAVED);
		return new PathSegment(newPath1, new PathSegment(newPath2, pathEnd));
	}
	
	/**
	 * Tries to extend the end segment of a path n times into a given direction
	 * @param pathEnd segment to extend
	 * @param facing direction to extend towards
	 * @param maxLength maximum times to extend the path
	 * @return
	 */
	private static List<PathSegment> extendPath(PathSegment pathEnd, PathMap pathMap, Direction facing, int maxLength) {
		List<PathSegment> newPathEnds = new ArrayList<>();
		Vec2 facingVec = facing.getVec2();
		Vec2 gridPos = pathEnd.getGridPos();
		
		for (int i = 1; i < maxLength; i++) {
			Vec2 extension1 = gridPos.clone().add(facingVec);
			Vec2 extension2 = extension1.clone().add(facingVec);
			
			if (pathMap.getSegmentType(extension1) == PathType.FREE &&
			    pathMap.getSegmentType(extension2) == PathType.FREE) {
				newPathEnds.add(pavePath(pathEnd, pathMap, facing));
			}else {
				break;
			}
		}
		return newPathEnds;
	}
	
	/**
	 * Represents a segment on a path map. It has a branch id for determining the exit it descends from
	 * and parent segment and an int existDist for tracing back it's way to the exit and the distance to it
	 */
	static class PathSegment {
		private final Vec2 gridPos;
		private final int branchId;
		private final PathSegment parent;
		private final int exitDist;
		
		public PathSegment(Vec2 gridPos, int branchId) {
			this.gridPos = gridPos;
			this.branchId = branchId;
			parent = null;
			exitDist = 0;
		}
		
		public PathSegment(Vec2 gridPos, PathSegment parent) {
			this.branchId = parent.getBranchId();
			this.gridPos = gridPos;
			this.parent = parent;
			exitDist = parent.getExitDist() + 1;
		}
		
		public int getBranchId() {
			return branchId;
		}
		
		public Vec2 getGridPos() {
			return gridPos.clone();
		}
		
		public PathSegment getParent() {
			return parent;
		}
		
		public int getExitDist() {
			return exitDist;
		}
	}
}
