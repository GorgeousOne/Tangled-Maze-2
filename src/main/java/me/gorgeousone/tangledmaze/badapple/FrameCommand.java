package me.gorgeousone.tangledmaze.badapple;

import me.gorgeousone.tangledmaze.cmdframework.argument.ArgType;
import me.gorgeousone.tangledmaze.cmdframework.argument.ArgValue;
import me.gorgeousone.tangledmaze.cmdframework.argument.Argument;
import me.gorgeousone.tangledmaze.cmdframework.command.ArgCommand;
import me.gorgeousone.tangledmaze.generation.AreaType;
import me.gorgeousone.tangledmaze.generation.MazeMap;
import me.gorgeousone.tangledmaze.generation.MazeMapFactory;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import me.gorgeousone.tangledmaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FrameCommand extends ArgCommand {
	
	public static int PADDING = 5;
	public static int INSET = 3;
	private final BuildHandler buildHandler;
	
	public FrameCommand(BuildHandler buildHandler) {
		super("frame");
		addArg(new Argument("index", ArgType.INTEGER));
		
		this.buildHandler = buildHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		int index = argValues.get(0).getInt();
		index = Math.max(1, Math.min(FrameLoader.FRAME_COUNT, index));
		byte[] newFrame = FrameLoader.loadFrame(index);
		updateMaze(newFrame, sender);
	}
	
	private void updateMaze(byte[] newFrame, CommandSender sender) {
		World world = Bukkit.getWorld("world");
		Vec2 min = new Vec2(0, 0);
		//first block after maze corner
		Vec2 max = new Vec2(FrameLoader.WIDTH + PADDING, FrameLoader.HEIGHT + PADDING);
		MazeMap map = new MazeMap(world, min, max, BadApple.FLOOR_Y);
		
		for (int z = 0; z < max.getZ(); ++z) {
			for (int x = 0; x < max.getX(); ++x) {
				map.setType(x, z, AreaType.FREE);
			}
		}
		//inset frame 2blocks to make maze go around whole frame
		//cut off 1px to get odd size
		for (int z = 0; z < FrameLoader.HEIGHT - 1; ++z) {
			for (int x = 0; x < FrameLoader.WIDTH - 1; ++x) {
				map.setType(x + INSET, z + INSET, getType(newFrame, x, z));
			}
		}
		int midZ = max.getZ() / 2 - 1;
		
		MazeMapFactory.createPaths(
				map,
				Arrays.asList(new Vec2(0, midZ), max.clone().sub(1, 0).setZ(midZ)),
				BadApple.SETTINGS,
				BlockUtil.getWorldMinHeight(world));
		map.flip();
		setAsync(sender);
		
		sender.sendMessage("build");
		try {
			buildHandler.buildMaze(map, BadApple.SETTINGS, () -> {
				finishAsync(sender);
			});
		} catch (TextException e) {
			finishAsync(sender);
			throw new RuntimeException(e);
		}
	}
	
	int CUTOFF = 64;
	
	private AreaType getType(byte[] frame, int x, int z) {
		int i = z * FrameLoader.WIDTH + x;
		int val = frame[i] & 0xFF;
		
		if (val >= CUTOFF) {
			return null;
		}
		for (Direction dir : Direction.values()) {
			int x1 = x + dir.getX();
			int z1 = z + dir.getZ();
			
			if (x1 < 0 || x1 >= FrameLoader.WIDTH ||
			    z1 < 0 || z1 >= FrameLoader.HEIGHT) {
				continue;
			}
			int i1 = z1 * FrameLoader.WIDTH + x1;
			int val1 = frame[i1] & 0xFF;
			
			if (val1 >= CUTOFF) {
				return AreaType.WALL;
			}
		}
		return AreaType.FREE;
	}
}