package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.maze.MazeHandler;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StartMaze extends BaseCommand {
	
	private final ClipHandler clipHandler;
	private final MazeHandler mazeHandler;
	
	public StartMaze(ClipHandler clipHandler,
	                 MazeHandler mazeHandler) {
		super("start");
		this.clipHandler = clipHandler;
		this.mazeHandler = mazeHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args, String alias) {
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip clip = clipHandler.getClip(playerId);
		
		if (clip == null) {
			sender.sendMessage("WRONG!");
			return;
		}
		
		mazeHandler.setMazeClip(playerId, clip);
		clipHandler.removeClip(playerId);
		Bukkit.getPluginManager().callEvent(new MazeStartEvent(playerId, clip));
	}
}
