package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.event.MazeStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StartMaze extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	
	public StartMaze(SessionHandler sessionHandler) {
		super("start");
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip clip = sessionHandler.getClip(playerId);
		
		if (clip == null) {
			sender.sendMessage("no clip");
			return;
		}
		
		sessionHandler.setMazeClip(playerId, clip);
		sessionHandler.removeClipTool(playerId);
		Bukkit.getPluginManager().callEvent(new MazeStartEvent(clip));
	}
}
