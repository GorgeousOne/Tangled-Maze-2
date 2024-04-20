package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.menu.UiHandler;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * A listener to remove a player from all handlers when they quit the server.
 */
public class PlayerQuitListener implements Listener {
	
	private final SessionHandler sessionHandler;
	private final RenderHandler renderHandler;
	private final ToolHandler toolHandler;
	private final UiHandler uiHandler;

	public PlayerQuitListener(SessionHandler sessionHandler,
	                          RenderHandler renderHandler,
	                          ToolHandler toolHandler, UiHandler uiHandler) {
		this.sessionHandler = sessionHandler;
		this.renderHandler = renderHandler;
		this.toolHandler = toolHandler;
		this.uiHandler = uiHandler;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		
		sessionHandler.removePlayer(playerId);
		renderHandler.removePlayer(playerId);
		toolHandler.removePlayer(playerId);
		uiHandler.remove(playerId);
	}
}
