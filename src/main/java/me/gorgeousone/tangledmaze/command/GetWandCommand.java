package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetWandCommand extends BaseCommand {
	
	public GetWandCommand() {
		super("wand");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		player.getInventory().addItem(new ItemStack(ConfigSettings.WAND_ITEM));
		sender.sendMessage("left click: mark clipboard click: set exit left click: FTW");
	}
}
