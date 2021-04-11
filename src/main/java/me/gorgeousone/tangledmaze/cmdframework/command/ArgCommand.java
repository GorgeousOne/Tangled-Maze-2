package me.gorgeousone.tangledmaze.cmdframework.command;

import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
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
	private final List<String> flags;
	
	public ArgCommand(String name) {
		super(name);
		arguments = new ArrayList<>();
		flags = new LinkedList<>();
	}
	
	public void addArg(Argument argument) {
		arguments.add(argument);
	}
	
	public void addFlag(String flagName) {
		flags.add("-" + flagName.toLowerCase());
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
		
		List<ArgValue> values = new ArrayList<>();
		Set<String> usedFlags = new HashSet<>();
		
		try {
			for (int i = 0; i < Math.max(inputCount, argCount); i++) {
				String input = i < inputCount ? stringArgs[i] : null;
				
				if (isFlag(input)) {
					usedFlags.add(matchFlag(input.toLowerCase()));
					continue;
				}
				values.add(i < argCount ? getArgs().get(i).createValue(input) : new ArgValue(input));
			}
		} catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			sendUsage(sender);
			return;
		}
		executeArgs(sender, values, usedFlags);
	}
	
	protected abstract void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags);
	
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
	
	protected String matchFlag(String input) {
		for (String flag : flags) {
			if (flag.equals(input)) {
				return flag.substring(1);
			}
		}
		throw new IllegalArgumentException("No flag '" + input + "' found.");
	}
	
	@Override
	public String getUsage() {
		StringBuilder usage = new StringBuilder(super.getUsage());
		
		for (Argument arg : getArgs()) {
			usage.append(" <");
			usage.append(arg.getName());
			usage.append(">");
		}
		for (String flag : flags) {
			usage.append(" ");
			usage.append(flag);
		}
		return usage.toString();
	}
	
	@Override
	public List<String> getTabList(String[] stringArgs) {
		String tabbedArg = stringArgs[stringArgs.length - 1];
		
		if (isFlag(tabbedArg)) {
			return flags;
		}
		if (this.arguments.size() >= stringArgs.length) {
			return this.arguments.get(stringArgs.length - 1).getTabList();
		}
		return new LinkedList<>();
	}
}
