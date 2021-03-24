package me.gorgeousone.tangledmaze.cmdframework.handler;

import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Takes care of executing any registered command when it is being called
 */
public class CommandHandler implements CommandExecutor {
	
	private final JavaPlugin plugin;
	private final Set<BaseCommand> commands;
	private final CommandCompleter cmdCompleter;
	
	public CommandHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		this.commands = new HashSet<>();
		this.cmdCompleter = new CommandCompleter(this);
	}
	
	public void registerCommand(BaseCommand command) {
		commands.add(command);
		plugin.getCommand(command.getName()).setExecutor(this);
		plugin.getCommand(command.getName()).setTabCompleter(cmdCompleter);
	}
	
	public Set<BaseCommand> getCommands() {
		return commands;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String cmdName = cmd.getName();
		
		for (BaseCommand command : commands) {
			if (command.matchesAlias(cmdName)) {
				command.execute(sender, args);
				return true;
			}
		}
		return false;
	}
}