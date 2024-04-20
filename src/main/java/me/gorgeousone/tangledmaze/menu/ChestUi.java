package me.gorgeousone.tangledmaze.menu;

import me.gorgeousone.tangledmaze.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChestUi {

	private final int rows;
	private final Inventory inventory;
	private final Map<Integer, Consumer<Player>> clickActions;

	public ChestUi(int rows, String title) {
		this.rows = rows;
		this.inventory = Bukkit.createInventory(null, rows * 9, title);
		this.clickActions = new HashMap<>();
	}

	public void setItem(int slot, ItemStack item, Consumer<Player> clickAction) {
		if (slot < 0 || slot >= rows * 9) {
			throw new IllegalArgumentException(String.format("%d is outside 0 and %d inventory slots", slot, rows * 9 - 1));
		}
		inventory.setItem(slot, item);
		clickActions.put(slot, clickAction);
	}

	public void toggleItemHighlight(int slot, boolean state) {
		if (state) {
			ItemUtil.addMagicGlow(inventory.getItem(slot));
		} else {
			ItemUtil.removeMagicGlow(inventory.getItem(slot));
		}
	}

	public void onClickSlot(Player player, int slot) {
		if (clickActions.containsKey(slot)) {
			player.closeInventory();
			clickActions.get(slot).accept(player);
		}
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}
}
