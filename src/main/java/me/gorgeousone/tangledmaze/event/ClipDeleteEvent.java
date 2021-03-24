package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class ClipDeleteEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip clip;
	private final UUID playerId;
	
	public ClipDeleteEvent(Clip clip, UUID playerId) {
		this.clip = clip;
		this.playerId = playerId;
	}
	
	public Clip getClip() {
		return clip;
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
