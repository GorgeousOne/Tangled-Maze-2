package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MazeUnbuildEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip maze;
	
	public MazeUnbuildEvent(Clip maze) {
		this.maze = maze;
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public UUID getOwnerId() {
		return maze.getOwnerId();
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
