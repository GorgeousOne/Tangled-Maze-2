package me.gorgeousone.tangledmaze.cmdframework.command;

import me.gorgeousone.tangledmaze.data.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
	private String permission;
	private boolean isPlayerRequired;
	
	public BaseCommand(String name) {
		this.name = name.toLowerCase();
		aliases = new HashSet<>();
		aliases.add(this.name);
	}
	
	public String getPermission() {
		return permission;
	}
	
	/**
	 * Sets a permission required to execute the command
	 * @param permission name of permission
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	public boolean isPlayerRequired() {
		return isPlayerRequired;
	}
	
	/**
	 * Sets whether this command can be executed over console/command block or not.
	 * @param playerRequired true if only players are allowed to execute this command
	 */
	public void setPlayerRequired(boolean playerRequired) {
		isPlayerRequired = playerRequired;
	}
	
	protected ParentCommand getParent() {
		return parent;
	}
	
	/**
	 * Sets a parent command for this command. This effects the string returned by {@link #getUsage()}
	 */
	public BaseCommand setParent(ParentCommand parent) {
		this.parent = parent;
		return this;
	}
	
	/**
	 * Executes the implemented functionality of this command if permissions and player requirements are met
	 * @param sender being that executed this command
	 * @param args further arguments input behind the command name
	 */
	public void execute(CommandSender sender, String[] args) {
		if (isPlayerRequired() && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
			return;
		}
		if (getPermission() != null && !sender.hasPermission(getPermission())) {
			Message.ERROR_MISSING_PERMISSION.sendTo(sender);
			return;
		}
		onCommand(sender, args);
	}
	
	/**
	 * Executes the functionality of the command
	 * @param sender being that executed this command
	 * @param args further arguments input behind the command name
	 */
	protected abstract void onCommand(CommandSender sender, String[] args);
	
	public String getName() {
		return name;
	}
	
	public void addAlias(String alias) {
		aliases.add(alias.toLowerCase());
	}
	
	public boolean matchesAlias(String alias) {
		return aliases.contains(alias);
	}
	
	/**
	 * Returns the pattern how to use this command
	 */
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
