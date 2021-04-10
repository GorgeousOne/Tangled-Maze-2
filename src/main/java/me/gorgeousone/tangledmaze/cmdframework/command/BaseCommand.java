package me.gorgeousone.tangledmaze.cmdframework.command;

import org.bukkit.ChatColor;
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
	private ParentCommand parent;
	
	public BaseCommand(String name) {
		this.name = name.toLowerCase();
		
		aliases = new HashSet<>();
		aliases.add(this.name);
	}
	
	protected ParentCommand getParent() {
		return parent;
	}
	
	public BaseCommand setParent(ParentCommand parent) {
		this.parent = parent;
		return this;
	}
	
	public abstract void execute(CommandSender sender, String[] args);
	
	public String getName() {
		return name;
	}
	
	public void addAlias(String alias) {
		aliases.add(alias.toLowerCase());
	}
	
	public boolean matchesAlias(String alias) {
		return aliases.contains(alias);
	}
	
	public String getUsage() {
		if (getParent() != null) {
			return getParent().getParentUsage() + " " + getName();
		}
		return ChatColor.RED + "/" + getName();
	}
	
	public void sendUsage(CommandSender sender) {
		sender.sendMessage(getUsage());
	}
	
	public List<String> getTabList(String[] arguments) {
		return new LinkedList<>();
	}
}
