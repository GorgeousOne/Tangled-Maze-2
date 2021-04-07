package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.MazeSegment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PathTree {
	
	private final static Random RANDOM = new Random();
	private final List<MazeSegment> openEnds;
	private final Set<MazeSegment> segments;
	private final Set<MazeSegment> intersections;
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
	
	public void addSegment(MazeSegment segment, MazeSegment parent) {
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
	
	public Set<MazeSegment> getSegments() {
		return segments;
	}
	
	public Set<MazeSegment> getIntersections() {
		return intersections;
	}
	
	public int getMaxExitDist() {
		return maxExitDist;
	}
	
	public int getExitDist(MazeSegment segment) {
		int dist = 0;
		while (segment.hasParent()) {
			++dist;
			segment = segment.getParent();
		}
		return dist;
	}
	
	public MazeSegment getLastEnd() {
		return openEnds.get(0);
	}
	
	public MazeSegment getRndEnd() {
		return openEnds.get(RANDOM.nextInt(openEnds.size()));
	}
	
	public void removeEnd(MazeSegment pathEnd) {
		openEnds.remove(pathEnd);
	}
	
	public void mergeTree(PathTree other, MazeSegment ownSegment, MazeSegment otherSegment, MazeSegment linkingSegment) {
		for (MazeSegment segment : other.segments) {
			segment.setTree(this);
		}
		segments.addAll(other.segments);
		intersections.addAll(other.intersections);
//		children.putAll(other.children);
		
		addSegment(linkingSegment, ownSegment);
		balanceTree(linkingSegment, otherSegment);
	}
	
	private void balanceTree(MazeSegment seg1, MazeSegment seg2) {
		int exitDist1 = getExitDist(seg1);
		int exitDist2 = getExitDist(seg2);
		int distDiff = Math.abs(exitDist2 - exitDist1);
		maxExitDist = Math.max(maxExitDist, (exitDist2 + exitDist1) / 2);
		
		MazeSegment furtherSeg = exitDist1 > exitDist2 ? seg1 : seg2;
		MazeSegment closerSeg = exitDist1 <= exitDist2 ? seg1 : seg2;
		
		
		for (int i = 0; i < distDiff / 2; i++) {
			MazeSegment oldParent = furtherSeg.getParent();
			furtherSeg.setParent(closerSeg);
			
			closerSeg = furtherSeg;
			furtherSeg = oldParent;
		}
	}
}
