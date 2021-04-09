package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import me.gorgeousone.tangledmaze.generation.PathSegment;
import me.gorgeousone.tangledmaze.generation.paving.PathTree;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BuildHandler {
	
	public void buildMaze(Clip maze, MazeSettings settings, MazePart mazePart) {
		MazeMap mazeMap = MazeMapFactory.createMazeMapOf(maze);
		MazeMapFactory.createPaths(mazeMap, maze.getExits(), settings);
		mazeMap.flip();
		TerrainEditor.improveTerrain(mazeMap);
		
		Set<WallSegment> walls = WallGen.genWalls(mazeMap, settings);
		buildWalls(mazeMap.getWorld(), walls, settings.getPalette(mazePart));
//		displayPaths(maze, mazeMap);
	}
	
	Random random = new Random();
	
	private void buildWalls(World world, Set<WallSegment> walls, BlockPalette palette) {
		for (WallSegment wall : walls) {
			for (BlockVec blockVec : wall.getBlocks()) {
				Block block = world.getBlockAt(blockVec.getX(), blockVec.getY(), blockVec.getZ());
				palette.getBlock(random.nextInt(palette.size())).updateBlock(block, false);
			}
		}
	}
	
	private void displayPaths(Clip maze, MazeMap mazeMap) {
		for (PathTree tree : mazeMap.getPathTrees()) {
			float maxDist = tree.getMaxExitDist();
			
			for (PathSegment segment : tree.getSegments()) {
				Color color = rainbowColor(tree.getExitDist(segment) / maxDist);
				Vec2 segMin = segment.getMin();
				Vec2 segMax = segment.getMax();
				
				for (int x = segMin.getX(); x < segMax.getX(); ++x) {
					for (int z = segMin.getZ(); z < segMax.getZ(); ++z) {
						Block block = maze.getWorld().getBlockAt(x, mazeMap.getY(x, z) + 1, z);
						spawnColor(block, color);
					}
				}
			}
		}
	}
	
	private Color rainbowColor(float percent) {
		float r, g, b;
		
		if (percent < 1 / 6f) {
			r = 255;
			g = 6 * percent * 255;
			b = 0;
		} else if (percent < 1 / 3f) {
			r = 255 - 6 * (percent - 1 / 6f) * 255;
			g = 255;
			b = 0;
		} else if (percent < 1 / 2f) {
			r = 0;
			g = 255;
			b = 6 * (percent - 1 / 3f) * 255;
		} else if (percent < 2 / 3f) {
			r = 0;
			g = 255 - 6 * (percent - 1 / 2f) * 255;
			b = 255;
		} else if (percent < 5 / 6f) {
			r = 6 * (percent - 2 / 3f) * 255;
			g = 0;
			b = 255;
		} else {
			r = 255;
			g = 0;
			b = 255 - 6 * (percent - 5 / 6f) * 255;
		}
		return Color.fromRGB(colorize(r), colorize(g), colorize(b));
	}
	
	private int colorize(float f) {
		return (int) Math.max(0, Math.min(255, f));
	}
	
	EulerAngle ninety = new EulerAngle(70, 0, 0);
	
	private void spawnColor(Block block, Color color) {
		Location spawnLoc = block.getLocation().add(0.5, 0, 0.5);
		ArmorStand stand = block.getWorld().spawn(spawnLoc, ArmorStand.class);
		stand.setArms(true);
		stand.setBodyPose(ninety);
		stand.setLeftArmPose(ninety);
		stand.setRightArmPose(ninety);
		stand.setVisible(false);
		
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
		meta.setColor(color);
		chest.setItemMeta(meta);
		stand.setChestplate(chest);
	}
}
