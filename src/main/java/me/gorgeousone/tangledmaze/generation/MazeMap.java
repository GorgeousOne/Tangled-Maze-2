package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.Vec2;

public class MazeMap {
	
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final AreaType[][] areaMap;
	private final int[][] terrainMap;
	
	public MazeMap(Vec2 min, Vec2 max) {
		this.mapMin = min;
		this.mapMax = max;
		
		int sizeX = max.getX() - min.getX() + 1;
		int sizeZ = max.getZ() - min.getZ() + 1;
		areaMap = new AreaType[sizeX][sizeZ];
		terrainMap = new int[sizeX][sizeZ];
	}
	
	public Vec2 getMin() {
		return mapMin.clone();
	}
	
	public Vec2 getMax() {
		return mapMax.clone();
	}
	
	AreaType getType(Vec2 loc) {
		return getType(loc.getX(), loc.getZ());
	}
	
	AreaType getType(int x, int z) {
		return areaMap[x - mapMin.getX()][z - mapMin.getZ()];
	}
	
	void setType(Vec2 loc, AreaType type) {
		setType(loc.getX(), loc.getZ(), type);
	}
	
	void setType(int x, int z, AreaType type) {
		areaMap[x - mapMin.getX()][z - mapMin.getZ()] = type;
	}
	
	public void setY(Vec2 loc, int y) {
		setY(loc.getX(), loc.getZ(), y);
	}
	
	public void setY(int x, int z, int y) {
		terrainMap[x - mapMin.getX()][z - mapMin.getZ()] = y;
	}
	
	public int getY(int x, int z) {
		return terrainMap[x - mapMin.getX()][z - mapMin.getZ()];
	}
}
