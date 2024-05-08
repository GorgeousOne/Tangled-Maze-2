package me.gorgeousone.tangledmaze.loot;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class LootRemoveCommand extends BaseCommand {

	private final SessionHandler sessionHandler;
	private final LootHandler lootHandler;
	private final boolean isAvailable;

	public LootRemoveCommand(SessionHandler sessionHandler, LootHandler lootHandler, boolean isAvailable) {
		super("remove");
		this.sessionHandler = sessionHandler;
		this.lootHandler = lootHandler;
		this.isAvailable = isAvailable;
	}

	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		if (!isAvailable) {
			Message.ERROR_INVALID_SETTING.sendTo(sender, new Placeholder("setting", "LootChest plugin not loaded"));
			return;
		}
		UUID playerId = getSenderId(sender);
		Clip maze = sessionHandler.getMazeClip(playerId);

		if (null == maze) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		int removedChestCount = lootHandler.removeChests(maze);
		sender.sendMessage("remove " + removedChestCount + " chests");
	}
}
