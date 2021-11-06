package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.UUID;

public class ChangeWorldListener implements Listener {
	
	private final ToolHandler toolHandler;
	private final RenderHandler renderHandler;
	
	public ChangeWorldListener(ToolHandler toolHandler, RenderHandler renderHandler) {
		this.toolHandler = toolHandler;
		this.renderHandler = renderHandler;
	}
	
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		
		renderHandler.changePlayerWorld(playerId, player.getWorld());
		toolHandler.resetClipTool(playerId);
	}
}
