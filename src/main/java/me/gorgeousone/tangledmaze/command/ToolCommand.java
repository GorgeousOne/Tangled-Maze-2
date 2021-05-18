package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.ClipType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ToolCommand extends ArgCommand {
	
	private final ToolHandler toolHandler;
	
	public ToolCommand(ToolHandler toolHandler) {
		super("tool");
		addArg(new Argument("tool", ArgType.STRING, "rect", "circle", "triangle").setDefault("rect"));
		setPlayerRequired(true);
		
		this.toolHandler = toolHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String toolArgument = argValues.get(0).get();
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		ClipType clipType;
		String toolName;
		
		switch (toolArgument) {
			case "rect":
			case "rectangle":
				clipType = ClipType.RECTANGLE;
				toolName = "rectangle";
				break;
			case "ellipse":
			case "circle":
				clipType = ClipType.ELLIPSE;
				toolName = "circle";
				break;
			case "triangle":
				clipType = ClipType.TRIANGLE;
				toolName = "triangle";
				break;
			default:
				Message.ERROR_INVALID_TOOL.sendTo(sender, new Placeholder("tool", toolArgument));
				return;
		}
		if (toolHandler.setClipType(playerId, clipType)) {
			Message.INFO_TOOL_SWITCH.sendTo(sender, new Placeholder("tool", toolName));
		}
	}
}
