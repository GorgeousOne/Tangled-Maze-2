package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.GridCell;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeSolver;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.render.RenderSession;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

/**
 * The command to render the solution of a maze with fake blocks to the player.
 */
public class SolveMazeCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final RenderHandler renderHandler;
	
	public SolveMazeCommand(
			SessionHandler sessionHandler,
			RenderHandler renderHandler) {
		super("solve");
		setPlayerRequired(true);
		
		this.sessionHandler = sessionHandler;
		this.renderHandler = renderHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		UUID playerId = getSenderId(sender);
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (null == maze) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		if (!sessionHandler.isBuilt(maze)) {
			Message.INFO_MAZE_NOT_BUILT.sendTo(sender);
			return;
		}
		MazeBackup backup = sessionHandler.getBackup(maze);
		MazeMap mazeMap = backup.getMazeMap();
		
		if (backup.getMazeMap().getPathMap().getExits().size() < 2) {
			Message.INFO_MAZE_SINGLE_EXIT.sendTo(sender);
			return;
		}
		List<GridCell> solution = MazeSolver.findSolvingPath(mazeMap.getPathMap());
		RenderSession render = renderHandler.getPlayerRender(playerId);
		renderHandler.displayMazeSolution(render, backup.getMazeMap(), solution);
	}
	
//	private int estimatePathLength(List<GridCell> path) {
//		int length = 0;
//
//		for (GridCell cell : path) {
//			Vec2 size = cell.getMax().sub(cell.getMin());
//			length += Math.max(size.getX(), size.getZ());
//		}
//		return length;
//	}
}
