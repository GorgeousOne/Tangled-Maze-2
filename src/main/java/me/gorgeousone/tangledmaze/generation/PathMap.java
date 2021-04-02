package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class PathMap {
	
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final int pathWidth;
	private final int wallWidth;
	
	private Vec2 gridMin;
	private Vec2 gridOffset;
	private MazeSegment[][] gridSegments;
	private PathType[][] segmentTypes;
	private final List<ExitSegment> exits;
	
	public PathMap(Vec2 mapMin,
	               Vec2 mapMax,
	               int pathWidth,
	               int wallWidth) {
		this.mapMin = mapMin;
		this.mapMax = mapMax;
		this.pathWidth = pathWidth;
		this.wallWidth = wallWidth;
		exits = new ArrayList<>();
	}
	
	public int getWidth() {
		return gridSegments.length;
	}
	
	public int getHeight() {
		return gridSegments[0].length;
	}
	
	public List<ExitSegment> getExits() {
		return exits;
	}
	
	public void setEntrance(Vec2 entranceLoc, Direction facing) {
		Vec2 entranceStart = calculateExitStart(entranceLoc, facing, pathWidth);
		ExitSegment entrance = new ExitSegment(entranceStart, facing, pathWidth);
		entrance.extend(wallWidth);
		exits.add(entrance);
		
		calculateGridProperties(entrance.getEnd());
		
		for (int gridX = 0; gridX < getWidth(); gridX++) {
			for (int gridZ = 0; gridZ < getHeight(); gridZ++) {
				gridSegments[gridX][gridZ] = createGridSegment(gridX, gridZ);
			}
		}
	}
	
	public void setExit(Vec2 exitBlockLoc, Direction facing) {
		Vec2 exitStart = calculateExitStart(exitBlockLoc, facing, pathWidth);
		ExitSegment exit = new ExitSegment(exitStart, facing, pathWidth);
		exit.extend(getExitDistToGrid(exit.getEnd(), facing));
		exits.add(exit);
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
	
	/**
	 * Calculates the distance in blocks that a secondary exit has to be extended by for it to reach the nearest grid path
	 *
	 * @param exitLoc current end location of the exit
	 * @param facing  direction to extend towards
	 */
	private int getExitDistToGrid(Vec2 exitLoc, Direction facing) {
		
		int gridShift;
		int exitCoord;
		
		if (facing.isCollinearX()) {
			gridShift = gridOffset.getX();
			exitCoord = exitLoc.getX();
		} else {
			gridShift = gridOffset.getZ();
			exitCoord = exitLoc.getZ();
		}
		
		int gridMeshSize = pathWidth + wallWidth;
		//calculates the offset of the exit coordinate to the path grid
		int gridDist = (exitCoord - gridShift) % gridMeshSize;
		//limits the offset/distance to positive values
		if (gridDist < 0) {
			gridDist += gridMeshSize;
		}
		//inverts the distance for exits with positive facing to extend towards the next greater path coordinate
		//not the previous lower one
		if (facing.isPositive()) {
			gridDist = gridMeshSize - gridDist;
		}
		if (gridDist < 1) {
			gridDist += gridMeshSize;
		}
		return gridDist;
	}
	
	public void setSegmentType(int gridX, int gridZ, PathType type) {
		segmentTypes[gridX][gridZ] = type;
	}
	
	/**
	 * Calculates position and count of rows and columns of the path grid
	 */
	private void calculateGridProperties(Vec2 pathStart) {
		int meshSize = pathWidth + wallWidth;
		
		gridOffset = new Vec2(
				pathStart.getX() % meshSize,
				pathStart.getZ() % meshSize);
		
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
