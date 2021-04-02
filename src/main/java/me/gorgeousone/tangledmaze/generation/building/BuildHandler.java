package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Material;

public class BuildHandler {
	
	public void buildMaze(Clip maze) {
		
		MazeMap mazeMap = MazeMapFactory.createMazeMapOf(maze);
		MazeMapFactory.createPaths(mazeMap, maze.getExits(), null);
		
		Vec2 mapMin = mazeMap.getMin();
		Vec2 mapMax = mazeMap.getMax();
		
		for (int x = mapMin.getX(); x < mapMax.getX(); x++) {
			for (int z = mapMin.getZ(); z < mapMax.getZ(); z++) {
				
				AreaType type = mazeMap.getType(x, z);
				if (type == AreaType.WALL || type == AreaType.FREE) {
					maze.getWorld().getBlockAt(x, mazeMap.getY(x, z) + 1, z).setType(Material.OAK_PLANKS);
				}
			}
		}
	}
}
