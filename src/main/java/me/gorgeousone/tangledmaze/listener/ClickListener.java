package me.gorgeousone.tangledmaze.listener;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.clip.ClipActionFactory;
import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.clip.ClipType;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.ui.UiHandler;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.render.RenderSession;
import me.gorgeousone.tangledmaze.tool.ClipTool;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A class for handling player interactions with the maze wand item or any block.
 * It handles the creation of gold block clips and the display of fake block renders.
 */
public class ClickListener implements Listener {

	private final JavaPlugin plugin;
	private final SessionHandler sessionHandler;
	private final ToolHandler toolHandler;
	private final RenderHandler renderHandler;
	private final UiHandler uiHandler;

	public ClickListener(JavaPlugin plugin, SessionHandler sessionHandler,
	                     ToolHandler toolHandler,
	                     RenderHandler renderHandler, UiHandler uiHandler) {
		this.plugin = plugin;
		this.sessionHandler = sessionHandler;
		this.toolHandler = toolHandler;
		this.renderHandler = renderHandler;
		this.uiHandler = uiHandler;
	}

	/**
	 * Adds block clicked with a golden shovel as vertex to the clip tool of the player
	 */
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (!isMainHand(event) || !player.hasPermission(Constants.BUILD_PERM)) {
			return;
		}
		ItemStack heldItem = getHeldItem(player);
		Block clickedBlock = event.getClickedBlock();

		if (!isMazeWand(heldItem)) {
			if (clickedBlock != null) {
				hideClickedRender(player, clickedBlock);
			}
			return;
		}
		event.setCancelled(true);

		if (!isLeftClick(event.getAction())) {
			uiHandler.openMenu(player);
			return;
		}
		Block tracedBlock = traceBlock(player, clickedBlock);

		if (tracedBlock == null) {
			return;
		}
		handleWandClick(player, tracedBlock);

		if (event.getClickedBlock() != null) {
			updateClickedBlocks(player, event.getClickedBlock());
		}
	}

	private boolean isMainHand(PlayerInteractEvent event) {
		try {
			return event.getHand() == EquipmentSlot.HAND;
		} catch (NoSuchMethodError legacyError) {
			return true;
		}
	}

	private ItemStack getHeldItem(Player player) {
		try {
			return player.getInventory().getItemInMainHand();
		} catch (NoSuchMethodError legacyError) {
			return player.getItemInHand();
		}
	}

	private boolean isLeftClick(Action action) {
		return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
	}

	/**
	 * Hides all fake block clip renders of a player if the player interacts with any block of them
	 *
	 * @param player
	 * @param clickedBlock
	 */
	private void hideClickedRender(Player player, Block clickedBlock) {
		UUID playerId = player.getUniqueId();
		RenderSession render = renderHandler.getPlayerRender(playerId);

		//TODO detect click on maze solution render / any rendered block
		if (render != null && render.containsVisible(clickedBlock)) {
			render.hide();
		}
	}

	/**
	 * Returns hovered clicked block if hover clicking enabled. Otherwise
	 *
	 * @param player
	 * @param clickedBlock
	 * @return
	 */
	private Block traceBlock(Player player, Block clickedBlock) {
		if (clickedBlock == null && ConfigSettings.HOVER_CLICKING_ENABLED) {
			BlockIterator iter = new BlockIterator(player, ConfigSettings.HOVER_RANGE);

			while (iter.hasNext()) {
				Block nextBlock = iter.next();

				if (nextBlock.getType().isSolid()) {
					return nextBlock;
				}
			}
		}
		return clickedBlock;
	}

	private void handleWandClick(Player player, Block clickedBlock) {
		UUID playerId = player.getUniqueId();
		ClipTool clipTool = toolHandler.createClipToolIfAbsent(playerId);
		Clip clip = sessionHandler.getClip(playerId);
		Clip maze = sessionHandler.getMazeClip(playerId);

		if (isClipToolUsed(clipTool, clip, maze, clickedBlock)) {
			addVertexToClipTool(playerId, clipTool, clickedBlock);
			return;
		}
		switch (toolHandler.createToolIfAbsent(playerId)) {
			case EXIT_SETTER:
				if (ClipActionFactory.canBeExit(maze, new Vec2(clickedBlock))) {
					maze.toggleExit(clickedBlock);
				}
				break;
			case BRUSH:
				double angle = player.getLocation().getYaw();
				maze.processAction(ClipActionFactory.brushBorder(maze, clickedBlock, angle), true);
				break;
		}
	}


	/**
	 * Returns true if the clip tool should be used for the clicked block.
	 * Returns false if nothing stands in the way of using brush or exit setter tool.
	 */
	private boolean isClipToolUsed(ClipTool clipTool, Clip clip, Clip maze, Block clickedBlock) {
		return maze == null
				|| !maze.isBorderBlock(clickedBlock)    //no redstone border clicked
				|| (clip != null && clip.isBorderBlock(clickedBlock))   //gold clip border clicked
				|| clipTool.getVertices().contains(clickedBlock)    //lapis clip vertex clicked
				|| (!clipTool.getVertices().isEmpty() && !clipTool.isComplete()) //clip creation in progress
				|| clipTool.getVertexToRelocate() != -1;   //vertex relocation in progress
	}

	private void addVertexToClipTool(UUID playerId, ClipTool clipTool, Block clickedBlock) {
		ClipType clipType = toolHandler.createClipTypeIfAbsent(playerId);

		if (clipTool.getShape() != clipType) {
			sessionHandler.removeClip(playerId, true);
			toolHandler.resetClipTool(playerId);
			clipTool.setType(clipType);
		}
		clipTool.addVertex(clickedBlock);
	}

	private void updateClickedBlocks(Player player, Block clickedBlock) {
		RenderSession render = renderHandler.getPlayerRender(player.getUniqueId());

		if (render == null) {
			return;
		}
		Set<Vec2> updatedBlocks = new HashSet<>();
		updatedBlocks.add(new Vec2(clickedBlock));

		for (Direction dir : Direction.CARDINALS) {
			updatedBlocks.add(new Vec2(clickedBlock.getRelative(dir.getX(), 0, dir.getZ())));
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				render.redisplayBlocks(updatedBlocks);
			}
		}.runTaskLater(plugin, 2);
	}

	/**
	 * Creates a clip for the player when a clip tool reaches the required amount of vertices for it.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onClipToolChange(ClipToolChangeEvent event) {
		ClipTool tool = event.getTool();
		UUID playerId = tool.getPlayerId();

		switch (event.getCause()) {
			case COMPLETE:
			case RESIZE_FINISH:
				sessionHandler.setClip(playerId, ClipFactory.createClip(playerId, tool.getVertices(), tool.getShape()));
				break;
			case RESTART:
				sessionHandler.removeClip(playerId, false);
			case PROGRESS:
				break;
		}
	}

	/**
	 * Show clip renders if player selects wand item in hotbar
	 *
	 * @param event
	 */
	@EventHandler
	public void onSlotSwitch(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

		if (!isMazeWand(newItem)) {
			return;
		}
		RenderSession render = renderHandler.getPlayerRender(player.getUniqueId());

		if (null != render) {
			render.show();
		}
	}

	/**
	 * Returns if the given ItemStack is a wand for maze creation
	 */
	boolean isMazeWand(ItemStack item) {
		return item != null && item.getType() == ConfigSettings.WAND_ITEM;
	}
}
