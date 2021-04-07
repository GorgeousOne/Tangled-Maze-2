package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.cmdframework.command.ParentCommand;
import me.gorgeousone.tangledmaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.tangledmaze.command.AddClip;
import me.gorgeousone.tangledmaze.command.BuildMaze;
import me.gorgeousone.tangledmaze.command.CutClip;
import me.gorgeousone.tangledmaze.command.SettingsCommand;
import me.gorgeousone.tangledmaze.command.StartMaze;
import me.gorgeousone.tangledmaze.command.SwitchTool;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.listener.ClickListener;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TangledMaze extends JavaPlugin {
	
	private SessionHandler sessionHandler;
	private ToolHandler toolHandler;
	private RenderHandler renderHandler;
	private BuildHandler buildHandler;
	
	@Override
	public void onEnable() {
		sessionHandler = new SessionHandler();
		toolHandler = new ToolHandler(sessionHandler);
		renderHandler = new RenderHandler(this, sessionHandler);
		buildHandler = new BuildHandler();
		registerListeners();
		registerCommands();
	}
	
	@Override
	public void onDisable() {
		renderHandler.disable();
		sessionHandler.disable();
		Bukkit.broadcastMessage(ChatColor.GOLD + "TODO mazemap setType contains check");
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(renderHandler, this);
		manager.registerEvents(new ClickListener(sessionHandler, toolHandler), this);
	}
	
	private void registerCommands() {
		ParentCommand mazeCmd = new ParentCommand("tangledmaze");
		mazeCmd.addAlias("maze");
		mazeCmd.addAlias("tm");
		
		mazeCmd.addChild(new StartMaze(sessionHandler));
		mazeCmd.addChild(new AddClip(sessionHandler));
		mazeCmd.addChild(new CutClip(sessionHandler));
		mazeCmd.addChild(new SwitchTool(toolHandler));
		mazeCmd.addChild(new BuildMaze(sessionHandler, buildHandler));
		mazeCmd.addChild(new SettingsCommand(sessionHandler));
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(mazeCmd);
	}
}
