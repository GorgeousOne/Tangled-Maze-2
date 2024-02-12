package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The command to give a player the maze wand item specified in the config.
 */
public class GetWandCommand extends BaseCommand {
	
	public GetWandCommand() {
		super("wand");
		setPlayerRequired(true);
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		player.getInventory().addItem(new ItemStack(ConfigSettings.WAND_ITEM));
		Message.INFO_MAZE_WAND_USAGE.sendTo(player);
	}
}
