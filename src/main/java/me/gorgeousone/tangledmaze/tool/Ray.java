package me.gorgeousone.tangledmaze.tool;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Ray {
	
	private final Location loc;
	private final Vector dir;
	
	public Ray(Player player) {
		loc = player.getEyeLocation();
		dir = loc.getDirection();
	}
	
	public Block traceBlock(int range) {
		double stepSize = 0.1;
		Vector step = dir.clone().multiply(stepSize);
		Location iter = loc.clone();
		
		for (double i = 0; i < range; i += stepSize) {
			Location last = iter.clone();
			iter.add(step);
			
			if (iter.getBlockX() == last.getBlockX() &&
			    iter.getBlockY() == last.getBlockY() &&
			    iter.getBlockZ() == last.getBlockZ()) {
				continue;
			}
			Block block = iter.getBlock();
			
			if (block.getType().isSolid()) {
				return block;
			}
		}
		return null;
	}
	
//	public Block traceBlock2(int range) {
//		Vector stepX = dir.clone().multiply(1 / Math.abs(dir.getX()));
//		Vector stepY = dir.clone().multiply(1 / Math.abs(dir.getY()));
//		Vector stepZ = dir.clone().multiply(1 / Math.abs(dir.getZ()));
//		Location iterX = loc.clone();
//		Location iterY = loc.clone();
//		Location iterZ = loc.clone();
//		return null;
//	}
}