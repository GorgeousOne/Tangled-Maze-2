package me.gorgeousone.tangledmaze.maze;

import java.util.UUID;

public class MazeSession {
	
	private final UUID playerId;
	
	public MazeSession(UUID playerId) {
		this.playerId = playerId;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	
}
