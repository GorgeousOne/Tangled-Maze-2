package me.gorgeousone.tangledmaze.loot;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.util.BlockVec;
import me.gorgeousone.tangledmaze.util.MathUtil;
import me.gorgeousone.tangledmaze.util.text.Placeholder;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LootSpawnCommand extends ArgCommand {

	private static final String EXCEPTION_TEXT = ChatColor.RED + "'%type%' is can only be %value%.";

	private final SessionHandler sessionHandler;
	private final LootHandler lootHandler;
	private final boolean isAvailable;

	public LootSpawnCommand(SessionHandler sessionHandler, LootHandler lootHandler, boolean isAvailable) {
		super("spawn");
		addArg(new Argument("x*chest...", ArgType.STRING));
		addFlag("hallways");
		addFlag("deadends");
		addFlag("rooms");

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
		Map<String, Integer> chestAmounts;
		Map<String, BlockVec> addedChests;
		boolean isLootInHallways = usedFlags.contains("hallways");
		boolean isLootInDeadEnds = usedFlags.contains("deadends");
		boolean isLootInRooms = usedFlags.contains("rooms");

		if (!isLootInHallways && !isLootInDeadEnds && !isLootInRooms) {
			isLootInHallways = isLootInDeadEnds = isLootInRooms = true;
		}
		try {
			chestAmounts = deserializeLootChestNames(argValues);
			addedChests = lootHandler.spawnChests(maze, chestAmounts, isLootInHallways, isLootInDeadEnds, isLootInRooms);
		} catch (TextException e) {
			e.sendTextTo(sender);
			return;
		}
		//TODO jsonfy
		sender.sendMessage("Placed " + addedChests.size() + " chests");
	}

	/**
	 * Converts a list of material strings in the format "count(optional)*material:subId(optional)" into a BlockPalette.
	 *
	 * @param stringArgs the list of strings provided by the command sender
	 * @return the BlockPalette created from the strings
	 * @throws TextException if the strings are not in the correct format
	 */
	public Map<String, Integer> deserializeLootChestNames(List<ArgValue> stringArgs) throws TextException {
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

		if (!isAvailable) {
			return tabList;
		}
		List<String> chestNames = lootHandler.getChestNames();

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
			return chestNames;
		}
		for (String chestName : chestNames) {
			tabList.add(factorString + chestName);
		}
		return tabList;
	}
}
