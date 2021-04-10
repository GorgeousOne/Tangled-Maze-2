package me.gorgeousone.tangledmaze.cmdframework.command;

import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.argument.Flag;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A command with specifiable arguments
 */
public abstract class ArgCommand extends BaseCommand {
	
	protected final List<Argument> arguments;
	protected final Set<Flag> flags;
	private final List<String> flagNames;
	
	public ArgCommand(String name) {
		super(name);
		arguments = new ArrayList<>();
		flags = new HashSet<>();
		flagNames = new LinkedList<>();
	}
	
	public void addArg(Argument argument) {
		arguments.add(argument);
	}
	
	public void addFlag(Flag flag) {
		flags.add(flag);
		flagNames.add("-" + flag.getName());
		
		if (flag.getShortName() != null) {
			flagNames.add("-" + flag.getShortName());
		}
	}
	
	public List<Argument> getArgs() {
		return arguments;
	}
	
	/**
	 * Converts the passed sting arguments to the into ArgValues according to the beforehand defined Arguments
	 */
	@Override
	public void execute(CommandSender sender, String[] stringArgs) {
		int argCount = getArgs().size();
		int inputCount = stringArgs.length;
		
		ArgValue[] values = new ArgValue[Math.max(argCount, inputCount)];
		Set<String> usedFlags = new HashSet<>();
		
		try {
			int argIndex = 0;
			
			for (int i = 0; i < Math.max(inputCount, argCount); i++) {
				String input = i < inputCount ? stringArgs[i] : null;
				
				if (isFlag(input)) {
					usedFlags.add(matchFlag(input).getName());
					continue;
				}
				values[argIndex] = i < argCount ? getArgs().get(i).createValue(input) : new ArgValue(input);
				++argIndex;
			}
		} catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
		executeArgs(sender, values, usedFlags);
	}
	
	protected boolean isFlag(String input) {
		if (input == null || !input.startsWith("-")) {
			return false;
		}
		try {
			Double.valueOf(input);
		} catch (NumberFormatException e) {
			return true;
		}
		return false;
	}
	
	protected Flag matchFlag(String input) {
		String flagName = input.substring(1);
		
		for (Flag flag : flags) {
			if (flag.matches(flagName)) {
				return flag;
			}
		}
		throw new IllegalArgumentException("No flag '" + flagName + "' found.");
	}
	
	protected abstract void executeArgs(CommandSender sender, ArgValue[] argValues, Set<String> usedFlags);
	
	@Override
	public List<String> getTabList(String[] stringArgs) {
		String tabbedArg = stringArgs[stringArgs.length - 1];
		
		if (isFlag(tabbedArg)) {
			return flagNames;
		}
		
		if (this.arguments.size() >= stringArgs.length) {
			return this.arguments.get(stringArgs.length - 1).getTabList();
		}
		return new LinkedList<>();
	}
}
