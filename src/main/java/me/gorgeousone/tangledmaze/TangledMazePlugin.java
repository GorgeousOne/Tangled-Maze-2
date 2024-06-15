package me.gorgeousone.tangledmaze;

import fr.black_eyes.lootchest.Main;
import me.gorgeousone.tangledmaze.cmdframework.command.ParentCommand;
import me.gorgeousone.tangledmaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.tangledmaze.command.AddClipCommand;
import me.gorgeousone.tangledmaze.command.BuildMazeCommand;
import me.gorgeousone.tangledmaze.command.CutClipCommand;
import me.gorgeousone.tangledmaze.command.GetWandCommand;
import me.gorgeousone.tangledmaze.command.HelpCommand;
import me.gorgeousone.tangledmaze.loot.LootRemoveCommand;
import me.gorgeousone.tangledmaze.loot.LootRespawnCommand;
import me.gorgeousone.tangledmaze.loot.LootSpawnCommand;
import me.gorgeousone.tangledmaze.command.ReloadCommand;
import me.gorgeousone.tangledmaze.command.SettingsCommand;
import me.gorgeousone.tangledmaze.command.SolveMazeCommand;
import me.gorgeousone.tangledmaze.command.StartMazeCommand;
import me.gorgeousone.tangledmaze.command.TeleportCommand;
import me.gorgeousone.tangledmaze.command.ToolCommand;
import me.gorgeousone.tangledmaze.command.UnbuildMazeCommand;
import me.gorgeousone.tangledmaze.command.UndoCommand;
import me.gorgeousone.tangledmaze.data.ConfigSettings;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Message;
import me.gorgeousone.tangledmaze.generation.building.BuildHandler;
import me.gorgeousone.tangledmaze.listener.BlockChangeListener;
import me.gorgeousone.tangledmaze.listener.ChangeWorldListener;
import me.gorgeousone.tangledmaze.listener.ClickListener;
import me.gorgeousone.tangledmaze.listener.PlayerQuitListener;
import me.gorgeousone.tangledmaze.listener.UiListener;
import me.gorgeousone.tangledmaze.loot.LootHandler;
import me.gorgeousone.tangledmaze.loot.UnbuildListener;
import me.gorgeousone.tangledmaze.menu.UiHandler;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import me.gorgeousone.tangledmaze.tool.ToolHandler;
import me.gorgeousone.tangledmaze.updatecheck.UpdateCheck;
import me.gorgeousone.tangledmaze.util.ConfigUtil;
import me.gorgeousone.tangledmaze.util.MaterialUtil;
import me.gorgeousone.tangledmaze.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class TangledMazePlugin extends JavaPlugin {

	private static final int resourceId = 59284;
	private static final String resourceName = "tangled-maze-maze-generator";
	private static final String updateInfoUrl = "https://pastebin.com/raw/BRJfXpPu";

	private static TangledMazePlugin instance;
	private SessionHandler sessionHandler;
	private ToolHandler toolHandler;
	private RenderHandler renderHandler;
	private BuildHandler buildHandler;
	private UiHandler uiHandler;
	private LootHandler lootHandler;
	private ConfigSettings settings;
	private ParentCommand mazeCmd;

	@Override
	public void onEnable() {
		instance = this;
		configureAPI(getDescription().getVersion());

		sessionHandler = new SessionHandler();
		toolHandler = new ToolHandler(sessionHandler);
		renderHandler = new RenderHandler(this, sessionHandler);
		buildHandler = new BuildHandler(this, sessionHandler);
		uiHandler = new UiHandler(toolHandler);

		hookLootChestPlugin();
		registerListeners();
		registerCommands();

		settings = new ConfigSettings(this);
		reload();
	}

	@Override
	public void onDisable() {
		renderHandler.disable();
		sessionHandler.disable();
		toolHandler.disable();
	}

	public void reload() {
		loadConfigSettings();
		loadLanguage();
		checkForUpdates();
	}

	/**
	 * Configures settings needed if the plugin is shaded into a plugin
	 */
	public static void configureAPI(String tangledMazeVersion) {
		VersionUtil.setup(tangledMazeVersion);
	}

	/**
	 * For API use only
	 */
	public static TangledMazePlugin getInstance() {
		return instance;
	}

	public SessionHandler getSessionHandler() {
		return sessionHandler;
	}

	public ParentCommand getMazeCommand() {
		return mazeCmd;
	}

	private void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(renderHandler, this);
		manager.registerEvents(new ClickListener(this, sessionHandler, toolHandler, renderHandler, uiHandler), this);
		manager.registerEvents(new PlayerQuitListener(sessionHandler, renderHandler, toolHandler, uiHandler), this);
		manager.registerEvents(new BlockChangeListener(this, sessionHandler), this);
		manager.registerEvents(new ChangeWorldListener(toolHandler, renderHandler), this);
		manager.registerEvents(new UiListener(uiHandler), this);
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
		mazeCmd.addChild(new TeleportCommand(sessionHandler, renderHandler));
		mazeCmd.addChild(new SolveMazeCommand(sessionHandler, renderHandler));

		boolean isLootAvailable = lootHandler != null;
		ParentCommand lootCmd = new ParentCommand("loot");
		lootCmd.addChild(new LootSpawnCommand(sessionHandler, lootHandler, isLootAvailable));
		lootCmd.addChild(new LootRespawnCommand(sessionHandler, lootHandler, isLootAvailable));
		lootCmd.addChild(new LootRemoveCommand(sessionHandler, lootHandler, isLootAvailable));
		mazeCmd.addChild(lootCmd);

		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(mazeCmd);
	}

	private void loadConfigSettings() {
		reloadConfig();
		settings.addVersionDefaults(getConfig(), VersionUtil.IS_LEGACY_SERVER);
		getConfig().options().copyDefaults(true);
		saveConfig();
		settings.loadSettings(getConfig());

		String versionString = VersionUtil.IS_LEGACY_SERVER ? "legacy" : "aquatic";
		Constants.loadMaterials(ConfigUtil.loadConfig("materials-" + versionString, this));
		MaterialUtil.load();
	}

	private void loadLanguage() {
		Message.loadLanguage(ConfigUtil.loadConfig("language", this));
		uiHandler.reloadLanguage();
	}

	private void checkForUpdates() {
		new UpdateCheck(this, resourceId, resourceName, updateInfoUrl).run();
	}

	private void hookLootChestPlugin() {
		Logger logger = getLogger();
		PluginManager manager = Bukkit.getPluginManager();

		try {
			Main lootChestPlugin = Main.getInstance();
			lootHandler = new LootHandler(sessionHandler, lootChestPlugin, getLogger());
			manager.registerEvents(new UnbuildListener(lootHandler), this);
			logger.info("    Successfully detected LootChest plugin for spawning loot chests.");
		} catch(NoClassDefFoundError e) {
			logger.info("    LootChest plugin not detected. Loot chests can't be used.");
			logger.info("    Download available at: https://www.spigotmc.org/resources/lootchest.61564/");
		} catch (RuntimeException e) {
			lootHandler = null;
			logger.warning("    Compatibility issues with LootChest. Loot chests can't be used.");
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
}
