package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UnbuildMazeCommand extends ArgCommand {
	
	private final SessionHandler sessionHandler;
	private final BuildHandler buildHandler;
	
	public UnbuildMazeCommand(SessionHandler sessionHandler,
	                          BuildHandler buildHandler) {
		super("unbuild");
		addAlias("u");
		addFlag("floor");
		addFlag("roof");
		
		this.sessionHandler = sessionHandler;
		this.buildHandler = buildHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		UUID playerId = getSenderId(sender);
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (null == maze) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		MazePart mazePart = MazePart.WALLS;
		
		if (usedFlags.contains("floor")) {
			mazePart = MazePart.FLOOR;
		} else if (usedFlags.contains("roof")) {
			mazePart = MazePart.ROOF;
		}
		try {
			buildHandler.unbuildMaze(maze, mazePart);
		} catch (TextException e) {
			e.sendTextTo(sender);
		}
	}
}
