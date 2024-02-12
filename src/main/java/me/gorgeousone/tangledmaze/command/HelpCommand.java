package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.util.MathUtil;
import me.gorgeousone.tangledmaze.util.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

/**
 * The command to display a help page of all commands or a specific command if a page number is given.
 */
public class HelpCommand extends ArgCommand {
	
	public HelpCommand() {
		super("help");
		addAlias("h");
		addAlias("?");
		addArg(new Argument("page", ArgType.INTEGER).setDefault("0"));
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Text[] helpPages = Message.helpPages();
		int pageIndex = MathUtil.clamp(argValues.get(0).getInt(), 0, helpPages.length);
		sender.sendMessage(Constants.prefix + "--- Help Pages --- " + ChatColor.GREEN + pageIndex + "/" + helpPages.length);
		
		if (pageIndex == 0) {
			sender.sendMessage(ChatColor.GREEN + "List of commands: ");
			
			for (int i = 0; i < helpPages.length; ++i) {
				sender.sendMessage(ChatColor.GREEN + "page " + (i + 1) + " " + helpPages[i].getParagraphs().get(0));
			}
			sender.sendMessage(ChatColor.YELLOW + "Use /maze help <page> to see details");
			
		} else {
			helpPages[pageIndex - 1].sendTo(sender);
		}
		sender.sendMessage("");
	}
}
