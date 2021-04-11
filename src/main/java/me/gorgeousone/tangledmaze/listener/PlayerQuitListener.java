package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {
	
	private final SessionHandler sessionHandler;
	private final RenderHandler renderHandler;
	
	public PlayerQuitListener(SessionHandler sessionHandler, RenderHandler renderHandler) {
		this.sessionHandler = sessionHandler;
		this.renderHandler = renderHandler;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		
		sessionHandler.removePlayer(playerId);
		renderHandler.removePlayer(playerId);
	}
}
