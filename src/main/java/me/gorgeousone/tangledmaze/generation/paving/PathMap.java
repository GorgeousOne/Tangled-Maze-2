package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.PathSegment;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class PathMap {
	
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final int pathWidth;
	private final int wallWidth;
	private final int gridMeshSize;
	
	private Vec2 gridMin;
	private Vec2 gridOffset;
	private PathSegment[][] gridSegments;
	private PathType[][] segmentTypes;
	private final List<ExitSegment> exits;
	private final List<PathSegment> pathStarts;
	
	public PathMap(Vec2 mapMin,
	               Vec2 mapMax,
	               int pathWidth,
	               int wallWidth) {
		this.mapMin = mapMin;
		this.mapMax = mapMax;
		this.pathWidth = pathWidth;
		this.wallWidth = wallWidth;
		gridMeshSize = pathWidth + wallWidth;
		exits = new ArrayList<>();
		pathStarts = new ArrayList<>();
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
	
	public List<PathSegment> getPathStarts() {
		return pathStarts;
	}
	
	public Vec2 getGridPos(Vec2 loc) {
		Vec2 gridPos = loc.clone().sub(gridMin).floorDiv(gridMeshSize).mult(2);
		gridPos.add(loc.clone().sub(gridMin).floorMod(gridMeshSize).floorDiv(pathWidth));
		return gridPos;
	}
	
	public PathSegment getSegment(Vec2 gridPos) {
		return getSegment(gridPos.getX(), gridPos.getZ());
	}
	
	public PathSegment getSegment(int gridX, int gridZ) {
		if (contains(gridX, gridZ)) {
			return gridSegments[gridX][gridZ];
		}
		return null;
	}
	
	public PathType getSegmentType(Vec2 gridPos) {
		return getSegmentType(gridPos.getX(), gridPos.getZ());
	}
	
	public PathType getSegmentType(int gridX, int gridZ) {
		if (!contains(gridX, gridZ)) {
			return PathType.BLOCKED;
		}
		return segmentTypes[gridX][gridZ];
	}
	
	public boolean contains(int gridX, int gridZ) {
		return gridX >= 0 && gridX < getWidth() &&
		       gridZ >= 0 && gridZ < getHeight();
	}
	
	public void setSegmentType(Vec2 gridPos, PathType type) {
		setSegmentType(gridPos.getX(), gridPos.getZ(), type);
	}
	
	public void setSegmentType(int gridX, int gridZ, PathType type) {
		segmentTypes[gridX][gridZ] = type;
	}
	
	public void setEntrance(Vec2 entranceLoc, Direction facing) {
		Vec2 entranceStart = calculateExitStart(entranceLoc, facing, pathWidth);
		ExitSegment entrance = new ExitSegment(entranceStart, facing, pathWidth);
		entrance.extend(wallWidth);
		exits.add(entrance);
		
		Vec2 entranceEnd = entrance.getEnd();
		calculateGridProperties(entranceEnd);
		
		for (int gridX = 0; gridX < getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < getHeight(); ++gridZ) {
				gridSegments[gridX][gridZ] = createGridSegment(gridX, gridZ);
				segmentTypes[gridX][gridZ] = PathType.FREE;
			}
		}
		Vec2 entranceGridPos = getGridPos(entranceEnd);
		setSegmentType(entranceGridPos, PathType.PAVED);
		pathStarts.add(getSegment(entranceGridPos));
	}
	
	public void setExit(Vec2 exitBlockLoc, Direction facing) {
		Vec2 exitStart = calculateExitStart(exitBlockLoc, facing, pathWidth);
		ExitSegment exit = new ExitSegment(exitStart, facing, pathWidth);
		exit.extend(getExitDistToGrid(exit.getEnd(), facing, false));
		
		Vec2 exitEnd = exit.getEnd();
		Direction left = facing.getLeft();
		Direction right = facing.getRight();
		
		ExitSegment leftTurn = new ExitSegment(exitEnd, left, pathWidth);
		ExitSegment rightTurn = new ExitSegment(exitEnd, right, pathWidth);
		leftTurn.extend(getExitDistToGrid(leftTurn.getEnd(), left, true));
		rightTurn.extend(getExitDistToGrid(rightTurn.getEnd(), right, true));
		
		boolean leftIsFree = getSegmentType(getGridPos(leftTurn.getEnd())) != PathType.BLOCKED;
		boolean rightIsFree = getSegmentType(getGridPos(rightTurn.getEnd())) != PathType.BLOCKED;
		
		if (!leftIsFree && !rightIsFree) {
			return;
		}
		ExitSegment chosenTurn;
		
		if (leftIsFree && rightIsFree) {
			boolean chooseLeft = leftTurn.length() > rightTurn.length();
			chosenTurn = chooseLeft ? leftTurn : rightTurn;
			ExitSegment otherTurn = chooseLeft ? rightTurn : leftTurn;
			
			//stops paths from reaching an exit from the opposite side
			if (otherTurn.length() > otherTurn.width() && otherTurn.length() < 2 * otherTurn.width() + 1) {
				setSegmentType(getGridPos(otherTurn.getEnd()), PathType.BLOCKED);
			}
		} else {
			chosenTurn = leftIsFree ? leftTurn : rightTurn;
		}
		exits.add(exit);
		exits.add(chosenTurn);
		Vec2 endGridPos = getGridPos(chosenTurn.getEnd());
		setSegmentType(endGridPos, PathType.PAVED);
		pathStarts.add(getSegment(endGridPos));
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
	private int getExitDistToGrid(Vec2 exitLoc, Direction facing, boolean allowZero) {
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
		int gridDist = Math.floorMod(exitCoord - gridShift, gridMeshSize);
		
		if (facing.isPositive()) {
			gridDist = (gridMeshSize - gridDist) % gridMeshSize;
		}
		if (!allowZero && gridDist == 0) {
			gridDist += gridMeshSize;
		}
		return gridDist;
	}
	
	/**
	 * Calculates position and count of rows and columns of the path grid
	 */
	private void calculateGridProperties(Vec2 pathStart) {
		gridOffset = new Vec2(
				pathStart.getX() % gridMeshSize,
				pathStart.getZ() % gridMeshSize);
		
		gridMin = mapMin.clone().sub(gridOffset);
		gridMin.floorDiv(gridMeshSize).mult(gridMeshSize);
		gridMin.add(gridOffset);
		
		int gridWidth = 2 * (int) Math.ceil(1f * (mapMax.getX() - gridMin.getX()) / gridMeshSize);
		int gridHeight = 2 * (int) Math.ceil(1f * (mapMax.getZ() - gridMin.getZ()) / gridMeshSize);
		
		gridSegments = new PathSegment[gridWidth][gridHeight];
		segmentTypes = new PathType[gridWidth][gridHeight];
	}
	
	private PathSegment createGridSegment(int gridX, int gridZ) {
		Vec2 segmentStart = gridMin.clone();
		
		segmentStart.add(
				(gridX / 2) * gridMeshSize,
				(gridZ / 2) * gridMeshSize);
		segmentStart.add(
				(gridX % 2) * pathWidth,
				(gridZ % 2) * pathWidth);
		Vec2 segmentSize = new Vec2(
				gridX % 2 == 0 ? pathWidth : wallWidth,
				gridZ % 2 == 0 ? pathWidth : wallWidth);
		
		return new PathSegment(segmentStart, segmentSize, new Vec2(gridX, gridZ));
	}
	
	public Vec2 getBlockLoc(int gridX, int gridZ) {
		return new Vec2(
				(gridX / 2) * gridMeshSize,
				(gridZ / 2) * gridMeshSize).add(
				(gridX % 2) * pathWidth,
				(gridZ % 2) * pathWidth);
	}
}
