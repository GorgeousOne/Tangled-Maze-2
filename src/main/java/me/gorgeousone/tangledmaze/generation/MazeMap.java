package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.generation.paving.PathMap;
import me.gorgeousone.tangledmaze.generation.paving.PathTree;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.List;

public class MazeMap {
	
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final AreaType[][] areaMap;
	private final int[][] terrainMap;
	
	public MazeMap(Vec2 min, Vec2 max) {
		this.mapMin = min;
		this.mapMax = max.add(1, 1);
		
		int sizeX = max.getX() - min.getX();
		int sizeZ = max.getZ() - min.getZ();
		areaMap = new AreaType[sizeX][sizeZ];
		terrainMap = new int[sizeX][sizeZ];
	}
	
	public Vec2 getMin() {
		return mapMin.clone();
	}
	
	public Vec2 getMax() {
		return mapMax.clone();
	}
	
	public boolean contains(int x, int z) {
		return x >= mapMin.getX() && x < mapMax.getX() &&
		       z >= mapMin.getZ() && z < mapMax.getZ();
	}
	
	public AreaType getType(Vec2 loc) {
		return getType(loc.getX(), loc.getZ());
	}
	
	public AreaType getType(int x, int z) {
		if (!contains(x, z)) {
			return null;
		}
		return areaMap[x - mapMin.getX()][z - mapMin.getZ()];
	}
	
	public void setType(Vec2 loc, AreaType type) {
		setType(loc.getX(), loc.getZ(), type);
	}
	
	public void setType(int x, int z, AreaType type) {
		areaMap[x - mapMin.getX()][z - mapMin.getZ()] = type;
	}
	
	public void setType(Vec2 min, Vec2 max, AreaType type) {
		for (int x = min.getX() - mapMin.getX(); x < max.getX() - mapMin.getX(); x++) {
			for (int z = min.getZ() - mapMin.getZ(); z < max.getZ() - mapMin.getZ(); z++) {
				areaMap[x][z] = type;
			}
		}
	}
	
	public int getY(int x, int z) {
		return terrainMap[x - mapMin.getX()][z - mapMin.getZ()];
	}
	
	public void setY(Vec2 loc, int y) {
		setY(loc.getX(), loc.getZ(), y);
	}
	
	public void setY(int x, int z, int y) {
		terrainMap[x - mapMin.getX()][z - mapMin.getZ()] = y;
	}
	
	private PathMap pathMap;
	private List<PathTree> pathTrees;
	
	public PathMap getPathMap() {
		return pathMap;
	}
	
	public void setPathMap(PathMap pathMap) {
		this.pathMap = pathMap;
	}
	
	public List<PathTree> getPathTrees() {
		return pathTrees;
	}
	
	public void setPathTrees(List<PathTree> pathTrees) {
		this.pathTrees = pathTrees;
	}
}
