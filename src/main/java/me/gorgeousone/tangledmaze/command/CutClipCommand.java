package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CutClipCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	
	public CutClipCommand(SessionHandler sessionHandler, ToolHandler toolHandler) {
		super("cut");
		addAlias("remove");
		setPlayerRequired(true);
		
		this.toolHandler = toolHandler;
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip clip = sessionHandler.getClip(playerId);
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (maze == null) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		if (clip == null) {
			Message.ERROR_CLIPBOARD_MISSING.sendTo(sender);
			return;
		}
		if (!maze.isActive()) {
			Message.INFO_MAZE_INACCESSIBLE.sendTo(sender);
			return;
		}
		toolHandler.resetClipTool(playerId);
		ClipAction changes = ClipActionFactory.removeClip(maze, clip);
		maze.processAction(changes, true);
	}
}
