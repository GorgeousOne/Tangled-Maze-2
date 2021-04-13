package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.event.ClipActionProcessEvent;
import me.gorgeousone.tangledmaze.event.ClipUpdateEvent;
import me.gorgeousone.tangledmaze.event.MazeExitSetEvent;
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
 * Stores xz-coordinates representing a clipped area. Each coordinate is paired with a y-coordinate and
 * can also be marked as being a border block of the clip.
 */
public class Clip {
	
	private final UUID playerId;
	private final World world;
	private final TreeMap<Vec2, Integer> fill;
	private final TreeSet<Vec2> border;
	private final List<Vec2> exits;
	
	private final Stack<ClipAction> actionHistory;
	
	public Clip(UUID playerId, World world) {
		this.playerId = playerId;
		this.world = world;
		fill = new TreeMap<>();
		border = new TreeSet<>();
		exits = new ArrayList<>();
		actionHistory = new Stack<>();
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Stack<ClipAction> getActionHistory() {
		return actionHistory;
	}
	
	public TreeMap<Vec2, Integer> getFill() {
		return fill;
	}
	
	public TreeSet<Vec2> getBorder() {
		return border;
	}
	
	public Map<Vec2, Integer> getBlocks(Collection<Vec2> locs) {
		return locs.stream().collect(Collectors.toMap(Function.identity(), this::getY));
	}
	
	public int getY(Vec2 loc) {
		return fill.get(loc);
	}
	
	public List<Vec2> getExits() {
		return exits;
	}
	
	public void add(Block fillBlock) {
		fill.put(new Vec2(fillBlock), fillBlock.getY());
	}
	
	public void add(Map<Vec2, Integer> fillBlocks) {
		fill.putAll(fillBlocks);
	}
	
	public void addBorder(Block borderBlock) {
		border.add(new Vec2(borderBlock));
	}
	
	public void addBorder(Set<Vec2> borderLocs) {
		border.addAll(borderLocs);
	}
	
	public void removeFill(Set<Vec2> locs) {
		fill.keySet().removeAll(locs);
		removeBorder(locs);
	}
	
	public void removeBorder(Set<Vec2> locs) {
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
	 *
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
		if (!isBorderBlock(block)) {
			return;
		}
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
	
}