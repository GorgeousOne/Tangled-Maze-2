package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.ClipShape;
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
		addArg(new Argument("tool", ArgType.STRING, "rect", "circle").setDefault("rect"));
		setPlayerRequired(true);
		
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
				if (toolHandler.setClipShape(playerId, ClipShape.RECTANGLE)) {
					Message.INFO_TOOL_SWITCH.sendTo(sender, new Placeholder("tool", "rectangle"));
				}
				break;
			case "circle":
				if (toolHandler.setClipShape(playerId, ClipShape.ELLIPSE)) {
					Message.INFO_TOOL_SWITCH.sendTo(sender, new Placeholder("tool", "circle"));
				}
				break;
			default:
				Message.ERROR_INVALID_TOOL.sendTo(sender);
				break;
		}
	}
}
