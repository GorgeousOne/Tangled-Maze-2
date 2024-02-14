package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.SessionHandler;
import me.gorgeousone.tangledmaze.clip.Clip;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The command to teleport a player to the location of the maze they are currently editing.
 */
public class TeleportCommand extends BaseCommand {
	
	private final SessionHandler sessionHandler;
	private final RenderHandler renderHandler;
	
	public TeleportCommand(SessionHandler sessionHandler, RenderHandler renderHandler) {
		super("teleport");
		addAlias("tp");
		
		setPermission(Constants.TP_PERM);
		setPlayerRequired(true);
		
		this.sessionHandler = sessionHandler;
		this.renderHandler = renderHandler;
	}
	
	public void onCommand(CommandSender sender, String[] arguments) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		
		Clip maze = this.sessionHandler.getMazeClip(player.getUniqueId());
		
		if (maze == null) {
			Message.ERROR_MAZE_MISSING.sendTo(sender);
			return;
		}
		Vec2 firstBorder = maze.getBorder().iterator().next();
		Location tpLoc = new Location(
				maze.getWorld(),
				firstBorder.getX() + 0.5,
				maze.getY(firstBorder) + 2,
				firstBorder.getZ() + 0.5);
		tpLoc.setDirection(player.getLocation().getDirection());
		
		player.teleport(tpLoc);
		renderHandler.getPlayerRender(playerId).show();
	}
}
