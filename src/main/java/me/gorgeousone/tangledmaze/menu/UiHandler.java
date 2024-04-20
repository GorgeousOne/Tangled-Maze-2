package me.gorgeousone.tangledmaze.menu;

import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.ItemUtil;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import me.gorgeousone.tangledmaze.util.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class that creates and manages command UI that can be opened by right-clicking the maze wand.
 */
public class UiHandler {

	private final ToolHandler toolHandler;
	private final Map<UUID, ChestUi> commandUis;
	private ChestUi uiTemplate;


	public UiHandler(ToolHandler toolHandler) {
		this.toolHandler = toolHandler;
		this.commandUis = new HashMap<>();
	}

	public void reloadLanguage() {
		commandUis.clear();
		createCmdUi();
	}

	public void openMenu(Player player) {
		UUID playerId = player.getUniqueId();
		ChestUi ui = commandUis.computeIfAbsent(playerId, menu -> uiTemplate.clone());

		ui.toggleHighlight(toolHandler.createClipTypeIfAbsent(playerId).ordinal(), true);
		ui.toggleHighlight(toolHandler.createToolIfAbsent(playerId).ordinal() + 4, true);
		ui.open(player);
	}

	public void handleClick(Player player, int slot) {
		UUID playerId = player.getUniqueId();
		commandUis.get(playerId).onClickSlot(player, slot);
	}

	private void createCmdUi() {
		uiTemplate = new ChestUi(3, Constants.UI_TITLE);
		setItem(0, Material.PAPER, Message.TOOL_RECT, p -> closeNExec(p, "tm tool rectangle"));
		setItem(1, Material.MAGMA_CREAM, Message.TOOL_CIRC, p -> closeNExec(p, "tm tool circle"));
		setItem(2, Material.PRISMARINE_SHARD, Message.TOOL_TRI, p -> closeNExec(p, "tm tool triangle"));

		setItem(4, MaterialUtil.match("DARK_OAK_DOOR_ITEM", "DARK_OAK_DOOR"), Message.TOOL_EXIT, p -> closeNExec(p, "tm tool exit"));
		setItem(5, MaterialUtil.match("BRUSH", "FEATHER"), Message.TOOL_BRUSH, p -> closeNExec(p, "tm tool brush"));

		setItem(8, Material.ENDER_PEARL, Message.COMMAND_TELEPORT, p -> closeNExec(p, "tm tp"));
		setItem(18, Material.REDSTONE_BLOCK, Message.COMMAND_START, p -> closeNExec(p, "tm start"));
		setItem(19, Material.EMERALD_BLOCK, Message.COMMAND_SOLVE, p -> closeNExec(p, "tm solve"));
		setItem(25, Material.IRON_PICKAXE, Message.COMMAND_ADD, p -> closeNExec(p, "tm add"));
		setItem(26, Material.SHEARS, Message.COMMAND_CUT, p -> closeNExec(p, "tm cut"));

		uiTemplate.addRadio(0, 1, 2);
		uiTemplate.addRadio(4, 5);
	}

	private void setItem(int slot, Material mat, String name, Consumer<Player> action) {
		uiTemplate.setItem(slot, ItemUtil.nameItem(mat, name), action);
	}

	private void setItem(int slot, Material mat, Text description, Consumer<Player> action) {
		List<String> lines = description.getParagraphs();
		String name = capitalize(lines.get(0));
		List<String> lore = lines.subList(1, lines.size());
		uiTemplate.setItem(slot, ItemUtil.nameItem(mat, name, lore), action);
	}


	private void closeNExec(Player player, String command) {
		player.closeInventory();
		player.performCommand(command);
	}

	public void remove(UUID playerId) {
		commandUis.remove(playerId);
	}


	private String capitalize(String input) {
		int i = findFirstNonColorCodeIndex(input);
		return input.substring(0, i) + Character.toUpperCase(input.charAt(i)) + input.substring(i + 1);
	}

	private int findFirstNonColorCodeIndex(String input) {
		boolean lastCharWasColorCode = false;
		for (int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);

			if (!lastCharWasColorCode && currentChar != '§') {
				return i;
			}
			lastCharWasColorCode = currentChar == '§';
		}
		return -1;
	}

}
