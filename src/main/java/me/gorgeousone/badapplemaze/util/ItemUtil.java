package me.gorgeousone.badapplemaze.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemUtil {

	private static final String TAG_SELECTED = ChatColor.GRAY + " [Selected]";
	private ItemUtil() {}

	public static ItemStack nameItem(Material mat, String displayName, String... lore) {
		return nameItem(mat, displayName, Arrays.asList(lore));
	}

	public static ItemStack nameItem(Material mat, String displayName, List<String> lore) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + displayName);

		if (!lore.isEmpty()) {
			meta.setLore(softWrapLines(lore, 30));
		}
		item.setItemMeta(meta);
		return item;
	}


	private static List<String> softWrapLines(List<String> lore, int limit) {
		List<String> wrappedLore = new ArrayList<>();

		for (String line : lore) {
			String[] words = line.split(" ");
			String buffer = "";

			for (String word : words) {
				buffer += word + " ";

				if (ChatColor.stripColor(buffer).trim().length() > limit) {
					wrappedLore.add(buffer);
					buffer = ChatColor.getLastColors(buffer);
				}
			}
			if (!buffer.isEmpty()) {
				wrappedLore.add(buffer);
			}
		}
		return wrappedLore;
	}

	public static void addMagicGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setDisplayName(meta.getDisplayName() + TAG_SELECTED);
		item.setItemMeta(meta);
	}

	public static void removeMagicGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.removeEnchant(Enchantment.ARROW_INFINITE);
		meta.setDisplayName(meta.getDisplayName().replace(TAG_SELECTED, ""));
		item.setItemMeta(meta);
	}
}
