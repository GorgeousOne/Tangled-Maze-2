package me.gorgeousone.badapplemaze.cmdframework.handler;

import me.gorgeousone.badapplemaze.cmdframework.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.LinkedList;
import java.util.List;

/**
 * Tab completes any command registered in the command handler
 */
public class CommandCompleter implements TabCompleter {
	
	private final CommandHandler cmdHandler;
	
	public CommandCompleter(CommandHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		for (BaseCommand command : cmdHandler.getCommands()) {
			if (command.matchesAlias(cmd.getName())) {
				
				if (null != command.getPermission() && !sender.hasPermission(command.getPermission())) {
					continue;
				}
				List<String> tabList = new LinkedList<>();
				
				for (String tab : command.getTabList(args)) {
					if (tab.startsWith(args[args.length - 1])) {
						tabList.add(tab);
					}
				}
				return tabList;
			}
		}
		return null;
	}
}
