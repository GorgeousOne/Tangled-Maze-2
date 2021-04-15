package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AddClipCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	
	public AddClipCommand(SessionHandler sessionHandler, ToolHandler toolHandler) {
		super("add");
		this.toolHandler = toolHandler;
		addAlias("merge");
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = sessionHandler.getMazeClip(playerId);
		Clip clip = sessionHandler.getClip(playerId);
		
		if (maze == null || clip == null) {
			sender.sendMessage("no maze / clip");
			return;
		}
		toolHandler.resetClipTool(playerId);
		ClipAction changes = ClipActionFactory.addClip(maze, clip);
		maze.processAction(changes, true);
	}
}
