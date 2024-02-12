package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.TangledMazePlugin;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Message;
import org.bukkit.command.CommandSender;

/**
 * The command to reload the config settings and the language file of the plugin.
 */
public class ReloadCommand extends BaseCommand {
	
	private final TangledMazePlugin tangledMazePlugin;
	
	public ReloadCommand(TangledMazePlugin tangledMazePlugin) {
		super("reload");
		addAlias("rl");
		setPermission(Constants.RELOAD_PERM);
		this.tangledMazePlugin = tangledMazePlugin;
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		tangledMazePlugin.reload();
		Message.INFO_PLUGIN_RELOAD.sendTo(sender);
	}
}
