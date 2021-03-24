package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AddClip extends BaseCommand {
	
	private final ClipHandler clipHandler;
	
	public AddClip(ClipHandler clipHandler) {
		super("add");
		addAlias("merge");
		this.clipHandler = clipHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args, String alias) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip clip = clipHandler.getClip(playerId);
		clipHandler.removeClip(playerId);
		Bukkit.getPluginManager().callEvent(new ClipDeleteEvent(clip, playerId));
		
		Clip maze = clipHandler.getMazeClip(playerId);
		ClipAction changes = ClipActionFactory.addClip(maze, clip);
		maze.processAction(changes, true);
	}
}
