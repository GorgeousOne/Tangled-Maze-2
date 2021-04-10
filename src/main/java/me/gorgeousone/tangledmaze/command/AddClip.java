package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AddClip extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	
	public AddClip(SessionHandler sessionHandler) {
		super("add");
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
		sessionHandler.removeClip(playerId, true);
		
		ClipAction changes = ClipActionFactory.addClip(maze, clip);
		maze.processAction(changes, true);
	}
}
