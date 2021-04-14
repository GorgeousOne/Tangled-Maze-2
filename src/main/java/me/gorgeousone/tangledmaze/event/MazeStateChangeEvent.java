package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MazeStateChangeEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip maze;
	private final boolean isActive;
	
	public MazeStateChangeEvent(Clip maze, boolean isActive) {
		this.maze = maze;
		this.isActive = isActive;
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public boolean isMazeActive() {
		return isActive;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
