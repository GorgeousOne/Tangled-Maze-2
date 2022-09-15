package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.clip.ClipType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.tool.ToolType;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ToolCommand extends ArgCommand {
	
	private final ToolHandler toolHandler;
	
	public ToolCommand(ToolHandler toolHandler) {
		super("tool");
		addArg(new Argument("tool", ArgType.STRING, getToolNames()));
		setPlayerRequired(true);
		
		this.toolHandler = toolHandler;
	}
	
	private String[] getToolNames() {
		List<String> toolNames = new LinkedList<>();
		
		for (ClipType clipType : ClipType.values()) {
			toolNames.add(clipType.getAliases()[0]);
		}
		for (ToolType toolType : ToolType.values()) {
			toolNames.add(toolType.getAliases()[0]);
		}
		return toolNames.toArray(new String[toolNames.size()]);
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String toolArgument = argValues.get(0).get();
		
		if (trySetClipType(toolArgument, sender)) {
			return;
		}
		if (!trySetToolType(toolArgument, sender)) {
			Message.ERROR_INVALID_TOOL.sendTo(sender, new Placeholder("tool", toolArgument));
		}
	}
	
	/**
	 * Tries to deserialize and set a ClipType
	 *
	 * @param clipArgument
	 * @return true if clipArgument matched a ClipType
	 */
	private boolean trySetClipType(String clipArgument, CommandSender sender) {
		ClipType clipType = ClipType.match(clipArgument);
		
		if (null == clipType) {
			return false;
		}
		if (toolHandler.setClipType(getSenderId(sender), clipType)) {
			Message.INFO_TOOL_SWITCH.sendTo(sender, new Placeholder("tool", clipType.getName()));
		}
		return true;
	}
	
	private boolean trySetToolType(String toolArgument, CommandSender sender) {
		ToolType toolType = ToolType.match(toolArgument);
		
		if (null == toolType) {
			return false;
		}
		if (toolHandler.setToolType(getSenderId(sender), toolType)) {
			Message.INFO_TOOL_SWITCH.sendTo(sender, new Placeholder("tool", toolType.getName()));
		}
		return true;
	}
}
