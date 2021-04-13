package me.gorgeousone.tangledmaze.event;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class ClipUpdateEvent extends Event{
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Clip clip;
	private final Vec2 blockLoc;
	private final int newY;
	
	public ClipUpdateEvent(Clip clip, Vec2 blockLoc, int newY) {
		this.clip = clip;
		this.blockLoc = blockLoc;
		this.newY = newY;
	}
	
	public Clip getClip() {
		return clip;
	}
	
	public Vec2 getLoc() {
		return blockLoc;
	}
	
	public int getNewY() {
		return newY;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
