package me.gorgeousone.tangledmaze.badapple;

import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;

public class ClearCommand extends BaseCommand {
	
	World w;
	BlockData AIR = Bukkit.createBlockData(Material.AIR);
	
	public ClearCommand() {
		super("clear");
		w = Bukkit.getWorld("world");
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		for (int x = 0; x < FrameLoader.WIDTH + FrameCommand.PADDING; ++x) {
			for (int z = 0; z < FrameLoader.HEIGHT + FrameCommand.PADDING; ++z) {
				for (int y = BadApple.FLOOR_Y+1; y < BadApple.FLOOR_Y + 3; ++y) {
					BlockState state = w.getBlockState(x, y, z);
					state.setBlockData(AIR);
					state.update(true, false);
				}
			}
		}
		BadApple.LAST_BLOCKS.clear();
	}
}
