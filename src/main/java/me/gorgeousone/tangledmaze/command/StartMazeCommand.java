package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class StartMazeCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	
	public StartMazeCommand(SessionHandler sessionHandler, ToolHandler toolHandler) {
		super("start");
		setPlayerRequired(true);
		
		this.sessionHandler = sessionHandler;
		this.toolHandler = toolHandler;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID playerId = getSenderId(sender);
		Clip clip = sessionHandler.getClip(playerId);
		
		if (null == clip) {
			Message.ERROR_CLIPBOARD_MISSING.sendTo(sender);
			return;
		}
		
		sessionHandler.setMazeClip(playerId, clip);
		toolHandler.resetClipTool(playerId);
		Bukkit.getPluginManager().callEvent(new MazeStartEvent(clip));
	}
}
