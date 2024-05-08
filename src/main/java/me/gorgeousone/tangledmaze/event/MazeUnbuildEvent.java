package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.maze.MazePart;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MazeUnbuildEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip maze;
	private final MazePart mazePart;
	
	public MazeUnbuildEvent(Clip maze, MazePart mazePart) {
		this.maze = maze;
		this.mazePart = mazePart;
	}
	
	public Clip getMaze() {
		return maze;
	}

	public MazePart getMazePart() {
		return mazePart;
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
