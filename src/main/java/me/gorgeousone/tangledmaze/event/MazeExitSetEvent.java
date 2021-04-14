package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores changes when exits are set/removed. Main exits are saved in both exit maps and main exit maps.
 */
public class MazeExitSetEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Clip maze;
	private final Map<Vec2, Integer> addedExits;
	private final Map<Vec2, Integer> addedMainExits;
	private final Set<Vec2> removedExits;
	private final Set<Vec2> removedMainExits;
	
	public MazeExitSetEvent(Clip maze) {
		this.maze = maze;
		
		addedExits = new HashMap<>();
		addedMainExits = new HashMap<>();
		removedExits = new HashSet<>();
		removedMainExits = new HashSet<>();
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public Map<Vec2, Integer> getAddedExits() {
		return addedExits;
	}
	
	public Map<Vec2, Integer> getAddedMainExits() {
		return addedMainExits;
	}
	
	public Set<Vec2> getRemovedExits() {
		return removedExits;
	}
	
	public Set<Vec2> getRemovedMainExits() {
		return removedMainExits;
	}
	
	public void addExit(Vec2 loc, int y) {
		addedExits.put(loc, y);
	}
	
	public void addMainExit(Vec2 loc, int y) {
		addedMainExits.put(loc, y);
	}
	
	public void removeExit(Vec2 removedExit) {
		removedExits.add(removedExit);
	}
	
	public void removeMainExit(Vec2 removedMainExit) {
		removedMainExits.add(removedMainExit);
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
