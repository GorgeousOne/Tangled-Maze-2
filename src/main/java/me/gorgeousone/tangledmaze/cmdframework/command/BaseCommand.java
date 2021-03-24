package me.gorgeousone.tangledmaze.cmdframework.command;

import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A command with no defined way of execution (not arguments or sub commands)
 */
public abstract class BaseCommand {
	
	private final String name;
	private final Set<String> aliases;
	
	public BaseCommand(String name) {
		this.name = name.toLowerCase();
		aliases = new HashSet<>();
		aliases.add(this.name);
	}
	
	public void execute(CommandSender sender, String[] args) {
		execute(sender, args, getName());
	}
	
	public abstract void execute(CommandSender sender, String[] args, String alias);
	
	public String getName() {
		return name;
	}
	
	public void addAlias(String alias) {
		aliases.add(alias.toLowerCase());
	}
	
	public boolean matchesAlias(String alias) {
		return aliases.contains(alias);
	}
	
	public void sendUsage(CommandSender sender) {
		sender.sendMessage("WRONG!");
	}
	
	public List<String> getTabList(String[] arguments) {
		return new LinkedList<>();
	}
}
