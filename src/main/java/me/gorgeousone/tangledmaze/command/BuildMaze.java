package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.util.MatUtilsAquatic;
import me.gorgeousone.tangledmaze.util.MathUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BuildMaze extends ArgCommand {
	
	private final SessionHandler sessionHandler;
	private final BuildHandler buildHandler;
	
	public BuildMaze(SessionHandler sessionHandler, BuildHandler buildHandler) {
		super("build");
		
		this.sessionHandler = sessionHandler;
		this.buildHandler = buildHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, ArgValue[] argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (maze == null) {
			sender.sendMessage(ChatColor.GRAY + "Well well well");
			return;
		}
		if (maze.getExits().isEmpty()) {
			sender.sendMessage(ChatColor.GRAY + "Mhh mhh mhh");
			return;
		}
		buildHandler.buildMaze(maze, sessionHandler.getSettings(playerId));
	}
	
	@Override
	public List<String> getTabList(String[] stringArgs) {
		List<String> tabList = super.getTabList(stringArgs);
		
		if (!tabList.isEmpty()) {
			return tabList;
		}
		String tabbedArg = stringArgs[stringArgs.length - 1];
		String factorString = "";
		String materialString = "";
		
		if (tabbedArg.contains("*")) {
			String[] stringParts = tabbedArg.split("\\*");
			
			if (stringParts.length < 1 || stringParts.length > 2 || !MathUtil.isInt(stringParts[0])) {
				return tabList;
			}
			factorString = stringParts[0] + "*";
			
			if (stringParts.length == 2) {
				materialString = stringParts[1];
			}
		} else if (MathUtil.isInt(tabbedArg)) {
			factorString = tabbedArg + "*";
		} else {
			materialString = tabbedArg;
		}
		for (String blockName : MatUtilsAquatic.getBlockNames()) {
			if (blockName.startsWith(materialString)) {
				tabList.add(factorString + blockName);
			}
		}
		return tabList;
	}
	
	
}
