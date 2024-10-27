package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.ui.UiHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class UiListener implements Listener {

	private final UiHandler uiHandler;

	public UiListener(UiHandler uiHandler) {
		this.uiHandler = uiHandler;
	}

	@EventHandler
	public void onUiClick(InventoryClickEvent event) {
		if (!event.getView().getTitle().equals(Constants.UI_TITLE)) {
			return;
		}
		event.setCancelled(true);
		Player player = (Player) event.getWhoClicked();
		uiHandler.handleClick(player, event.getSlot());
	}
}
