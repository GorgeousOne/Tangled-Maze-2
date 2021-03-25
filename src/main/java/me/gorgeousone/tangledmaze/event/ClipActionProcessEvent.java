package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class ClipActionProcessEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip clip;
	private final ClipAction action;
	private final UUID playerId;
	
	public ClipActionProcessEvent(Clip clip, ClipAction action, UUID playerId) {
		this.clip = clip;
		this.action = action;
		this.playerId = playerId;
	}
	
	public Clip getClip() {
		return clip;
	}
	
	public ClipAction getAction() {
		return action;
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
