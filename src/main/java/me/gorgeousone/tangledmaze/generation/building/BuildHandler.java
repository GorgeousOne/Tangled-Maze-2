package me.gorgeousone.tangledmaze.generation.building;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.event.MazeBuildEvent;
import me.gorgeousone.tangledmaze.generation.GridSegment;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import me.gorgeousone.tangledmaze.generation.paving.PathTree;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BuildHandler {
	
	private final HashMap<Clip, MazeBackup> mazeBackups;
	
	public BuildHandler() {
		this.mazeBackups = new HashMap<>();
	}
	
	public void disable() {
		for (Clip maze : mazeBackups.keySet()) {
			unbuildMaze(null, maze, MazePart.WALLS);
		}
	}
	
	public void buildMaze(UUID playerId, Clip maze, MazeSettings settings, MazePart mazePart) {
		if (!mazeBackups.containsKey(maze) && mazePart != MazePart.WALLS) {
			Bukkit.broadcastMessage("sry lad, walls first pls");
			return;
		}
		mazeBackups.computeIfAbsent(maze, backup -> {
			MazeMap mazeMap = MazeMapFactory.createMazeMapOf(maze, settings);
			return new MazeBackup(mazeMap);
		});
		MazeBackup backup = mazeBackups.get(maze);
		MazeMap mazeMap = backup.getMazeMap();
		Set<BlockSegment> segments;
		
		switch (mazePart) {
			case WALLS:
				segments = backup.getOrCompute(MazePart.WALLS, walls -> WallGen.genWalls(mazeMap, settings));
				break;
			case FLOOR:
				segments = backup.getOrCompute(MazePart.FLOOR, floor -> FloorGen.genFloor(mazeMap));
				break;
			default:
				Bukkit.broadcastMessage("not implemented");
				return;
		}
		Set<BlockState> backupBlocks = buildSegments(mazeMap.getWorld(), segments, settings.getPalette(mazePart));
		backup.setSegments(mazePart, segments);
		boolean isFirstBuild = backup.setBlocks(mazePart, backupBlocks);
		
		if (mazePart == MazePart.WALLS && isFirstBuild) {
			Bukkit.getPluginManager().callEvent(new MazeBuildEvent(maze, playerId));
		}
	}
	
	Random random = new Random();
	
	private Set<BlockState> buildSegments(World world, Set<BlockSegment> segments, BlockPalette palette) {
		Set<BlockState> backupBlocks = new HashSet<>();
		
		for (BlockSegment segment : segments) {
			for (BlockVec blockVec : segment.getBlocks()) {
				Block block = world.getBlockAt(blockVec.getX(), blockVec.getY(), blockVec.getZ());
				BlockType type = palette.getBlock(random.nextInt(palette.size()));
				backupBlocks.add(type.updateBlock(block, false));
			}
		}
		return backupBlocks;
	}
	
	public void unbuildMaze(UUID playerId, Clip maze, MazePart mazePart) {
		if (!mazeBackups.containsKey(maze)) {
			throw new IllegalArgumentException("no built maze");
		}
		MazeBackup backup = mazeBackups.get(maze);
		
		if (mazePart == MazePart.WALLS) {
			for (MazePart builtPart : backup.getBuiltParts()) {
				unbuildMazePart(backup.getBlocks(builtPart));
			}
			mazeBackups.remove(maze);
			maze.setActive(true);
			return;
		}
		if (!backup.getBuiltParts().contains(mazePart)) {
			throw new IllegalArgumentException("didnt build " + mazePart.name());
		}
		unbuildMazePart(backup.getBlocks(mazePart));
		backup.removeMazePart(mazePart);
	}
	
	private void unbuildMazePart(Set<BlockState> backupBlocks) {
		for (BlockState block : backupBlocks) {
			block.update(true, false);
		}
	}
	
	private void displayPaths(Clip maze, MazeMap mazeMap) {
		for (PathTree tree : mazeMap.getPathTrees()) {
			float maxDist = tree.getMaxExitDist();
			
			for (GridSegment segment : tree.getSegments()) {
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
