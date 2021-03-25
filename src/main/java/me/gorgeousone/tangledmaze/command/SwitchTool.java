package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.tool.ToolType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SwitchTool extends ArgCommand {
	
	private final ToolHandler toolHandler;
	
	public SwitchTool(ToolHandler toolHandler) {
		super("tool");
		addArg(new Argument("tool", ArgType.STRING, ToolType.getNames()));
		this.toolHandler = toolHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, ArgValue[] argValues, String alias) {
		String toolName = argValues[0].get().toUpperCase();
		Player player = (Player) sender;
		ToolType tool;
		
		try {
			tool = ToolType.valueOf(toolName);
		}catch (IllegalArgumentException e) {
			sender.sendMessage("nah pal");
			return;
		}
		boolean switchedTool = toolHandler.setTool(player.getUniqueId(), tool);
		
		if (switchedTool) {
			player.sendMessage("Switched to the most sublime " + tool.getName());
		}
	}
}
