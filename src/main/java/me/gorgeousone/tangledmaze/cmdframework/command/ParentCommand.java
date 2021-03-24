package me.gorgeousone.tangledmaze.cmdframework.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A command that can have other commands as arguments/children.
 */
public class ParentCommand extends BaseCommand {
	
	private final Set<BaseCommand> children;
	
	public ParentCommand(String name) {
		super(name);
		children = new HashSet<>();
	}
	
	public Set<BaseCommand> getChildren() {
		return children;
	}
	
	public void addChild(BaseCommand child) {
		children.add(child);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args, String alias) {
		
		if (args.length == 0) {
			sendUsage(sender);
		}
		
		for (BaseCommand child : children) {
			if (child.matchesAlias(args[0])) {
				child.execute(sender, Arrays.copyOfRange(args, 1, args.length), args[0]);
				return;
			}
		}
	}
	
	@Override
	public List<String> getTabList(String[] arguments) {
		List<String> tabList = new LinkedList<>();
		
		if (arguments.length == 1) {
			for (BaseCommand child : getChildren()) {
				tabList.add(child.getName());
			}
			return tabList;
		}
		
		for (BaseCommand child : getChildren()) {
			if (child.matchesAlias(arguments[0])) {
				return child.getTabList(Arrays.copyOfRange(arguments, 1, arguments.length));
			}
		}
		return tabList;
	}
}
