package me.gorgeousone.tangledmaze.generation.paving;

import me.gorgeousone.tangledmaze.generation.MazeSegment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PathTree {
	
	private final static Random RANDOM = new Random();
	private final List<MazeSegment> openEnds;
	
	private final Map<MazeSegment, MazeSegment> parents;
	private final Map<MazeSegment, Set<MazeSegment>> children;
	
	int segmentCount;
	
	public PathTree() {
		this.openEnds = new ArrayList<>();
		
		parents = new HashMap<>();
		children = new HashMap<>();
	}
	
	public int size() {
		return segmentCount;
	}
	
	public boolean isComplete() {
		return openEnds.isEmpty();
	}
	
	public void addSegment(MazeSegment segment, MazeSegment parent) {
		segment.setTree(this);
		++segmentCount;
		
		if (parent != null) {
			parents.put(segment, parent);
			children.computeIfAbsent(parent, set -> new HashSet<>());
			children.get(parent).add(segment);
		}
		if (segment.gridX() % 2 == 0 && segment.gridZ() % 2 == 0) {
			openEnds.add(0, segment);
		}
	}
	
	public void addAllEnds(List<MazeSegment> newPathEnds) {
		openEnds.addAll(0, newPathEnds);
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
}
