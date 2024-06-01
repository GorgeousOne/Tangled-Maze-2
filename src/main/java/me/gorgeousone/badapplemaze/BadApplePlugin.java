package me.gorgeousone.badapplemaze;

import me.gorgeousone.badapplemaze.cmdframework.command.ParentCommand;
import me.gorgeousone.badapplemaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.badapplemaze.command.FrameCommand;
import me.gorgeousone.badapplemaze.generation.building.BlockPalette;
import me.gorgeousone.badapplemaze.generation.building.BuildHandler;
import me.gorgeousone.badapplemaze.maze.MazePart;
import me.gorgeousone.badapplemaze.maze.MazeProperty;
import me.gorgeousone.badapplemaze.maze.MazeSettings;
import me.gorgeousone.badapplemaze.util.BlockVec;
import me.gorgeousone.badapplemaze.util.VersionUtil;
import me.gorgeousone.badapplemaze.util.blocktype.BlockType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class BadApplePlugin extends JavaPlugin {

	public static final int FLOOR_Y = -63;
	public static Set<BlockVec> LAST_BLOCKS;
	public static MazeSettings SETTINGS;

	private static BadApplePlugin instance;
	private BuildHandler buildHandler;
	private ParentCommand mazeCmd;

	@Override
	public void onEnable() {
		instance = this;
		configureAPI();
		buildHandler = new BuildHandler(this);
		registerCommands();
		setBuildSettings();
	}

	private void setBuildSettings() {
		BlockPalette black = new BlockPalette();
		black.addBlock(BlockType.get("black_concrete"), 1);

		MazeSettings settings = new MazeSettings();
		settings.setPalette(MazePart.WALLS, black);
		settings.setValue(MazeProperty.CURLINESS, 1);
		BadApplePlugin.SETTINGS = settings;
	}

	/**
	 * Configures settings needed if the plugin jar is used as API
	 */
	public static void configureAPI() {
		BlockType.configureVersion(VersionUtil.IS_LEGACY_SERVER);
	}

	/**
	 * For API use only
	 */
	public static BadApplePlugin getInstance() {
		return instance;
	}


	public ParentCommand getMazeCommand() {
		return mazeCmd;
	}

	private void registerCommands() {
		mazeCmd = new ParentCommand("tangledmaze");
		mazeCmd.addAlias("maze");
		mazeCmd.addAlias("tm");

		mazeCmd.addChild(new FrameCommand(buildHandler));

		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(mazeCmd);
	}
}
