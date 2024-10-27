package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.ExitSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathType;
import me.gorgeousone.tangledmaze.generation.paving.Room;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that divides the area of a maze map into a grid of cells with path type, floor ys and wall ys for each cell.
 * The size of the grid cells in blocks vary depending on the path width and wall width.
 * Junction, connecting path and wall cells form a plaid pattern.
 * (junctions: path width x path width, paths: wall x path / path x wall, walls: wall x wall).
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
	private final List<Room> rooms;

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
		rooms = new ArrayList<>();
	}

	public int getWidth() {
		return gridCells.length;
	}

	public int getHeight() {
		return gridCells[0].length;
	}

	public List<ExitSegment> getExits() {
		return new ArrayList<>(exits);
	}

	public List<GridCell> getPathStarts() {
		return pathStarts;
	}

	/**
	 * Returns the grid coordinates of the grid cell that is located at this world location
	 */
	public Vec2 getGridPos(Vec2 loc) {
		Vec2 gridPos = loc.clone().sub(gridMin).floorDiv(gridMeshSize).mult(2);
		Vec2 offset = loc.clone().sub(gridMin).floorMod(gridMeshSize);
		gridPos.add(
				offset.getX() < pathWidth ? 0 : 1,
				offset.getZ() < pathWidth ? 0 : 1);
		return gridPos;
	}

	public GridCell getCell(GridCell cell, Direction direction) {
		Vec2 neighborPos = cell.getGridPos().add(direction.getVec2());
		return getCell(neighborPos);
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

	public PathType getPathType(GridCell cell) {
		return getPathType(cell.getGridPos());
	}

	public PathType getPathType(int gridX, int gridZ) {
		if (contains(gridX, gridZ)) {
			return pathTypes[gridX][gridZ];
		}
		return null;
	}

	public void setPathType(Vec2 gridPos, PathType type) {
		setPathType(gridPos.getX(), gridPos.getZ(), type);
	}

	public void setPathType(GridCell cell, PathType type) {
		setPathType(cell.getGridPos(), type);
	}

	/**
	 * Returns the maximum floor height for the whole grid cell
	 */
	public int getFloorY(Vec2 gridPos) {
		if (!contains(gridPos.getX(), gridPos.getZ())) {
			throw new IllegalArgumentException("Floor " + gridPos.getX() + ", " + gridPos.getZ() + " out of grid map.");
		}
		return floorYs[gridPos.getX()][gridPos.getZ()];
	}

	public void setFloorY(int gridX, int gridZ, int y) {
		floorYs[gridX][gridZ] = y;
	}

	/**
	 * Returns the collective y height for all wall columns in this cell
	 * Returns 0 if not set
	 */
	public int getWallY(Vec2 gridPos) {
		if (!contains(gridPos.getX(), gridPos.getZ())) {
			throw new IllegalArgumentException("Wall " + gridPos.getX() + ", " + gridPos.getZ() + " out of grid map.");
		}
		return wallYs[gridPos.getX()][gridPos.getZ()];
	}

	/**
	 * Sets the top y coordinate of a grid...
	 */
	public void setWallY(int gridX, int gridZ, int y) {
		wallYs[gridX][gridZ] = y;
	}

	public void setPathType(int gridX, int gridZ, PathType type) {
		pathTypes[gridX][gridZ] = type;
	}

	public boolean contains(Vec2 gridPos) {
		return contains(gridPos.getX(), gridPos.getZ());
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
		setPathType(entranceGridPos, PathType.EXIT);
		pathStarts.add(getCell(entranceGridPos));
	}

	/**
	 * Creates an exit segment extending to the next grid cell.
	 * Then optionally creates a left or right turn in order to perfectly end on a path conjunction
	 */
	public void setExit(Vec2 exitBlockLoc, Direction facing) {
		Vec2 exitStart = calculateExitStart(exitBlockLoc, facing, pathWidth);
		ExitSegment exit = new ExitSegment(exitStart, facing, pathWidth);
		exit.extend(getDistToPathGrid(exit.getEnd(), facing, false));
		exits.add(exit);
		
		Vec2 exitEnd = exit.getEnd();
		Direction left = facing.getLeft();
		Direction right = facing.getRight();
		
		int leftGridDist = getDistToPathGrid(exitEnd, left, true);
		int rightGridDist = getDistToPathGrid(exitEnd, right, true);
		
		//don't add extra bends to the exit if it perfectly landed on the path grid
		if (leftGridDist == 0) {
			Vec2 endGridPos = getGridPos(exit.getEnd());
			setPathType(endGridPos, PathType.EXIT);
			pathStarts.add(getCell(endGridPos));
			return;
		}
		ExitSegment leftTurn = new ExitSegment(exitEnd, left, pathWidth);
		ExitSegment rightTurn = new ExitSegment(exitEnd, right, pathWidth);
		leftTurn.extend(leftGridDist);
		rightTurn.extend(rightGridDist);
		
		//bruh
		boolean leftIsFree = Arrays.asList(PathType.PAVED, PathType.FREE).contains(getPathType(getGridPos(leftTurn.getEnd())));
		boolean rightIsFree = Arrays.asList(PathType.PAVED, PathType.FREE).contains(getPathType(getGridPos(rightTurn.getEnd())));

		if (!leftIsFree && !rightIsFree) {
			return;
		}
		ExitSegment chosenTurn;

		if (leftIsFree && rightIsFree) {
			boolean chooseLeft = leftTurn.length() > rightTurn.length();
			chosenTurn = chooseLeft ? leftTurn : rightTurn;
			ExitSegment otherTurn = chooseLeft ? rightTurn : leftTurn;

			//places a blocked path at the opposite side of chosen turn
			//to prevent path generator to connect a second path to this exit
			if (otherTurn.length() > otherTurn.width() && otherTurn.length() < 2 * otherTurn.width() + 1) {
				setPathType(getGridPos(otherTurn.getEnd()), PathType.BLOCKED);
			}
		} else {
			chosenTurn = leftIsFree ? leftTurn : rightTurn;
		}
		exits.add(chosenTurn);
		Vec2 endGridPos = getGridPos(chosenTurn.getEnd());
		setPathType(endGridPos, PathType.EXIT);
		pathStarts.add(getCell(endGridPos));
	}

	public void addRoom(Room room) {
		rooms.add(room);
	}

	public Room findRoom(Vec2 gridPos) {
		for (Room room : rooms) {
			if (room.contains(gridPos)) {
				return room;
			}
		}
		return null;
	}

	public List<Room> getRooms() {
		return new ArrayList<>(rooms);
	}

	/**
	 * Calculates the location of the exit segment start so that any exits bigger than 1 block are always
	 * aligned towards the right of the selected exit block, independent of faced direction.
	 */
	private Vec2 calculateExitStart(Vec2 exitBlockLoc, Direction facing, int exitWidth) {
		Vec2 exitStart = exitBlockLoc.clone();
		//get offset on z axis
		if (!facing.isPositive()) {
			exitStart.sub(0, exitWidth - 1);
		}
		//get offset on x axis
		if (facing.isPositive() ^ facing.isCollinearX()) {
			exitStart.sub(exitWidth - 1, 0);
		}
		return exitStart;
	}

	/**
	 * Calculates the distance in blocks that a secondary exit has to be extended by for it to reach the nearest grid path
	 * in it's facing direction.
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
		//calculate the positive offset to the previous grid mesh start
		int gridDist = Math.floorMod(exitCoord - gridShift, gridMeshSize);
		
		//invert distance for exits that need the distance to the next mesh start
		if (facing.isPositive()) {
			gridDist = (gridMeshSize - gridDist) % gridMeshSize;
		}
		if (!allowZero && gridDist == 0) {
			return gridMeshSize;
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

	public List<Direction> getWallDirs(GridCell cell) {
		Vec2 gridPos = cell.getGridPos();
		List<Direction> wallDirs = new ArrayList<>();

		for (Direction dir : Direction.CARDINALS) {
			PathType pathType = getPathType(gridPos.clone().add(dir.getVec2()));
			if (pathType == PathType.BLOCKED || pathType == PathType.FREE) {
				wallDirs.add(dir);
			}
		}
		return wallDirs;
	}

	public boolean isDeadEnd(int x, int z) {
		int pathNeighbors = 0;

		for (Direction dir : Direction.CARDINALS) {
			PathType pathType = getPathType(x + dir.getX(), z + dir.getZ());
			if (pathType == PathType.PAVED || pathType == PathType.EXIT || pathType == PathType.ROOM) {
				++pathNeighbors;
			}
		}
		return pathNeighbors == 1;
	}
}
