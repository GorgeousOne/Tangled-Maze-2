package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Flag;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.generation.building.BlockPalette;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import me.gorgeousone.tangledmaze.util.MathUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
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
		MazeSettings settings = sessionHandler.getSettings(playerId);
		MazePart mazePart = MazePart.WALLS;
		
		if (usedFlags.contains("floor")) {
			mazePart = MazePart.FLOOR;
		}else if (usedFlags.contains("roof")) {
			mazePart = MazePart.ROOF;
		}
		if (argValues.length != 0) {
			try {
				BlockPalette palette = deserializeBlockPalette(argValues);
				sender.sendMessage("created " + palette.size() + " blocks for " + mazePart.name().toLowerCase());
				settings.setPalette(palette, mazePart);
			} catch (Exception e) {
				sender.sendMessage(e.getMessage());
			}
		}
		buildHandler.buildMaze(maze, settings, mazePart);
	}
	
	public BlockPalette deserializeBlockPalette(ArgValue[] stringArgs) {
		BlockPalette palette = new BlockPalette();
		
		for (ArgValue input : stringArgs) {
			String inputString = input.get();
			String countString = "";
			String materialString = "";
			
			if (inputString.contains("*")) {
				String[] stringParts = inputString.split("\\*");
				
				if (stringParts.length < 1 || stringParts.length > 2) {
					throw new IllegalArgumentException("invalid input: " + inputString);
				}
				if (!MathUtil.isInt(stringParts[0])) {
					throw new IllegalArgumentException("invalid multiplier: " + inputString);
				}
				countString = stringParts[0];
				
				if (stringParts.length == 2) {
					materialString = stringParts[1];
				}
			} else if (MathUtil.isInt(inputString)) {
				countString = inputString;
			} else {
				materialString = inputString;
			}
			BlockType blockType;
			try {
				blockType = BlockType.deserialize(materialString);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("invalid block: " + materialString);
			}
			int count = MathUtil.clamp(Integer.parseInt(countString), 1, 1000);
			palette.addBlock(blockType, count);
		}
		return palette;
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
		for (String blockName : MaterialUtil.getBlockNames()) {
			if (blockName.startsWith(materialString)) {
				tabList.add(factorString + blockName);
			}
		}
		return tabList;
	}
}
