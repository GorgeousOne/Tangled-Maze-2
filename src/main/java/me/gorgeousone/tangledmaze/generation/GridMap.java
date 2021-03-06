package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that devides the area of a maze map into a grid of cells where path and wall segments can be identified and generated more easily.
 */
public class GridMap {
	
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final int pathWidth;
	private final int wallWidth;
	private final int gridMeshSize;
	
	private Vec2 gridMin;
	private Vec2 gridOffset;
	private GridCell[][] gridCells;
	
	private PathType[][] pathTypes;
	private int[][] floorYs;
	private int[][] wallYs;
	
	private final List<ExitSegment> exits;
	private final List<GridCell> pathStarts;
	
	public GridMap(Vec2 mapMin,
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
		return gridCells.length;
	}
	
	public int getHeight() {
		return gridCells[0].length;
	}
	
	public List<ExitSegment> getExits() {
		return exits;
	}
	
	public List<GridCell> getPathStarts() {
		return pathStarts;
	}
	
	/**
	 * Returns the grid coordinates of the grid cell that is located at this world location
	 */
	public Vec2 getGridPos(Vec2 loc) {
		Vec2 gridPos = loc.clone().sub(gridMin).floorDiv(gridMeshSize).mult(2);
		gridPos.add(loc.clone().sub(gridMin).floorMod(gridMeshSize).floorDiv(pathWidth));
		return gridPos;
	}
	
	public GridCell getCell(Vec2 gridPos) {
		return getCell(gridPos.getX(), gridPos.getZ());
	}
	
	public GridCell getCell(int gridX, int gridZ) {
		if (contains(gridX, gridZ)) {
			return gridCells[gridX][gridZ];
		}
		return null;
	}
	
	public PathType getPathType(Vec2 gridPos) {
		return getPathType(gridPos.getX(), gridPos.getZ());
	}
	
	public PathType getPathType(int gridX, int gridZ) {
		if (!contains(gridX, gridZ)) {
			return PathType.BLOCKED;
		}
		return pathTypes[gridX][gridZ];
	}
	
	public void setPathType(Vec2 gridPos, PathType type) {
		setPathType(gridPos.getX(), gridPos.getZ(), type);
	}
	
	public int getFloorY(Vec2 gridPos) {
		return getFloorY(gridPos.getX(), gridPos.getZ());
	}
	
	/**
	 * Returns the maximum floor height for the whole grid cell
	 */
	public int getFloorY(int gridX, int gridZ) {
		if (!contains(gridX, gridZ)) {
			return -1;
		}
		return floorYs[gridX][gridZ];
	}
	
	public void setFloorY(Vec2 gridPos, int y) {
		setFloorY(gridPos.getX(), gridPos.getZ(), y);
	}
	
	public void setFloorY(int gridX, int gridZ, int y) {
		floorYs[gridX][gridZ] = y;
	}
	
	public int getWallY(Vec2 gridPos) {
		return getWallY(gridPos.getX(), gridPos.getZ());
	}
	
	public int getWallY(Vec2 gridPos, int wallHeight) {
		return getWallY(gridPos.getX(), gridPos.getZ(), wallHeight);
	}
	
	/**
	 * Returns the y height of a wall segment in the grid. Returns floorY + wallHeight if not set
	 */
	public int getWallY(int gridX, int gridZ, int wallHeight) {
		int wallY = getWallY(gridX, gridZ);
		
		if (wallY == 0) {
			wallY = getFloorY(gridX, gridZ) + wallHeight;
		}
		return wallY;
	}
	
	/**
	 * Returns the y height of a wall segment in the grid. Returns 0 if not set
	 */
	public int getWallY(int gridX, int gridZ) {
		if (!contains(gridX, gridZ)) {
			return -1;
		}
		return wallYs[gridX][gridZ];
	}
	
	public void setWallY(int gridX, int gridZ, int y) {
		wallYs[gridX][gridZ] = y;
	}
	
	public void setPathType(int gridX, int gridZ, PathType type) {
		pathTypes[gridX][gridZ] = type;
	}
	
