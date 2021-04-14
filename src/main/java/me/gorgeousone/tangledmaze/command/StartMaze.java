package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StartMaze extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	
	public StartMaze(SessionHandler sessionHandler, ToolHandler toolHandler) {
		super("start");
		this.sessionHandler = sessionHandler;
		this.toolHandler = toolHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip clip = sessionHandler.getClip(playerId);
		
		if (clip == null) {
			sender.sendMessage("no clip");
			return;
		}
		
		sessionHandler.setMazeClip(playerId, clip);
		toolHandler.resetClipTool(playerId);
		Bukkit.getPluginManager().callEvent(new MazeStartEvent(clip));
	}
}
