package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class BuildMaze extends ArgCommand {
	
	private final ClipHandler clipHandler;
	private final BuildHandler buildHandler;
	
	public BuildMaze(ClipHandler clipHandler, BuildHandler buildHandler) {
		super("build");
		
		this.clipHandler = clipHandler;
		this.buildHandler = buildHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, ArgValue[] argValues, Set<String> usedFlags) {
		
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		Clip maze = clipHandler.getMazeClip(playerId);
		
		if (maze == null) {
			sender.sendMessage(ChatColor.GRAY + "Well well well");
			return;
		}
		
		if (maze.getExits().isEmpty()) {
			sender.sendMessage(ChatColor.GRAY + "Mhh mhh mhh");
			return;
		}
		
		buildHandler.buildMaze(maze);
	}
}
