package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.cmdframework.command.ParentCommand;
import me.gorgeousone.tangledmaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.tangledmaze.command.AddClipCommand;
import me.gorgeousone.tangledmaze.command.BuildMazeCommand;
import me.gorgeousone.tangledmaze.command.CutClipCommand;
import me.gorgeousone.tangledmaze.command.GetWandCommand;
import me.gorgeousone.tangledmaze.command.HelpCommand;
import me.gorgeousone.tangledmaze.command.ReloadCommand;
import me.gorgeousone.tangledmaze.command.SettingsCommand;
import me.gorgeousone.tangledmaze.command.StartMazeCommand;
import me.gorgeousone.tangledmaze.command.ToolCommand;
import me.gorgeousone.tangledmaze.command.UnbuildMazeCommand;
import me.gorgeousone.tangledmaze.command.UndoCommand;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.listener.BlockChangeListener;
import me.gorgeousone.tangledmaze.listener.ClickListener;
import me.gorgeousone.tangledmaze.listener.PlayerQuitListener;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.util.ConfigUtil;
import me.gorgeousone.tangledmaze.util.VersionUtil;
import me.gorgeousone.tangledmaze.util.blocktype.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TangledMazePlugin extends JavaPlugin {
	
	private SessionHandler sessionHandler;
	private ToolHandler toolHandler;
	private RenderHandler renderHandler;
	private BuildHandler buildHandler;
	private ConfigSettings settings;
	
	private ParentCommand mazeCmd;
	
	@Override
	public void onEnable() {
		BlockType.configureVersion(VersionUtil.IS_LEGACY_SERVER);
		sessionHandler = new SessionHandler();
		toolHandler = new ToolHandler(sessionHandler);
		renderHandler = new RenderHandler(this, sessionHandler);
		buildHandler = new BuildHandler();
		registerListeners();
		registerCommands();
		
		settings = new ConfigSettings(this);
		reload();
	}
	
	@Override
	public void onDisable() {
		renderHandler.disable();
		sessionHandler.disable();
		buildHandler.disable();
		toolHandler.disable();
		Bukkit.broadcastMessage(ChatColor.GOLD + "TODO mazemap setType contains check");
	}
	
	public void reload() {
		loadConfigSettings();
		loadLanguage();
	}
	
	public ParentCommand getMazeCommand() {
		return mazeCmd;
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(sessionHandler, this);
		manager.registerEvents(toolHandler, this);
		manager.registerEvents(renderHandler, this);
		manager.registerEvents(new ClickListener(this, sessionHandler, toolHandler, renderHandler), this);
		manager.registerEvents(new PlayerQuitListener(sessionHandler, renderHandler), this);
		manager.registerEvents(new BlockChangeListener(this, sessionHandler), this);
	}
	
	private void registerCommands() {
		mazeCmd = new ParentCommand("tangledmaze");
		mazeCmd.addAlias("maze");
		mazeCmd.addAlias("tm");
		mazeCmd.setPermission(Constants.BUILD_PERM);
		
		mazeCmd.addChild(new HelpCommand());
		mazeCmd.addChild(new GetWandCommand());
		mazeCmd.addChild(new ReloadCommand(this));
		mazeCmd.addChild(new StartMazeCommand(sessionHandler, toolHandler));
		mazeCmd.addChild(new ToolCommand(toolHandler));
		mazeCmd.addChild(new AddClipCommand(sessionHandler, toolHandler));
		mazeCmd.addChild(new CutClipCommand(sessionHandler, toolHandler));
		mazeCmd.addChild(new UndoCommand(sessionHandler));
		mazeCmd.addChild(new SettingsCommand(sessionHandler));
		mazeCmd.addChild(new BuildMazeCommand(sessionHandler, buildHandler, toolHandler));
		mazeCmd.addChild(new UnbuildMazeCommand(sessionHandler, buildHandler));
		
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(mazeCmd);
	}
	
	private void loadConfigSettings() {
		reloadConfig();
		settings.addVersionDefaults(getConfig(), VersionUtil.IS_LEGACY_SERVER);
		getConfig().options().copyDefaults(true);
		saveConfig();
		settings.loadSettings(getConfig());
	}
	
	private void loadLanguage() {
		Message.loadLanguage(ConfigUtil.loadConfig("language", this));
	}
}
