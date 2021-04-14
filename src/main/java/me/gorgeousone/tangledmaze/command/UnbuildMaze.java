package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.maze.MazePart;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UnbuildMaze extends ArgCommand {
	
	private final SessionHandler sessionHandler;
	private final BuildHandler buildHandler;
	
	public UnbuildMaze(SessionHandler sessionHandler,
	                   BuildHandler buildHandler) {
		super("unbuild");
		addFlag("floor");
		addFlag("roof");
		
		this.sessionHandler = sessionHandler;
		this.buildHandler = buildHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (maze == null) {
			sender.sendMessage(ChatColor.GRAY + "no maze");
			return;
		}
		MazePart mazePart = MazePart.WALLS;
		
		if (usedFlags.contains("floor")) {
			mazePart = MazePart.FLOOR;
		} else if (usedFlags.contains("roof")) {
			mazePart = MazePart.ROOF;
		}
		
		try {
			buildHandler.unbuildMaze(playerId, maze, mazePart);
		} catch (IllegalArgumentException e) {
			sender.sendMessage("Error: " + e.getMessage());
		}
	}
}
