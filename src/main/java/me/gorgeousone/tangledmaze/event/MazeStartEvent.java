package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MazeStartEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final UUID playerId;
	private final Clip clip;
	
	public MazeStartEvent(UUID playerId, Clip clip) {
		this.playerId = playerId;
		this.clip = clip;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public Clip getClip() {
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
