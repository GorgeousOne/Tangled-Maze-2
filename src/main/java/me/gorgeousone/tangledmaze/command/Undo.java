package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Undo extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	
	public Undo(SessionHandler sessionHandler) {
		super("undo");
		addAlias("unoreverse");
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (maze == null) {
			sender.sendMessage(ChatColor.GRAY + "no maze");
			return;
		}
		
		if (maze.getActionHistory().isEmpty()) {
			sender.sendMessage("nothing to undo");
			return;
		}
		maze.processAction(maze.getActionHistory().pop().invert(), false);
	}
}
