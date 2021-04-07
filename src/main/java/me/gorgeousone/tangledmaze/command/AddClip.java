package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
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
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = clipHandler.getMazeClip(playerId);
		Clip clip = clipHandler.getClip(playerId);
		
		if (maze == null || clip == null) {
			sender.sendMessage("WRO-HONG!");
			return;
		}
		clipHandler.removeClip(playerId, true);
		
		ClipAction changes = ClipActionFactory.addClip(maze, clip);
		maze.processAction(changes, true);
	}
}
