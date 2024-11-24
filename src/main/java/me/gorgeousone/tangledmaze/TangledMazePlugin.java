package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.badapple.BadApple;
import me.gorgeousone.tangledmaze.badapple.FrameCommand;
import me.gorgeousone.tangledmaze.cmdframework.command.ParentCommand;
import me.gorgeousone.tangledmaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class TangledMazePlugin extends JavaPlugin {
	
	private BuildHandler buildHandler;
	private ParentCommand mazeCmd;
	
	@Override
	public void onEnable() {
		buildHandler = new BuildHandler(this);
		registerCommands();
		BadApple.setBuildSettings();
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