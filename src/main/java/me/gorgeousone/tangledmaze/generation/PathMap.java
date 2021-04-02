package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

public class PathMap {
	
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final int pathWidth;
	private final int wallWidth;
	
	private Vec2 gridMin;
	private Vec2 gridOffset;
	private MazeSegment[][] gridSegments;
	private PathType[][] segmentTypes;
	
	public PathMap(Vec2 mapMin,
	               Vec2 mapMax,
	               int pathWidth,
	               int wallWidth) {
		this.mapMin = mapMin;
		this.mapMax = mapMax;
		this.pathWidth = pathWidth;
		this.wallWidth = wallWidth;
	}
	
	public int getWidth() {
		return gridSegments.length;
	}
	
	public int getHeight() {
		return gridSegments[0].length;
	}
	
	private void setEntrance(Vec2 entrance, Direction facing) {
		ExitSegment entranceSegment = createEntranceSegment(entrance, facing);
		calculateGridProperties(entranceSegment.getEnd());
		
		for (int gridX = 0; gridX < gridSegments.length; gridX++) {
			for (int gridZ = 0; gridZ < gridSegments[0].length; gridZ++) {
				gridSegments[gridX][gridZ] = createGridSegment(gridX, gridZ);
			}
		}
	}
	
	private ExitSegment createEntranceSegment(Vec2 entrance, Direction facing) {
		Vec2 exitStart = calculateExitStart(entrance, facing, pathWidth);
		ExitSegment segment = new ExitSegment(exitStart, facing, pathWidth);
		segment.extend(wallWidth);
		return segment;
	}
	
	/**
	 * Calculates the location of the exit segment start so that any exits bigger than 1 block are always
	 * aligned towards the right of the selected exit block, independent of faced direction.
	 */
	private Vec2 calculateExitStart(Vec2 exitBlockLoc, Direction facing, int exitWidth) {
		Vec2 exitStart = exitBlockLoc.clone();
		
		if (!facing.isPositive()) {
			exitStart.sub(0, exitWidth - 1);
		}
		if (facing.isPositive() ^ facing.isCollinearX()) {
			exitStart.sub(exitWidth - 1, 0);
		}
		return exitStart;
	}
	
	void setSegmentType(int gridX, int gridZ, PathType type) {
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
		
		gridMin = mapMin.clone().sub(gridOffset);
		gridMin.setX((int) Math.floor(1f * gridMin.getX() / meshSize));
		gridMin.setZ((int) Math.floor(1f * gridMin.getZ() / meshSize));
		gridMin.mult(meshSize).add(gridOffset);
		
		int gridWidth = 2 * (int) Math.ceil(1f * (mapMax.getX() - gridMin.getX()) / meshSize);
		int gridHeight = 2 * (int) Math.ceil(1f * (mapMax.getZ() - gridMin.getZ()) / meshSize);
		
		gridSegments = new MazeSegment[gridWidth][gridHeight];
		segmentTypes = new PathType[gridWidth][gridHeight];
	}
	
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
	
	public MazeSegment getSegment(int gridX, int gridZ) {
		return gridSegments[gridX][gridZ];
	}
}
