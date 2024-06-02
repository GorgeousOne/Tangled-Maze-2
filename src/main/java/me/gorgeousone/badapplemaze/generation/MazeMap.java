package me.gorgeousone.badapplemaze.generation;

import me.gorgeousone.badapplemaze.util.Vec2;
import org.bukkit.World;

import java.util.Arrays;

/**
 * A class for laying out the final design of a maze and to finish it off to be ready for building.
 * It stores the locations of the maze blocks with their terrain height. Each location is assigned an area type
 * if it is a wall or path. A grid map is used to store the maze layout for the path generation and path finding.
 */
public class MazeMap {
	
	private final World world;
	private final Vec2 mapMin;
	private final Vec2 mapMax;
	private final AreaType[][] areaTypeMap;
	private int y;
	private GridMap gridMap;
	
	public MazeMap(World world, Vec2 min, Vec2 max, int y) {
		this.world = world;
		this.mapMin = min;
		this.mapMax = max.clone();
		
		int sizeX = mapMax.getX() - min.getX();
		int sizeZ = mapMax.getZ() - min.getZ();
		areaTypeMap = new AreaType[sizeX][sizeZ];
		this.y = y;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Vec2 getMin() {
		return mapMin.clone();
	}
	
	public Vec2 getMax() {
		return mapMax.clone();
	}
	
	public boolean contains(Vec2 loc) {
		return contains(loc.getX(), loc.getZ());
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
		return areaTypeMap[x - mapMin.getX()][z - mapMin.getZ()];
	}
	
	public void setType(Vec2 loc, AreaType type) {
		setType(loc.getX(), loc.getZ(), type);
	}
	
	public void setType(int x, int z, AreaType type) {
		areaTypeMap[x - mapMin.getX()][z - mapMin.getZ()] = type;
	}
	
	public void setType(Vec2 min, Vec2 max, AreaType type) {
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				if (contains(x, z)) {
					areaTypeMap[x - mapMin.getX()][z - mapMin.getZ()] = type;
				}
			}
		}
	}
	
	public int getY(Vec2 loc) {
		return getY(loc.getX(), loc.getZ());
	}

	/**
	 * Returns the terrain y height of the maze's floor at a given x and z position
	 */
	public int getY(int x, int z) {
		return y;
	}
	

	public void setY(int y) {
		this.y = y;
	}
	
	public GridMap getPathMap() {
		return gridMap;
	}
	
	public void setGridMap(GridMap gridMap) {
		this.gridMap = gridMap;
	}
	
	/**
	 * Switches all area types from FREE and EXIT to WALL and PATH
	 */
	public void flip() {
		for (int x = 0; x < areaTypeMap.length; ++x) {
			for (int z = 0; z < areaTypeMap[0].length; ++z) {
				AreaType type = areaTypeMap[x][z];
				
				if (type == null) {
					continue;
				}
				switch (type) {
					case FREE:
						areaTypeMap[x][z] = AreaType.WALL;
						break;
					case EXIT:
						areaTypeMap[x][z] = AreaType.PATH;
						break;
					default:
						break;
				}
			}
		}
	}
}