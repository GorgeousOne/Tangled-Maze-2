package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event that gets called when a maze changes its state of being editable (built / unbuilt).
 */
public class MazeStateChangeEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip maze;
	private final boolean isEditable;
	
	public MazeStateChangeEvent(Clip maze, boolean isEditable) {
		this.maze = maze;
		this.isEditable = isEditable;
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public boolean isMazeActive() {
		return isEditable;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
