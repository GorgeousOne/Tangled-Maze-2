package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UndoCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	
	public UndoCommand(SessionHandler sessionHandler) {
		super("undo");
		addAlias("unoreverse");
		setPlayerRequired(true);
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (maze == null) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		if (!maze.isActive()) {
			Message.INFO_MAZE_INACCESSIBLE.sendTo(sender);
			return;
		}
		if (!maze.getActionHistory().isEmpty()) {
			maze.processAction(maze.getActionHistory().pop().invert(), false);
		}
	}
}
