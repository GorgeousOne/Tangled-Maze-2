package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.building.BlockPalette;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.maze.MazePart;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import me.gorgeousone.tangledmaze.util.MathUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BuildMazeCommand extends ArgCommand {
	
	private final SessionHandler sessionHandler;
	private final BuildHandler buildHandler;
	private final ToolHandler toolHandler;
	
	public BuildMazeCommand(SessionHandler sessionHandler,
	                        BuildHandler buildHandler, ToolHandler toolHandler) {
		super("build");
		addAlias("b");
		addFlag("floor");
		addFlag("roof");
		
		this.toolHandler = toolHandler;
		this.sessionHandler = sessionHandler;
		this.buildHandler = buildHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		Clip maze = sessionHandler.getMazeClip(playerId);
		
		if (maze == null) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		if (maze.getExits().isEmpty()) {
			Message.ERROR_EXIT_MISSING.sendTo(sender);
			return;
		}
		MazeSettings settings = sessionHandler.getSettings(playerId);
		MazePart mazePart = MazePart.WALLS;
		
		if (usedFlags.contains("floor")) {
			mazePart = MazePart.FLOOR;
		} else if (usedFlags.contains("roof")) {
			mazePart = MazePart.ROOF;
		}
		if (argValues.size() != 0) {
			try {
				BlockPalette palette = deserializeBlockPalette(argValues);
				settings.setPalette(palette, mazePart);
			} catch (TextException e) {
				e.sendTextTo(sender);
				e.printStackTrace();
			}
		}
		
		maze.setActive(false);
		toolHandler.resetClipTool(playerId);
		
		try {
			buildHandler.buildMaze(playerId, maze, settings, mazePart);
			buildHandler.buildMaze(playerId, maze, settings, MazePart.ROOF);
		}catch (TextException e) {
			e.sendTextTo(sender);
		}
	}
	
	public BlockPalette deserializeBlockPalette(List<ArgValue> stringArgs) throws TextException {
		BlockPalette palette = new BlockPalette();
		
		for (ArgValue input : stringArgs) {
			String inputString = input.get();
			int count = 1;
			String materialString;
			
			if (inputString.contains("*")) {
				String[] stringParts = inputString.split("\\*");
				
				if (stringParts.length < 1 || !MathUtil.isInt(stringParts[0])) {
					throw new IllegalArgumentException("invalid input: " + inputString);
				}
				count = Integer.parseInt(stringParts[0]);
				materialString = stringParts[1];
			} else {
				materialString = inputString;
			}
			try {
				BlockType blockType = BlockType.get(materialString);
				palette.addBlock(blockType, MathUtil.clamp(count, 1, 1000));
			} catch (IllegalArgumentException e) {
				throw new TextException(Message.ERROR_INVALID_BLOCK_NAME, new Placeholder("block", materialString));
			}
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
		String factorString;
		
		if (tabbedArg.contains("*")) {
			String[] stringParts = tabbedArg.split("\\*");
			
			if (stringParts.length < 1 || stringParts.length > 2 || !MathUtil.isInt(stringParts[0])) {
				return tabList;
			}
			factorString = stringParts[0] + "*";
			
		} else if (MathUtil.isInt(tabbedArg)) {
			factorString = tabbedArg + "*";
		} else {
			return MaterialUtil.getBlockNames();
		}
		
		for (String blockName : MaterialUtil.getBlockNames()) {
			tabList.add(factorString + blockName);
		}
		return tabList;
	}
}
