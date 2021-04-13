package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.cmdframework.command.ParentCommand;
import me.gorgeousone.tangledmaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.tangledmaze.command.AddClip;
import me.gorgeousone.tangledmaze.command.BuildMaze;
import me.gorgeousone.tangledmaze.command.CutClip;
import me.gorgeousone.tangledmaze.command.SettingsCommand;
import me.gorgeousone.tangledmaze.command.StartMaze;
import me.gorgeousone.tangledmaze.command.SwitchTool;
import me.gorgeousone.tangledmaze.command.UnbuildMaze;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.listener.BlockChangeListener;
import me.gorgeousone.tangledmaze.listener.ClickListener;
import me.gorgeousone.tangledmaze.listener.PlayerQuitListener;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.VersionUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
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
		BlockType.configureVersion(VersionUtil.IS_LEGACY_SERVER);
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
		buildHandler.disable();
		Bukkit.broadcastMessage(ChatColor.GOLD + "TODO mazemap setType contains check");
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(sessionHandler, this);
		manager.registerEvents(toolHandler, this);
		manager.registerEvents(renderHandler, this);
		manager.registerEvents(new ClickListener(sessionHandler, toolHandler), this);
		manager.registerEvents(new PlayerQuitListener(sessionHandler, renderHandler), this);
		manager.registerEvents(new BlockChangeListener(this, sessionHandler), this);
	}
	
	private void registerCommands() {
		ParentCommand mazeCmd = new ParentCommand("tangledmaze");
		mazeCmd.addAlias("maze");
		mazeCmd.addAlias("tm");
		
		mazeCmd.addChild(new StartMaze(sessionHandler));
		mazeCmd.addChild(new AddClip(sessionHandler));
		mazeCmd.addChild(new CutClip(sessionHandler));
		mazeCmd.addChild(new SwitchTool(toolHandler));
		mazeCmd.addChild(new SettingsCommand(sessionHandler));
		mazeCmd.addChild(new BuildMaze(sessionHandler, buildHandler));
		mazeCmd.addChild(new UnbuildMaze(sessionHandler, buildHandler));
		
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(mazeCmd);
	}
}
