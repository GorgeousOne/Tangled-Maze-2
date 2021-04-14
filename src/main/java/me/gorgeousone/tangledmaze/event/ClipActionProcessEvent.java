package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClipActionProcessEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Clip clip;
	private final ClipAction action;
	
	public ClipActionProcessEvent(Clip clip, ClipAction action) {
		this.clip = clip;
		this.action = action;
	}
	
	public Clip getClip() {
		return clip;
	}
	
	public ClipAction getAction() {
		return action;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
