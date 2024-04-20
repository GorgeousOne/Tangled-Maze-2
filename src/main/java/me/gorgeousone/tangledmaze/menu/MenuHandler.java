package me.gorgeousone.tangledmaze.menu;

import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.ItemUtil;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuHandler {

	private final ToolHandler toolHandler;
	private final Map<UUID, ChestUi> playerMenus;
	private ItemStack itemClipRect;
	private ItemStack itemClipCirc;
	private ItemStack itemClipTri;
	private ItemStack itemToolExit;
	private ItemStack itemToolBrush;
	private ItemStack itemTp;

	public MenuHandler(ToolHandler toolHandler) {
		this.toolHandler = toolHandler;
		this.playerMenus = new HashMap<>();

		System.out.println("TODO add menu to lang");
		itemClipRect = ItemUtil.nameItem(Material.PAPER, "rect");
		itemClipCirc = ItemUtil.nameItem(Material.SLIME_BALL, "circ");
		itemClipTri = ItemUtil.nameItem(Material.PRISMARINE_SHARD, "tri");
		itemToolExit = ItemUtil.nameItem(MaterialUtil.match("OAK_DOOR", "WOODEN_DOOR"), "exit");
		itemToolBrush = ItemUtil.nameItem(Material.FEATHER, "brush");
		itemTp = ItemUtil.nameItem(Material.ENDER_PEARL, "");
	}

	public void openMenu(Player player) {
		UUID playerId = player.getUniqueId();
		ChestUi ui = playerMenus.computeIfAbsent(playerId, menu -> createMenu(playerId));
		ui.open(player);
	}

	private ChestUi createMenu(UUID playerId) {
		ChestUi ui = new ChestUi(2);
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
		return ui;
	}
}
