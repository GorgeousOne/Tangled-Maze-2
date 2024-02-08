package me.gorgeousone.tangledmaze.maze;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.VersionUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MazeBackup {
	
	@SerializedName(value = "version")
	@Expose(deserialize = false)
	private final String versionString = VersionUtil.PLUGIN_VERSION.toString();
	
	private final Clip maze;
	private final MazeSettings settings;
	private MazeMap mazeMap;
	@SerializedName(value = "partBlockLocs", alternate = "partSegments")
	private final Map<MazePart, BlockCollection> partBlockLocs;
	@SerializedName(value = "partBlockTypes", alternate = "partBlocks")
	private final Map<MazePart, Set<BlockLocType>> partBlocksTypes;
	
	public MazeBackup(Clip maze, MazeSettings settings) {
		this.maze = maze;
		this.settings = settings;
		partBlockLocs = new HashMap<>();
		partBlocksTypes = new HashMap<>();
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public MazeSettings getSettings() {
		return settings;
	}
	
	public void createMazeMapIfAbsent(MazeSettings settings) {
		if (null == mazeMap) {
			this.mazeMap = MazeMapFactory.createMazeMapOf(maze, settings, BlockUtil.getWorldMinHeight(maze.getWorld()));
		}
	}
	
	public MazeMap getMazeMap() {
		return mazeMap;
	}
	
	public Set<MazePart> getBuiltParts() {
		return partBlocksTypes.keySet();
	}
	
	public BlockCollection getPartBlockLocs(MazePart mazePart) {
		return partBlockLocs.get(mazePart);
	}
	
	public void computeSegmentsIfAbsent(MazePart mazePart, Function<MazePart, BlockCollection> mappingFunction) {
		partBlockLocs.computeIfAbsent(mazePart, mappingFunction);
	}

	public Set<BlockLocType> getAllBlocks() {
		return partBlocksTypes.values().stream()
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	public Set<BlockLocType> getBlocks(MazePart mazePart) {
		return partBlocksTypes.get(mazePart);
	}
	
	/**
	 * Saves the previous block states of the block changed for this maze part, if none have been saved before.
	 * Returns true if this maze part didn't yet have backup blocks.
	 */
	public void setBlocksIfAbsent(MazePart mazePart, Set<BlockLocType> backupBlocks) {
		partBlocksTypes.putIfAbsent(mazePart, backupBlocks);
	}
	
	public boolean hasBlocks(MazePart mazePart) {
		return partBlocksTypes.containsKey(mazePart);
	}
	
	public void removeMazePart(MazePart mazePart) {
		partBlockLocs.remove(mazePart);
		partBlocksTypes.remove(mazePart);
	}
	
	/**
	 * Removes all saved block states of the maze and the maze map
	 */
	public void removeAllMazeParts() {
		partBlockLocs.clear();
		partBlocksTypes.clear();
		mazeMap = null;
	}
}
