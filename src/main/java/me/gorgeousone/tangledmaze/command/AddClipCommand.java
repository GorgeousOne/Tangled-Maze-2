package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * The command to add a gold block clip to a redstone maze.
 */
public class AddClipCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	
	public AddClipCommand(SessionHandler sessionHandler, ToolHandler toolHandler) {
		super("add");
		setPlayerRequired(true);
		addAlias("merge");
		
		this.toolHandler = toolHandler;
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID playerId = getSenderId(sender);
		Clip maze = sessionHandler.getMazeClip(playerId);
		Clip clip = sessionHandler.getClip(playerId);
		
		if (null == maze) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		if (null == clip) {
			Message.ERROR_CLIPBOARD_MISSING.sendTo(sender);
			return;
		}
		if (!maze.isEditable()) {
			Message.INFO_MAZE_NOT_EDITABLE.sendTo(sender);
			return;
		}
		ClipAction changes = ClipActionFactory.addClip(maze, clip);
		toolHandler.resetClipTool(playerId);
		
		if (null == changes) {
			return;
		}
		maze.processAction(changes, true);
	}
}