	public boolean contains(int gridX, int gridZ) {
		return gridX >= 0 && gridX < getWidth() &&
		       gridZ >= 0 && gridZ < getHeight();
	}
	
	/**
	 * Sets the entrance segment of the grid map and calculates all the grid properties based on that
	 */
	public void setEntrance(Vec2 entranceLoc, Direction facing) {
		Vec2 entranceStart = calculateExitStart(entranceLoc, facing, pathWidth);
		ExitSegment entrance = new ExitSegment(entranceStart, facing, pathWidth);
		entrance.extend(wallWidth);
		exits.add(entrance);
		
		Vec2 entranceEnd = entrance.getEnd();
		calculateGridProperties(entranceEnd);
		
		Vec2 entranceGridPos = getGridPos(entranceEnd);
		setPathType(entranceGridPos, PathType.PAVED);
		pathStarts.add(getCell(entranceGridPos));
	}
	
	/**
	 * Creates an exit segment extending to the next grid cell.
	 * Then optionally creates a left or right turn in order to perfectly end on a conjunction
	 */
	public void setExit(Vec2 exitBlockLoc, Direction facing) {
		Vec2 exitStart = calculateExitStart(exitBlockLoc, facing, pathWidth);
		ExitSegment exit = new ExitSegment(exitStart, facing, pathWidth);
		exit.extend(getDistToPathGrid(exit.getEnd(), facing, false));
		
		Vec2 exitEnd = exit.getEnd();
		Direction left = facing.getLeft();
		Direction right = facing.getRight();
		
		ExitSegment leftTurn = new ExitSegment(exitEnd, left, pathWidth);
		ExitSegment rightTurn = new ExitSegment(exitEnd, right, pathWidth);
		leftTurn.extend(getDistToPathGrid(leftTurn.getEnd(), left, true));
		rightTurn.extend(getDistToPathGrid(rightTurn.getEnd(), right, true));
		
		boolean leftIsFree = getPathType(getGridPos(leftTurn.getEnd())) != PathType.BLOCKED;
		boolean rightIsFree = getPathType(getGridPos(rightTurn.getEnd())) != PathType.BLOCKED;
		
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
				setPathType(getGridPos(otherTurn.getEnd()), PathType.BLOCKED);
			}
		} else {
			chosenTurn = leftIsFree ? leftTurn : rightTurn;
		}
		exits.add(exit);
		exits.add(chosenTurn);
		Vec2 endGridPos = getGridPos(chosenTurn.getEnd());
		setPathType(endGridPos, PathType.PAVED);
		pathStarts.add(getCell(endGridPos));
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
	 * @param exitLoc   current end location of the exit
	 * @param facing    direction to extend towards
	 * @param allowZero set false if returned distance must be greater than 0
	 */
	private int getDistToPathGrid(Vec2 exitLoc, Direction facing, boolean allowZero) {
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
		
		
		createGridCells(gridWidth, gridHeight);
	}
	
	private void createGridCells(int gridWidth, int gridHeight) {
		gridCells = new GridCell[gridWidth][gridHeight];
		pathTypes = new PathType[gridWidth][gridHeight];
		floorYs = new int[gridWidth][gridHeight];
		wallYs = new int[gridWidth][gridHeight];
		
		for (int gridX = 0; gridX < getWidth(); ++gridX) {
			for (int gridZ = 0; gridZ < getHeight(); ++gridZ) {
				gridCells[gridX][gridZ] = createGridSegment(gridX, gridZ);
				pathTypes[gridX][gridZ] = PathType.FREE;
			}
		}
	}
	
	private GridCell createGridSegment(int gridX, int gridZ) {
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
		
		return new GridCell(segmentStart, segmentSize, new Vec2(gridX, gridZ));
	}
	
	//	public Vec2 getBlockLoc(int gridX, int gridZ) {
	//		return new Vec2(
	//				(gridX / 2) * gridMeshSize,
	//				(gridZ / 2) * gridMeshSize).add(
	//				(gridX % 2) * pathWidth,
	//				(gridZ % 2) * pathWidth);
	//	}
}
