package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event to signal that a maze has been started working on.
 * May refer to a new maze or a maze that has been loaded from file.
 */
public class MazeStartEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Clip clip;
	
	public MazeStartEvent(Clip clip) {
		this.clip = clip;
	}
	
	public Clip getMaze() {
		return clip;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
