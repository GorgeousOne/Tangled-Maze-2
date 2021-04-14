package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.ClipShape;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.tool.ToolType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SwitchTool extends ArgCommand {
	
	private final ToolHandler toolHandler;
	
	public SwitchTool(ToolHandler toolHandler) {
		super("tool");
		addArg(new Argument("tool", ArgType.STRING, "rect", "circle"));
		this.toolHandler = toolHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String toolName = argValues.get(0).get();
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		switch (toolName) {
			case "rect":
			case "rectangle":
				toolHandler.setClipShape(playerId, ClipShape.RECTANGLE);
				break;
			case "circle":
				toolHandler.setClipShape(playerId, ClipShape.ELLIPSE);
				break;
			default:
				sender.sendMessage("invalid tool");
				return;
		}
	}
}
