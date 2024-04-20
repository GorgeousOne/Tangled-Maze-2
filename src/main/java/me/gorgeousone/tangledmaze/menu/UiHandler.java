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
		ChestUi ui = commandUis.computeIfAbsent(playerId, menu -> createCmdUi());
		ui.open(player);
	}

	private ChestUi createCmdUi() {
		ChestUi ui = new ChestUi(3, Constants.UI_TITLE);
		ui.setItem(0, itemClipRect, player -> {
			player.performCommand("tm tool rectangle");
		});
		ui.setItem(1, itemClipCirc, player -> {
			player.performCommand("tm tool circle");
		});
		ui.setItem(2, itemClipTri, player -> {
			player.performCommand("tm tool triangle");
		});
		ui.setItem(4, itemToolExit, player -> {
			player.performCommand("tm tool exit");
		});
		ui.setItem(5, itemToolBrush, player -> {
			player.performCommand("tm tool brush");
		});
		ui.setItem(8, itemTp, player -> {
			player.performCommand("tm tp");
		});
		ui.setItem(18, itemMazeStart, player -> {
			player.performCommand("tm start");
		});
		ui.setItem(19, itemMazeSolve, player -> {
			player.performCommand("tm solve");
		});
		ui.setItem(25, itemClipAdd, player -> {
			player.performCommand("tm add");
		});
		ui.setItem(26, itemClipCut, player -> {
			player.performCommand("tm cut");
		});

		return ui;
	}

	public void handleClick(Player player, int slot) {
		UUID playerId = player.getUniqueId();
		commandUis.get(playerId).onClickSlot(player, slot);
	}

	public void remove(UUID playerId) {
		commandUis.remove(playerId);
	}
}
