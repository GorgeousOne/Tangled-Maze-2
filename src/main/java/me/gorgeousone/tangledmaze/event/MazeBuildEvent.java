package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MazeBuildEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip maze;
	private final UUID playerId;
	
	public MazeBuildEvent(Clip maze, UUID playerId) {
		this.maze = maze;
		this.playerId = playerId;
	}
	
	public Clip getMaze() {
		return maze;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
