package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClipDeleteEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip clip;
	
	public ClipDeleteEvent(Clip clip) {
		this.clip = clip;
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
