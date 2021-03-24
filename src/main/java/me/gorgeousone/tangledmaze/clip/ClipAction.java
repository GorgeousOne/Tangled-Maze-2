package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.util.Vec2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClipAction {
	
	private final Clip clip;
	private final Map<Vec2, Integer> addedFill;
	private final Map<Vec2, Integer> removedFill;
	private final Set<Vec2> addedBorder;
	private final Set<Vec2> removedBorder;
	private final Set<Vec2> removedExits;
	
	public ClipAction(Clip clip) {
		this.clip = clip;
		addedFill = new HashMap<>();
		removedFill = new HashMap<>();
		addedBorder = new HashSet<>();
		removedBorder = new HashSet<>();
		removedExits = new HashSet<>();
	}
	
	public Map<Vec2, Integer> getAddedFill() {
		return addedFill;
	}
	
	public Map<Vec2, Integer> getRemovedFill() {
		return removedFill;
	}
	
	public Set<Vec2> getAddedBorder() {
		return addedBorder;
	}
	
	public Map<Vec2, Integer> getAddedBorderBlocks() {
		HashMap<Vec2, Integer> addedBorderBlocks = new HashMap<>();
		addedBorder.forEach(loc -> addedBorderBlocks.put(loc, addedFill.containsKey(loc) ? addedFill.get(loc) : clip.getY(loc)));
		return addedBorderBlocks;
	}
	
	public Set<Vec2> getRemovedBorder() {
		return removedBorder;
	}
	
	public Set<Vec2> getRemovedExits() {
		return removedExits;
	}
	
	public void addFill(Vec2 loc, int y) {
		addedFill.put(loc, y);
	}
	
	public void removeBorder(Vec2 loc) {
		removedBorder.add(loc);
	}
	
	public void removeExit(Vec2 loc) {
		removedExits.add(loc);
	}
	
	public void addBorder(Vec2 loc) {
		addedBorder.add(loc);
	}
	
	public void removeFill(Vec2 loc, int y) {
		removedFill.put(loc, y);
	}
	
	public boolean clipWillContain(Vec2 loc) {
		return clip.contains(loc) && !getRemovedFill().containsKey(loc) || getAddedFill().containsKey(loc);
	}
	
	public boolean clipBorderWillContain(Vec2 loc) {
		return getAddedBorder().contains(loc) || !getRemovedBorder().contains(loc) && clip.borderContains(loc);
	}
}
