package me.gorgeousone.tangledmaze.maze;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.generation.BlockCollection;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.VersionUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A backup of a maze's clip, build settings, build materials, and the previous block states of the blocks changed for each maze part.
 */
public class MazeBackup {
	
	private final String versionString = VersionUtil.PLUGIN_VERSION.toString();
	
	private final Clip maze;
	private final MazeSettings settings;
	private MazeMap mazeMap;
	private final Map<MazePart, BlockCollection> partBlockLocs;
	private final Map<MazePart, Set<BlockLocType>> partBlocksTypes;
	private Map<String, BlockVec> lootLocations;

	public MazeBackup(Clip maze, MazeSettings settings) {
		this.maze = maze;
		this.settings = settings;
		partBlockLocs = new HashMap<>();
		partBlocksTypes = new HashMap<>();
		lootLocations = new HashMap<>();
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

	public void addLootLocations(Map<String, BlockVec> chestLocations) {
		if (lootLocations == null) {
			lootLocations = new HashMap<>();
		}
		lootLocations.putAll(chestLocations);
	}

	public void clearLootLocations() {
		if (lootLocations == null) {
			lootLocations = new HashMap<>();
		}
		lootLocations.clear();
	}

	public Map<String, BlockVec> getLootLocations() {
		//workaround to load maze backups from <2.5.0
		if (lootLocations == null) {
			lootLocations = new HashMap<>();
		}
		return new HashMap<>(lootLocations);
	}
}
