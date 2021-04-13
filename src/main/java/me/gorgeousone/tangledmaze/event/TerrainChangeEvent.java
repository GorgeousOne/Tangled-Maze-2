package me.gorgeousone.tangledmaze.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class TerrainChangeEvent extends Event{
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Collection<Block> updatedBlocks;
	private final Player cause;
	
	public TerrainChangeEvent(Collection<Block> updatedBlocks, Player cause) {
		this.updatedBlocks = updatedBlocks;
		this.cause = cause;
	}
	
	public Collection<Block> getChangedBlocks() {
		return updatedBlocks;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
