package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

public class PathMap {
	
	private final MazeMap terrain;
	private int pathWidth = 2;
	private int wallWidth = 4;
	
	private Vec2 gridMin;
	private Vec2 gridOffset;
	private MazeSegment[][] gridSegments;
	private PathType[][] segmentTypes;
	
	public PathMap(MazeMap terrain, int pathWidth, int wallWidth) {
		this.terrain = terrain;
		this.pathWidth = pathWidth;
		this.wallWidth = wallWidth;
	}
	
	private void setEntrance() {
		
		
		calculateGridProperties(null);
		for (int gridX = 0; gridX < gridSegments.length; gridX++) {
			for (int gridZ = 0; gridZ < gridSegments[0].length; gridZ++) {
				gridSegments[gridX][gridZ] = createGridSegment(gridX, gridZ);
			}
		}
	}
	
	public static MazeSegment createEntranceSegment(
	                                                Vec2 entrancePoint,
	                                                int pathWidth,
	                                                int wallWidth) {
		
		Direction facing = getExitFacing(entrancePoint);
		Vec2 exitStart = calculateExitStart(entrancePoint, facing, pathWidth);
		
		MazeSegment entrance = new MazeSegment(exitStart, new Vec2(pathWidth, pathWidth));
		entrance.expandLength(facing, wallWidth);
		return entrance;
	}
	
	private static Direction getExitFacing(Vec2 exit) {
		
		for (Direction dir : Direction.fourCardinals()) {
			
			Vec2 neighbor = exit.clone().add(dir.getVec2());
			
			if (terrainMap.getAreaType(neighbor) == MazeAreaType.UNDEFINED) {
				return dir;
			}
		}
		
		throw new IllegalArgumentException("This exit does not seem to touch the maze.");
	}
	
	private static Vec2 calculateExitStart(Vec2 exitPoint, Direction facing, int exitWidth) {
		
		Vec2 exitStart = exitPoint.clone();
		
		if (!facing.isXAligned()) {
			
			if (facing.isPositive()) {
				exitStart.add(-exitWidth + 1, 0);
			} else {
				exitStart.add(0, -exitWidth + 1);
			}
			
		} else if (!facing.isPositive()) {
			exitStart.add(-exitWidth + 1, -exitWidth + 1);
		}
		
		return exitStart;
	}
	
	private void setSegmentType(int gridX, int gridZ, PathType type) {
		segmentTypes[gridX][gridZ] = type;
	}
	
	/**
	 * Calculates position and count of rows and columns of the path grid
	 */
	private void calculateGridProperties(Vec2 pathStart) {
		
		int meshSize = pathWidth + wallWidth;
		//		int offX = (min.getX() - pathStart.getX()) % meshSize;
		//		int offZ = (min.getZ() - pathStart.getZ()) % meshSize;
		
		gridOffset = new Vec2(
				pathStart.getX() % meshSize,
				pathStart.getZ() % meshSize);
		
		//		gridMin = new Vec2(
		//				(int) Math.floor(1f * (mapMin.getX() - gridOffset.getX()) / meshSize) * meshSize + gridOffset.getX(),
		//				(int) Math.floor(1f * (mapMin.getZ() - gridOffset.getZ()) / meshSize) * meshSize + gridOffset.getZ());
		
		gridMin = terrain.getMin().sub(gridOffset);
		gridMin.setX((int) Math.floor(1f * gridMin.getX() / meshSize));
		gridMin.setZ((int) Math.floor(1f * gridMin.getZ() / meshSize));
		gridMin.mult(meshSize).add(gridOffset);
		
		Vec2 mapMax = terrain.getMax();
		int gridWidth = 2 * (int) Math.ceil(1f * (mapMax.getX() - gridMin.getX()) / meshSize);
		int gridHeight = 2 * (int) Math.ceil(1f * (mapMax.getZ() - gridMin.getZ()) / meshSize);
		
		gridSegments = new MazeSegment[gridWidth][gridHeight];
		segmentTypes = new PathType[gridWidth][gridHeight];
	}
	
//	private void createSegments() {
//		for (int gridX = 0; gridX < gridSegments.length; gridX++) {
//			for (int gridZ = 0; gridZ < gridSegments[0].length; gridZ++) {
//				gridSegments[gridX][gridZ] = createGridSegment(gridX, gridZ);
//			}
//		}
//	}
	
	private MazeSegment createGridSegment(int gridX, int gridZ) {
		int meshSize = pathWidth + wallWidth;
		Vec2 segmentStart = gridMin.clone();
		
		segmentStart.add(
				(gridX / 2) * meshSize,
				(gridZ / 2) * meshSize);
		segmentStart.add(
				(gridX % 2) * pathWidth,
				(gridZ % 2) * pathWidth);
		
		Vec2 segmentSize = new Vec2(
				gridX % 2 == 0 ? pathWidth : wallWidth,
				gridZ % 2 == 0 ? pathWidth : wallWidth);
		
		return new MazeSegment(segmentStart, segmentSize);
	}
	
	private void setSegmentTypes() {
		for (int gridX = 0; gridX < gridSegments.length; gridX++) {
			for (int gridZ = 0; gridZ < gridSegments[0].length; gridZ++) {
				PathType type;
				if (!isSegmentFree(gridSegments[gridX][gridZ]) || (gridX % 2 != 0 && gridZ % 2 != 0)) {
					type = PathType.BLOCKED;
				} else if (gridX % 2 == 0 && gridZ % 2 == 0) {
					type = PathType.INTERSECTION;
				} else {
					type = PathType.PATH;
				}
				setSegmentType(gridX, gridZ, type);
			}
		}
	}
	
	private boolean isSegmentFree(MazeSegment segment) {
		Vec2 segMin = segment.getLoc();
		Vec2 segMax = segment.getLoc().add(segment.getSize());
		
		for (int x = segMin.getX(); x < segMax.getX(); x++) {
			for (int z = segMin.getZ(); z < segMax.getZ(); z++) {
				if (terrain.getType(z, z) != AreaType.FREE) {
					return false;
				}
			}
		}
		return true;
	}
}
