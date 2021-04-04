package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathTree {
	
	private final static Random RND = new Random();
	private final List<Leave> openEnds;
	private final int id;

	public PathTree(int id) {
		this.openEnds = new ArrayList<>();
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isComplete() {
		return openEnds.isEmpty();
	}
	
	public void addEnd(Leave newPathEnd) {
		openEnds.add(0, newPathEnd);
	}
	
	public void addAllEnds(List<Leave> newPathEnds) {
		openEnds.addAll(0, newPathEnds);
	}
	
	public Leave getLastEnd() {
		return openEnds.get(0);
	}
	
	public Leave getRndEnd() {
		return openEnds.get(RND.nextInt(openEnds.size()));
	}
	
	public void removeEnd(Leave pathEnd) {
		openEnds.remove(pathEnd);
	}
	
	/**
	 * Represents a segment on a path map. It has a branch id for determining the exit it descends from
	 * and parent segment and an int existDist for tracing back it's way to the exit and the distance to it
	 */
	static class Leave {
		private final Vec2 gridPos;
		private final int branchId;
		private final Leave parent;
		private final int exitDist;
		
		public Leave(Vec2 gridPos, int branchId) {
			this.gridPos = gridPos;
			this.branchId = branchId;
			parent = null;
			exitDist = 0;
		}
		
		public Leave(Vec2 gridPos, Leave parent) {
			this.branchId = parent.getBranchId();
			this.gridPos = gridPos;
			this.parent = parent;
			exitDist = parent.getExitDist() + 1;
		}
		
		public int getBranchId() {
			return branchId;
		}
		
		public Vec2 getGridPos() {
			return gridPos.clone();
		}
		
		public Leave getParent() {
			return parent;
		}
		
		public int getExitDist() {
			return exitDist;
		}
	}
}
