package me.gorgeousone.tangledmaze.cmdframework.handler;

import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Takes care of executing any registered command when it is being called
 */
public class CommandHandler implements CommandExecutor {
	
	private final JavaPlugin plugin;
	private final Set<BaseCommand> commands;
	private final CommandCompleter cmdCompleter;
	private final HashMap<UUID, LinkedList<QueuedCommand>> commandQueues;
	
	public CommandHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		this.commands = new HashSet<>();
		this.cmdCompleter = new CommandCompleter(this);
		this.commandQueues = new HashMap<>();
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
				queueCommand(command, sender, args);
				return true;
			}
		}
		return false;
	}
	
	void queueCommand(BaseCommand command, CommandSender sender, String[] args) {
		UUID senderId = BaseCommand.getSenderId(sender);
		
		if (!commandQueues.containsKey(senderId)) {
			commandQueues.put(senderId, new LinkedList<>());
		}
		LinkedList<QueuedCommand> queue = commandQueues.get(senderId);
		queue.add(new QueuedCommand(command, sender, args));
		
		if (queue.size() == 1) {
			executeNextCommand(senderId);
		}
	}
	
	void executeNextCommand(UUID senderId) {
		if (!commandQueues.containsKey(senderId)) {
			return;
		}
		LinkedList<QueuedCommand> queue = commandQueues.get(senderId);
		
		if (queue.isEmpty()) {
			commandQueues.remove(senderId);
			return;
		}
		QueuedCommand cmd = queue.getFirst();
		CommandSender sender = cmd.getSender();
		
		if (sender instanceof Player && !((Player) sender).isOnline()) {
			commandQueues.remove(senderId);
			return;
		}
		cmd.execute(() -> {
			queue.removeFirst();
			executeNextCommand(senderId);
		});
	}
}
