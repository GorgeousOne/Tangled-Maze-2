package me.gorgeousone.tangledmaze.command;

import fr.black_eyes.lootchest.Main;
import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.loot.LootHandler;
import me.gorgeousone.tangledmaze.maze.MazeBackup;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import me.gorgeousone.tangledmaze.util.MathUtil;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpawnLootCommand extends ArgCommand {

	private static final String EXCEPTION_TEXT = ChatColor.RED + "'%type%' is can only be %value%.";

	private final SessionHandler sessionHandler;
	private final LootHandler lootHandler;
	private final boolean isAvailable;

	public SpawnLootCommand(SessionHandler sessionHandler, LootHandler lootHandler, boolean isAvailable) {
		super("loot");
		addArg(new Argument("action", ArgType.STRING, "spawn", "despawn"));

		this.sessionHandler = sessionHandler;
		this.lootHandler = lootHandler;
		this.isAvailable = isAvailable;
	}

	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
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
		if (!sessionHandler.isBuilt(maze)) {
			Message.INFO_MAZE_NOT_BUILT.sendTo(sender);
			return;
		}
		MazeBackup backup = sessionHandler.getBackup(maze);
		boolean isSpawn;

		try {
			isSpawn = getSpawnAction(argValues.get(0).get());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
		if (argValues.size() < 2) {
			sender.sendMessage("please specify chests to place (times*chestName)");
			return;
		}
		Map<String, Integer> chestAmounts;
		argValues.remove(0);

		try {
			chestAmounts = deserializeLootChestPrefabs(argValues);
			lootHandler.placeChests(backup.getMazeMap(), chestAmounts);
		} catch (TextException e) {
			e.sendTextTo(sender);
		}
	}

	private boolean getSpawnAction(String value) {
		if ("spawn".equals(value)) {
			return true;
		} else if ("despawn".equals(value)) {
			return false;
		}
		throw new IllegalArgumentException(EXCEPTION_TEXT
				.replace("%type%", "Action")
				.replace("%value%", String.join("/", getArgs().get(0).getTabList())));
	}

	/**
	 * Converts a list of material strings in the format "count(optional)*material:subId(optional)" into a BlockPalette.
	 *
	 * @param stringArgs the list of strings provided by the command sender
	 * @return the BlockPalette created from the strings
	 * @throws TextException if the strings are not in the correct format
	 */
	public Map<String, Integer> deserializeLootChestPrefabs(List<ArgValue> stringArgs) throws TextException {
		Map<String, Integer> chestAmounts = new HashMap<>();

		for (ArgValue input : stringArgs) {
			String inputString = input.get();
			int count = 1;
			String chestName;

			if (inputString.contains("*")) {
				String[] stringParts = inputString.split("\\*");

				if (stringParts.length < 1 || !MathUtil.isInt(stringParts[0])) {
					throw new IllegalArgumentException("invalid input: " + inputString);
				}
				count = Integer.parseInt(stringParts[0]);
				chestName = stringParts[1];
			} else {
				chestName = inputString;
			}
			if (!lootHandler.chestExists(chestName)) {
				//TODO add lang message
				throw new TextException(Message.ERROR_INVALID_BLOCK_NAME, new Placeholder("block", chestName + " (chest)"));
			}
			Bukkit.broadcastMessage("spawn " + count + " * " + chestName);
			chestAmounts.put(chestName, count);
		}
		return chestAmounts;
	}

	/**
	 * Returns a list of block names that the sender could tab complete to.
	 */
	@Override
	public List<String> getTabList(String[] stringArgs) {
		List<String> tabList = super.getTabList(stringArgs);

		if (!tabList.isEmpty()) {
			return tabList;
		}
		String tabbedArg = stringArgs[stringArgs.length - 1];
		String factorString;

		if (tabbedArg.contains("*")) {
			String[] stringParts = tabbedArg.split("\\*");

			if (stringParts.length < 1 || stringParts.length > 2 || !MathUtil.isInt(stringParts[0])) {
				return tabList;
			}
			factorString = stringParts[0] + "*";

		} else if (MathUtil.isInt(tabbedArg)) {
			factorString = tabbedArg + "*";
		} else {
			return MaterialUtil.getBlockNames();
		}

		for (String blockName : MaterialUtil.getBlockNames()) {
			tabList.add(factorString + blockName);
		}
		return tabList;
	}
}
