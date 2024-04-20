package me.gorgeousone.tangledmaze.menu;

import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.ItemUtil;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class that creates and manages command ui that can be opened by right clicking maze wand.
 */

public class UiHandler {

	private final ToolHandler toolHandler;
	private final Map<UUID, ChestUi> commandUis;
	private final ItemStack itemClipRect;
	private final ItemStack itemClipCirc;
	private final ItemStack itemClipTri;
	private final ItemStack itemToolExit;
	private final ItemStack itemToolBrush;
	private final ItemStack itemTp;
	private final ItemStack itemMazeSolve;
	private final ItemStack itemMazeStart;

	private final ItemStack itemClipCut;
	private final ItemStack itemClipAdd;


	public UiHandler(ToolHandler toolHandler) {
		this.toolHandler = toolHandler;
		this.commandUis = new HashMap<>();

		itemClipRect = ItemUtil.nameItem(Material.PAPER, "rect");
		itemClipCirc = ItemUtil.nameItem(Material.MAGMA_CREAM, "circ");
		itemClipTri = ItemUtil.nameItem(Material.PRISMARINE_SHARD, "tri");
		itemToolExit = ItemUtil.nameItem(MaterialUtil.match("DARK_OAK_DOOR", "DARK_OAK_DOOR_ITEM"), "exit");
		itemToolBrush = ItemUtil.nameItem(Material.FEATHER, "brush");
		itemTp = ItemUtil.nameItem(Material.ENDER_PEARL, "tp");
		itemMazeStart = ItemUtil.nameItem(Material.REDSTONE_BLOCK, "start");
		itemMazeSolve = ItemUtil.nameItem(Material.EMERALD_BLOCK, "solve");
		itemClipAdd = ItemUtil.nameItem(Material.IRON_PICKAXE, "add");
		itemClipCut = ItemUtil.nameItem(Material.SHEARS, "cut");
	}

	public void openMenu(Player player) {
		UUID playerId = player.getUniqueId();
		ChestUi ui = commandUis.computeIfAbsent(playerId, menu -> createCmdUi(playerId));

		ui.toggleHighlight(toolHandler.createClipTypeIfAbsent(playerId).ordinal(), true);
		ui.toggleHighlight(toolHandler.createToolIfAbsent(playerId).ordinal() + 4, true);

		ui.open(player);
	}

	private ChestUi createCmdUi(UUID playerId) {
		ChestUi ui = new ChestUi(3, Constants.UI_TITLE);
		ui.setItem(0, itemClipRect, p -> execNClose(p, "tm tool rectangle"));
		ui.setItem(1, itemClipCirc, p -> execNClose(p, "tm tool circle"));
		ui.setItem(2, itemClipTri, p -> execNClose(p, "tm tool triangle"));

		ui.setItem(4, itemToolExit, p -> execNClose(p, "tm tool exit"));
		ui.setItem(5, itemToolBrush, p -> execNClose(p, "tm tool brush"));

		ui.addRadio(0, 1, 2);
		ui.addRadio(4, 5);

		ui.setItem(8, itemTp, p -> execNClose(p, "tm tp"));
		ui.setItem(18, itemMazeStart, p -> execNClose(p, "tm start"));
		ui.setItem(19, itemMazeSolve, p -> execNClose(p, "tm solve"));
		ui.setItem(25, itemClipAdd, p -> execNClose(p, "tm add"));
		ui.setItem(26, itemClipCut, p -> execNClose(p, "tm cut"));

		return ui;
	}

	private void execNClose(Player player, String command) {
		player.closeInventory();
		player.performCommand(command);
	}

	public void handleClick(Player player, int slot) {
		UUID playerId = player.getUniqueId();
		commandUis.get(playerId).onClickSlot(player, slot);
	}

	public void remove(UUID playerId) {
		commandUis.remove(playerId);
	}
}
