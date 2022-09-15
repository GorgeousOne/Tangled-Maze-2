package me.gorgeousone.tangledmaze.generation;

import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TerrainEditor {
	
	/**
	 * Lowers the y coordinates of the terrain map at locations where smaller block peeks like trees disturb
	 * an otherwise even area.
	 */
	public static void levelOffSpikes(MazeMap mazeMap) {
		Vec2 min = mazeMap.getMin();
		Vec2 max = mazeMap.getMax();
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				if (mazeMap.getType(x, z) == null) {
					continue;
				}
				int floorY = mazeMap.getY(x, z);
				List<Integer> neighborYs = getNeighborYs(x, z, 2, mazeMap);
				int maxNeighborY = Collections.max(neighborYs);
				
				if (floorY >= maxNeighborY + 1) {
					int averageY = Math.round(1f * neighborYs.stream().mapToInt(Integer::intValue).sum() / neighborYs.size());
					mazeMap.setY(x, z, averageY);
				}
			}
		}
	}
	
	private static List<Integer> getNeighborYs(int x, int z, int radius, MazeMap mazeMap) {
		List<Integer> neighborYs = new LinkedList<>();
		
		for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, radius)) {
			if (mazeMap.contains(neighbor)) {
				neighborYs.add(mazeMap.getY(neighbor));
			}
		}
		return neighborYs;
	}
	
	/**
	 * Lowers the feet of walls (is that how you say it?) down to level of any path next to it so the wall looks consistent everywhere.
	 */
	public static void cleanWallEdges(MazeMap mazeMap) {
		Vec2 min = mazeMap.getMin();
		Vec2 max = mazeMap.getMax();
		
		for (int x = min.getX(); x < max.getX(); ++x) {
			for (int z = min.getZ(); z < max.getZ(); ++z) {
				if (mazeMap.getType(x, z) != AreaType.WALL) {
					continue;
				}
				int floorY = mazeMap.getY(x, z);
				
				for (Vec2 neighbor : BlockUtil.getNeighbors(x, z, 1)) {
					if (mazeMap.getType(neighbor) != AreaType.PATH) {
						continue;
					}
					int neighborY = mazeMap.getY(neighbor);
					
					if (neighborY < floorY) {
						mazeMap.setY(x, z, neighborY);
						floorY = neighborY;
					}
				}
			}
		}
	}
}
