package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.GridCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Class to keep track of connections of maze paths
 */
public class PathTree {
	
	private final static Random RANDOM = new Random();
	private final List<GridCell> openEnds;
	private final Set<GridCell> cells;
	private final Set<GridCell> intersections;
	
	private int maxExitDist;
	
	public PathTree(int index) {
		openEnds = new ArrayList<>();
		cells = new HashSet<>();
		intersections = new HashSet<>();
	}
	
	public int size() {
		return cells.size();
	}
	
	public boolean isComplete() {
		return openEnds.isEmpty();
	}
	
	public void addSegment(GridCell cell, GridCell parent) {
		cell.setTree(this);
		cell.setParent(parent);
		cells.add(cell);
		
		int exitDist = getExitDist(cell);
		maxExitDist = Math.max(exitDist, maxExitDist);
		
		if (cell.gridX() % 2 == 0 && cell.gridZ() % 2 == 0) {
			intersections.add(cell);
			openEnds.add(0, cell);
		}
	}
	
	public Set<GridCell> getCells() {
		return cells;
	}
	
	public Set<GridCell> getIntersections() {
		return intersections;
	}
	
	public int getMaxExitDist() {
		return maxExitDist;
	}
	
	public int getExitDist(GridCell cell) {
		int dist = 0;
		while (cell.hasParent()) {
			++dist;
			cell = cell.getParent();
		}
		return dist;
	}
	
	public GridCell getLastEnd() {
		return openEnds.get(0);
	}
	
	public GridCell getRndEnd() {
		return openEnds.get(RANDOM.nextInt(openEnds.size()));
	}
	
	public void removeEnd(GridCell pathEnd) {
		openEnds.remove(pathEnd);
	}
	
	public void mergeTree(PathTree other, GridCell ownSegment, GridCell otherSegment, GridCell linkSegment) {
		for (GridCell cell : other.cells) {
			cell.setTree(this);
		}
		cells.addAll(other.cells);
		intersections.addAll(other.intersections);
		addSegment(linkSegment, ownSegment);
		balanceTree(linkSegment, otherSegment);
	}
	
	/**
	 *
	 * @param seg1
	 * @param seg2
	 */
	private void balanceTree(GridCell seg1, GridCell seg2) {
		int exitDist1 = getExitDist(seg1);
		int exitDist2 = getExitDist(seg2);
		int distDiff = Math.abs(exitDist2 - exitDist1);
		maxExitDist = Math.max(maxExitDist, (exitDist2 + exitDist1) / 2);
		
		GridCell furtherSeg = exitDist1 > exitDist2 ? seg1 : seg2;
		GridCell closerSeg = exitDist1 <= exitDist2 ? seg1 : seg2;
		
		for (int i = 0; i < distDiff / 2; i++) {
			GridCell oldParent = furtherSeg.getParent();
			furtherSeg.setParent(closerSeg);
			
			closerSeg = furtherSeg;
			furtherSeg = oldParent;
		}
	}
}
