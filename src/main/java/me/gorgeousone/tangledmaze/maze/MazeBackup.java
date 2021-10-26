package me.gorgeousone.tangledmaze.maze;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.BlockSegment;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import org.bukkit.block.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MazeBackup {
	
	private Clip maze;
	private transient MazeMap mazeMap;
	private final transient Map<MazePart, Set<BlockSegment>> partSegments;
	private final transient Map<MazePart, Set<BlockState>> partBlocks;
	
	public MazeBackup(Clip maze) {
		this.maze = maze;
		partSegments = new HashMap<>();
		partBlocks = new HashMap<>();
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public void createMazeMapIfAbsent(MazeSettings settings) {
		this.mazeMap = MazeMapFactory.createMazeMapOf(maze, settings);
		
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
	
	public void computeSegmentsIfAbsent(MazePart mazePart, Function<MazePart, Set<BlockSegment>> mappingFunction) {
		partSegments.computeIfAbsent(mazePart, mappingFunction);
	}
	
	public Set<BlockState> getBlocks(MazePart mazePart) {
		return partBlocks.get(mazePart);
	}
	
	public void setSegments(MazePart mazePart, Set<BlockSegment> segments) {
		partSegments.put(mazePart, segments);
	}
	
	/**
	 * Saves the previous block states of the block changed for this maze part, if none have been saved before.
	 * Returns true if this maze part didn't yet have backup blocks.
	 */
	public void setBlocksIfAbsent(MazePart mazePart, Set<BlockState> backupBlocks) {
		partBlocks.putIfAbsent(mazePart, backupBlocks);
	}
	
	public boolean hasBlocks(MazePart mazePart) {
		return partBlocks.containsKey(mazePart);
	}
	
	public void removeMazePart(MazePart mazePart) {
		partSegments.remove(mazePart);
		partBlocks.remove(mazePart);
	}
}
