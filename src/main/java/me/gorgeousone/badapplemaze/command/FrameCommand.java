package me.gorgeousone.badapplemaze.command;

import me.gorgeousone.badapplemaze.BadApplePlugin;
import me.gorgeousone.badapplemaze.cmdframework.argument.ArgType;
import me.gorgeousone.badapplemaze.cmdframework.argument.ArgValue;
import me.gorgeousone.badapplemaze.cmdframework.argument.Argument;
import me.gorgeousone.badapplemaze.cmdframework.command.ArgCommand;
import me.gorgeousone.badapplemaze.generation.AreaType;
import me.gorgeousone.badapplemaze.generation.MazeMap;
import me.gorgeousone.badapplemaze.generation.MazeMapFactory;
import me.gorgeousone.badapplemaze.generation.building.BuildHandler;
import me.gorgeousone.badapplemaze.maze.MazeSettings;
import me.gorgeousone.badapplemaze.util.BlockUtil;
import me.gorgeousone.badapplemaze.util.Direction;
import me.gorgeousone.badapplemaze.util.FrameLoader;
import me.gorgeousone.badapplemaze.util.Vec2;
import me.gorgeousone.badapplemaze.util.text.TextException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FrameCommand extends ArgCommand {

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
		Vec2 max = new Vec2(FrameLoader.WIDTH + 5, FrameLoader.HEIGHT + 5);

		MazeMap map = new MazeMap(world, min, max, BadApplePlugin.FLOOR_Y);
		MazeSettings settings = BadApplePlugin.SETTINGS;

		for (int z = 0; z < max.getZ(); ++z) {
			for (int x = 0; x < max.getX(); ++x) {
				map.setType(x, z, AreaType.FREE);
			}
		}
		//inset frame 2blocks to make maze go around whole frame
		//cut off 1px to get odd size
		for (int z = 0; z < FrameLoader.HEIGHT - 1; ++z) {
			for (int x = 0; x < FrameLoader.WIDTH - 1; ++x) {
				map.setType(x + 3, z + 3, getType(newFrame, x, z));
			}
		}
		int midZ = max.getZ() / 2 - 1;
		MazeMapFactory.createPaths(
				map,
				Arrays.asList(new Vec2(0, midZ)),
				settings,
				BlockUtil.getWorldMinHeight(world));
		//theres some buggy behavior for mazes with >1 exits in narrow passages
		//placing 2n exit manually :(
		map.setType(max.getX() - 1, midZ, AreaType.PATH);
		map.flip();
		setAsync(sender);

		sender.sendMessage("build");
		try {
			buildHandler.buildMaze(map, settings, () -> {
				finishAsync(sender);
			});
		} catch (TextException e) {
			finishAsync(sender);
			throw new RuntimeException(e);
		}
	}

	private AreaType getType(byte[] frame, int x, int z) {
		int i = z * FrameLoader.WIDTH + x;
		int val = frame[i] & 0xFF;

		if (val >= 64) {
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

			if (val1 >= 64) {
				return AreaType.WALL;
			}
		}
		return AreaType.FREE;
	}
}
