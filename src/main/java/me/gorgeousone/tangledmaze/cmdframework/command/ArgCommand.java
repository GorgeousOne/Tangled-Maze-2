package me.gorgeousone.tangledmaze.cmdframework.command;

import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

/**
 * A command with specifiable arguments
 */
public abstract class ArgCommand extends BaseCommand {
	
	protected List<Argument> arguments;
	
	protected ArgCommand(String name) {
		super(name);
	}
	
	public void addArg(Argument argument) {
		arguments.add(argument);
	}
	
	public List<Argument> getArgs() {
		return arguments;
	}
	
	/**
	 * Converts the passed sting arguments to the into ArgValues according to the beforehand defined Arguments
	 */
	@Override
	public void execute(CommandSender sender, String[] args, String alias) {
		int argCount = getArgs().size();
		int stringCount = args.length;
		ArgValue[] values = new ArgValue[Math.max(argCount, stringCount)];
		
		try {
			if (stringCount >= argCount) {
				for (int i = 0; i < stringCount; i++) {
					values[i] = getArgs().get(i).createValue(args[i]);
				}
			} else {
				for (int i = 0; i < argCount; i++) {
					values[i] = getArgs().get(i).createValue(i < stringCount ? args[i] : null);
				}
			}
		}catch (IllegalArgumentException ex) {
			sender.sendMessage(ex.getMessage());
			return;
		}
		executeArgs(sender, values, alias);
	}
	
	protected abstract void executeArgs(CommandSender sender, ArgValue[] argValues, String alias);
	
	@Override
	public List<String> getTabList(String[] arguments) {
		if (this.arguments.size() < arguments.length) {
			return new LinkedList<>();
		}
		return this.arguments.get(arguments.length - 1).getTabList();
	}
}
