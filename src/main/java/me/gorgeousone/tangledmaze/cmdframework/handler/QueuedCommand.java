package me.gorgeousone.tangledmaze.cmdframework.handler;

import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.command.CommandSender;

public class QueuedCommand {
	
	private final BaseCommand command;
	private final CommandSender sender;
	private final String[] args;
	
	public QueuedCommand(BaseCommand command, CommandSender sender, String[] args) {
		this.command = command;
		this.sender = sender;
		this.args = args;
	}
	
	public BaseCommand getCommand() {
		return command;
	}
	
	public CommandSender getSender() {
		return sender;
	}
	
	public String[] getArgs() {
		return args;
	}
	
	public void execute(Runnable callback) {
		command.execute(sender, args, callback);
	}
}
