package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.util.Vec2;

public class MazeMap {
	
	private final Vec2 min;
	private final Vec2 max;
	private AreaType[][] areaMap;
	private int[][] terrainMap;
	
	public MazeMap(Vec2 min, Vec2 max) {
		this.min = min;
		this.max = max;
		
		int sizeX = max.getX() - min.getX() + 1;
		int sizeZ = max.getZ() - min.getZ() + 1;
		areaMap = new AreaType[sizeX][sizeZ];
		terrainMap = new int[sizeX][sizeZ];
	}
	
	public Vec2 getMin() {
		return min;
	}
	
	public Vec2 getMax() {
		return max;
	}
	
	void setType(Vec2 loc, AreaType type) {
		setType(loc.getX(), loc.getZ(), type);
	}
	
	void setType(int x, int z, AreaType type) {
		areaMap[x - min.getX()][z - min.getZ()] = type;
	}
	
	public void setY(Vec2 loc, int y) {
		setY(loc.getX(), loc.getZ(), y);
	}
	
	public void setY(int x, int z, int y) {
		terrainMap[x - min.getX()][z - min.getZ()] = y;
	}
}
