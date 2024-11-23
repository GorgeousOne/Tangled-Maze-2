package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.maze.MazeProperty;
import me.gorgeousone.tangledmaze.maze.MazeSettings;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The command to change build settings of a maze.
 */
public class SettingsCommand extends ArgCommand {
	
	private final SessionHandler sessionHandler;
	
	public SettingsCommand(SessionHandler sessionHandler) {
		super("setting");
		addArg(new Argument("setting", ArgType.STRING, MazeProperty.commandNames()));
		addArg(new Argument("integer", ArgType.INTEGER).setDefault("-1"));
		this.sessionHandler = sessionHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		UUID playerId = getSenderId(sender);
		String settingName = argValues.get(0).get();
		MazeProperty property = MazeProperty.match(settingName);
		
		if (null == property) {
			Message.ERROR_INVALID_SETTING.sendTo(sender, new Placeholder("setting", settingName));
			return;
		}
		MazeSettings settings = sessionHandler.getSettings(playerId);
		int inputValue = argValues.get(1).getInt();

		if (inputValue == -1) {
			int currentValue = settings.getValue(property);
			Message.INFO_SETTING_INFO.sendTo(
					sender,
					new Placeholder("setting", property.textName()),
					new Placeholder("value", currentValue));
			if (property == MazeProperty.SEED) {
				Message.INFO_SETTING_SEED_INFO.sendTo(sender);
			}
			return;
		}
		int outputValue = settings.setValue(property, inputValue);
		Message.INFO_SETTING_CHANGE.sendTo(
				sender,
				new Placeholder("setting", property.textName()),
				new Placeholder("value", outputValue));
		if (property == MazeProperty.SEED) {
			Message.INFO_SETTING_SEED_INFO.sendTo(sender);
		}
	}
}
