package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipAction;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.event.ClipActionProcessEvent;
import me.gorgeousone.tangledmaze.event.ClipDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CutClip extends BaseCommand {
	
	private final ClipHandler clipHandler;
	
	public CutClip(ClipHandler clipHandler) {
		super("cut");
		addAlias("remove");
		this.clipHandler = clipHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args, String alias) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip clip = clipHandler.getClip(playerId);
		Clip maze = clipHandler.getMazeClip(playerId);
		
		if (maze == null || clip == null) {
			sender.sendMessage("WRO-HONG!");
			return;
		}
		clipHandler.removeClip(playerId, true);
		ClipAction changes = ClipActionFactory.removeClip(maze, clip);
		maze.processAction(changes, true);
	}
}
