package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SettingsCommand extends ArgCommand {
	
	private final SessionHandler sessionHandler;
	
	public SettingsCommand(SessionHandler sessionHandler) {
		super("setting");
		addArg(new Argument("setting", ArgType.STRING, MazeProperty.commandNames()));
		addArg(new Argument("integer", ArgType.INTEGER));
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		MazeProperty property = MazeProperty.match(argValues.get(0).get());
		if (property == null) {
			sender.sendMessage("invalid setting");
			return;
		}
		int inputValue = argValues.get(1).getInt();
		MazeSettings settings = sessionHandler.getSettings(playerId);
		sender.sendMessage("Set " + property.textName() + " to " + settings.setValue(property, inputValue) + " blocks(s).");
	}
}
