package me.gorgeousone.tangledmaze.maze;

import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.building.BlockSegment;
import org.bukkit.block.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MazeBackup {
	
	private final MazeMap mazeMap;
	private final Map<MazePart, Set<BlockSegment>> partSegments;
	private final Map<MazePart, Set<BlockState>> partBlocks;
	
	public MazeBackup(MazeMap mazeMap) {
		this.mazeMap = mazeMap;
		partSegments = new HashMap<>();
		partBlocks = new HashMap<>();
	}
	
	public MazeMap getMazeMap() {
		return mazeMap;
	}
	
	public Set<MazePart> getBuiltParts() {
		return partBlocks.keySet();
	}
	
	public Set<BlockSegment> getSegments(MazePart mazePart) {
		return partSegments.get(mazePart);
	}
	
	public Set<BlockSegment> getOrCompute(MazePart mazePart, Function<MazePart, Set<BlockSegment>> mappingFunction) {
		partSegments.computeIfAbsent(mazePart, mappingFunction);
		return partSegments.get(mazePart);
	}
	
	public Set<BlockState> getBlocks(MazePart mazePart) {
		return partBlocks.get(mazePart);
	}
	
	public void setSegments(MazePart mazePart, Set<BlockSegment> segments) {
		partSegments.put(mazePart, segments);
	}
	
	public void setBlocks(MazePart mazePart, Set<BlockState> backupBlocks) {
		partBlocks.putIfAbsent(mazePart, backupBlocks);
	}
	
	public void removeMazePart(MazePart mazePart) {
		partSegments.remove(mazePart);
		partBlocks.remove(mazePart);
	}
}
