package me.gorgeousone.tangledmaze.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public final class ItemUtil {

	private ItemUtil() {}

	public static ItemStack nameItem(Material mat, String displayName, String... lore) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + displayName);

		if (lore.length > 0) {
			meta.setLore(Arrays.asList(lore));
		}
		item.setItemMeta(meta);
		return item;
	}

	public static void addMagicGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setDisplayName(meta.getDisplayName() + " [Selected]");
		item.setItemMeta(meta);
	}

	public static void removeMagicGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.removeEnchant(Enchantment.ARROW_INFINITE);
		meta.setDisplayName(meta.getDisplayName().replace(" [Selected]", ""));
		item.setItemMeta(meta);
	}
}
