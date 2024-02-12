package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.event.ClipActionProcessEvent;
import me.gorgeousone.tangledmaze.event.ClipUpdateEvent;
import me.gorgeousone.tangledmaze.event.MazeExitSetEvent;
import me.gorgeousone.tangledmaze.event.MazeStateChangeEvent;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class for storing the layout of a redstone maze or a goldblock clip. It stores the fill of the
 * clip by using key value pairs of xz coordinates and y-coordinates. The xy coordinates can also be marked
 * as border of the clip and exits of the maze.
 */
public class Clip {
	
	private UUID ownerId;
	private final World world;
	private final Map<Vec2, Integer> fill;
	private final Set<Vec2> border;
	private final List<Vec2> exits;
	private final Stack<ClipAction> actionHistory;
	private boolean isEditable;
	
	public Clip(UUID playerId, World world) {
		this.ownerId = playerId;
		this.world = world;
		fill = new TreeMap<>();
		border = new TreeSet<>();
		exits = new ArrayList<>();
		actionHistory = new Stack<>();
		isEditable = true;
	}
	
	public UUID getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public World getWorld() {
		return world;
	}
	
	/**
	 * Returns true if the clip is editable and no built as maze right now
	 */
	public boolean isEditable() {
		return isEditable;
	}
	
	/**
	 * Calls an event to signal a toggle of being editable
	 */
	public void setEditable(boolean editable) {
		boolean oldState = isEditable;
		isEditable = editable;
		
		if (oldState != editable) {
			Bukkit.getPluginManager().callEvent(new MazeStateChangeEvent(this, editable));
		}
	}
	
	public Stack<ClipAction> getActionHistory() {
		return actionHistory;
	}
	
	public Map<Vec2, Integer> getFill() {
		return fill;
	}
	
	public Set<Vec2> getBorder() {
		return border;
	}
	
	public Map<Vec2, Integer> getBlocks(Collection<Vec2> locs) {
		return locs.stream().collect(Collectors.toMap(Function.identity(), this::getY));
	}
	
	public int getY(Vec2 loc) {
		return fill.getOrDefault(loc, -1);
	}
	
	public List<Vec2> getExits() {
		return exits;
	}
	
	public void add(Block fillBlock) {
		add(fillBlock.getX(), fillBlock.getZ(), fillBlock.getY());
	}
	
	public void add(int x, int z, int y) {
		fill.put(new Vec2(x, z), y);
	}
	
	public void add(Map<Vec2, Integer> fillBlocks) {
		fill.putAll(fillBlocks);
	}
	
	public void addBorder(Block borderBlock) {
		addBorder(new Vec2(borderBlock));
	}
	
	public void addBorder(Vec2 loc) {
		border.add(loc);
	}
	
	public void addBorder(Set<Vec2> borderLocs) {
		border.addAll(borderLocs);
	}
	
	public void removeFill(Collection<Vec2> locs) {
		fill.keySet().removeAll(locs);
		removeBorder(locs);
	}
	
	public void removeBorder(Collection<Vec2> locs) {
		border.removeAll(locs);
	}
	
	public boolean contains(Vec2 loc) {
		return fill.containsKey(loc);
	}
	
	public boolean borderContains(Vec2 loc) {
		return border.contains(loc);
	}
	
	public boolean isFillBlock(Block block) {
		Vec2 blockLoc = new Vec2(block);
		return contains(blockLoc) && block.getY() == getY(blockLoc);
	}
	
	public boolean isBorderBlock(Block block) {
		Vec2 blockLoc = new Vec2(block);
		return borderContains(blockLoc) && block.getY() == getY(blockLoc);
	}
	
	public boolean exitsContain(Vec2 loc) {
		return exits.contains(loc);
	}
	
	/**
	 * Processes the changes stored in a {@link ClipAction} and calls an event
	 * @param action        changes container
	 * @param saveToHistory will save the action to history for undoing later if set to true
	 */
	public void processAction(ClipAction action, boolean saveToHistory) {
		removeBorder(action.getRemovedBorder());
		removeFill(action.getRemovedFill().keySet());
		add(action.getAddedFill());
		addBorder(action.getAddedBorder());
		exits.removeAll(action.getRemovedExits());
		
		if (saveToHistory) {
			actionHistory.push(action);
		}
		Bukkit.getPluginManager().callEvent(new ClipActionProcessEvent(this, action));
	}
	
	public void toggleExit(Block block) {
		Vec2 exitLoc = new Vec2(block);
		int exitY = block.getY();
		
		if (exits.contains(exitLoc)) {
			removeExit(exitLoc);
		} else {
			addExit(exitLoc, exitY);
		}
	}
	
	private void addExit(Vec2 loc, int y) {
		MazeExitSetEvent exitSetEvent = new MazeExitSetEvent(this);
		
		if (!exits.isEmpty()) {
			exitSetEvent.removeMainExit(exits.get(0));
		}
		exits.add(0, loc);
		exitSetEvent.addExit(loc, y);
		exitSetEvent.addMainExit(loc, y);
		Bukkit.getPluginManager().callEvent(exitSetEvent);
	}
	
	private void removeExit(Vec2 loc) {
		MazeExitSetEvent exitSetEvent = new MazeExitSetEvent(this);
		int index = exits.indexOf(loc);
		exits.remove(index);
		exitSetEvent.removeExit(loc);
		
		if (index == 0) {
			exitSetEvent.removeMainExit(loc);
			if (!exits.isEmpty()) {
				exitSetEvent.addMainExit(exits.get(0), getY(exits.get(0)));
			}
		}
		Bukkit.getPluginManager().callEvent(exitSetEvent);
	}
	
	public void updateLoc(Vec2 loc, int newY) {
		fill.put(loc, newY);
		
		if (borderContains(loc)) {
			Bukkit.getPluginManager().callEvent(new ClipUpdateEvent(this, loc, newY));
		}
	}
	
	/**
	 * Recalculates y-coordinates of all locations in the clip
	 */
	public void updateHeights() {
		for (Map.Entry<Vec2, Integer> fill : getFill().entrySet()) {
			fill.setValue(BlockUtil.getSurfaceY(world, fill.getKey(), fill.getValue()));
		}
	}
}