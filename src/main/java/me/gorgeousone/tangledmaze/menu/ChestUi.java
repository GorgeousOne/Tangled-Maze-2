package me.gorgeousone.tangledmaze.menu;

import me.gorgeousone.tangledmaze.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChestUi {

	private final String title;
	private final int rows;
	private final Inventory inventory;
	private final Map<Integer, Consumer<Player>> clickActions;

	private final List<List<Integer>> radioGroups;
	private final Map<Integer, Integer> radioHighlights;


	public ChestUi(int rows, String title) {
		this.title = title;
		this.rows = rows;
		inventory = Bukkit.createInventory(null, rows * 9, title);
		clickActions = new HashMap<>();
		radioGroups = new ArrayList<>();
		radioHighlights = new HashMap<>();
	}

	public void setItem(int slot, ItemStack item, Consumer<Player> clickAction) {
		if (slot < 0 || slot >= rows * 9) {
			throw new IllegalArgumentException(String.format("%d is outside 0 and %d inventory slots", slot, rows * 9 - 1));
		}
		inventory.setItem(slot, item);
		clickActions.put(slot, clickAction);
	}

	public void toggleHighlight(int slot, boolean state) {
		if (state) {
			updateRadio(slot);
			ItemUtil.addMagicGlow(inventory.getItem(slot));
		} else {
			ItemUtil.removeMagicGlow(inventory.getItem(slot));
		}
	}

	/**
	 * If the clicked slot is part of a radio group this method removes magic glow from
	 * the previously "selected" item in the group.
	 */
	private void updateRadio(int slot) {
		for (int i = 0; i < radioGroups.size(); ++i) {
			List<Integer> radio = radioGroups.get(i);

			if (radio.contains(slot)) {
				ItemUtil.removeMagicGlow(inventory.getItem(radioHighlights.getOrDefault(i, slot)));
				radioHighlights.put(i, slot);
				break;
			}
		}
	}

	public void onClickSlot(Player player, int slot) {
		if (clickActions.containsKey(slot)) {
			clickActions.get(slot).accept(player);
		}
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}

	public void addRadio(int... slots) {
		radioGroups.add(Arrays.stream(slots).boxed().collect(Collectors.toList()));
	}

	@Override
	protected ChestUi clone() {
		ChestUi clone = new ChestUi(rows, title);
		clone.inventory.setContents(inventory.getContents());
		clone.clickActions.putAll(clickActions);
		clone.radioGroups.addAll(radioGroups);
		clone.radioHighlights.putAll(radioHighlights);
		return clone;

	}
}
