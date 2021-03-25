package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.tool.ClipTool;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class ClipToolChangeEvent extends Event {
	
	public enum Cause {
		COMPLETE, RESTART, PROGRESS
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final ClipTool tool;
	private final Cause cause;
	
	public ClipToolChangeEvent(ClipTool clipTool, Cause cause) {
		this.cause = cause;
		this.tool = clipTool;
	}
	
	public ClipTool getTool() {
		return tool;
	}
	
	public UUID getPlayerId() {
		return tool.getPlayerId();
	}
	
	public Cause getCause() {
		return cause;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
