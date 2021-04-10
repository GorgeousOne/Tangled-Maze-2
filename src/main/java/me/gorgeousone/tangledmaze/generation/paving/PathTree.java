package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.GridSegment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PathTree {
	
	private final static Random RANDOM = new Random();
	private final List<GridSegment> openEnds;
	private final Set<GridSegment> segments;
	private final Set<GridSegment> intersections;
	//	private final Map<MazeSegment, Set<MazeSegment>> children;
	int maxExitDist;
	private final int index;
	
	public PathTree(int index) {
		this.index = index;
		this.openEnds = new ArrayList<>();
		segments = new HashSet<>();
		intersections = new HashSet<>();
		//		children = new HashMap<>();
	}
	
	@Override
	public String toString() {
		return "Tree " + index;
	}
	
	public int size() {
		return segments.size();
	}
	
	public boolean isComplete() {
		return openEnds.isEmpty();
	}
	
	public void addSegment(GridSegment segment, GridSegment parent) {
		segment.setTree(this);
		segment.setParent(parent);
		segments.add(segment);
		
		int exitDist = getExitDist(segment);
		maxExitDist = Math.max(exitDist, maxExitDist);
		
		//		if (parent != null) {
		//			children.computeIfAbsent(parent, set -> new HashSet<>());
		//			children.get(parent).add(segment);
		//		}
		if (segment.gridX() % 2 == 0 && segment.gridZ() % 2 == 0) {
			intersections.add(segment);
			openEnds.add(0, segment);
		}
	}
	
	public Set<GridSegment> getSegments() {
		return segments;
	}
	
	public Set<GridSegment> getIntersections() {
		return intersections;
	}
	
	public int getMaxExitDist() {
		return maxExitDist;
	}
	
	public int getExitDist(GridSegment segment) {
		int dist = 0;
		while (segment.hasParent()) {
			++dist;
			segment = segment.getParent();
		}
		return dist;
	}
	
	public GridSegment getLastEnd() {
		return openEnds.get(0);
	}
	
	public GridSegment getRndEnd() {
		return openEnds.get(RANDOM.nextInt(openEnds.size()));
	}
	
	public void removeEnd(GridSegment pathEnd) {
		openEnds.remove(pathEnd);
	}
	
	public void mergeTree(PathTree other, GridSegment ownSegment, GridSegment otherSegment, GridSegment linkSegment) {
		for (GridSegment segment : other.segments) {
			segment.setTree(this);
		}
		segments.addAll(other.segments);
		intersections.addAll(other.intersections);
		//		children.putAll(other.children);
		
		addSegment(linkSegment, ownSegment);
		balanceTree(linkSegment, otherSegment);
	}
	
	private void balanceTree(GridSegment seg1, GridSegment seg2) {
		int exitDist1 = getExitDist(seg1);
		int exitDist2 = getExitDist(seg2);
		int distDiff = Math.abs(exitDist2 - exitDist1);
		maxExitDist = Math.max(maxExitDist, (exitDist2 + exitDist1) / 2);
		
		GridSegment furtherSeg = exitDist1 > exitDist2 ? seg1 : seg2;
		GridSegment closerSeg = exitDist1 <= exitDist2 ? seg1 : seg2;
		
		
		for (int i = 0; i < distDiff / 2; i++) {
			GridSegment oldParent = furtherSeg.getParent();
			furtherSeg.setParent(closerSeg);
			
			closerSeg = furtherSeg;
			furtherSeg = oldParent;
		}
	}
}
