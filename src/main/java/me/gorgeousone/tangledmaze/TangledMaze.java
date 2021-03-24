package me.gorgeousone.tangledmaze;

import me.gorgeousone.tangledmaze.clip.ClickListener;
import me.gorgeousone.tangledmaze.clip.ClipHandler;
import me.gorgeousone.tangledmaze.cmdframework.command.ParentCommand;
import me.gorgeousone.tangledmaze.cmdframework.handler.CommandHandler;
import me.gorgeousone.tangledmaze.command.AddClip;
import me.gorgeousone.tangledmaze.command.CutClip;
import me.gorgeousone.tangledmaze.command.StartMaze;
import me.gorgeousone.tangledmaze.render.RenderHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TangledMaze extends JavaPlugin {
	
	private ClipHandler clipHandler;
	private RenderHandler renderHandler;
	
	@Override
	public void onEnable() {
		clipHandler = new ClipHandler();
		renderHandler = new RenderHandler(this, clipHandler);
		registerListeners();
		registerCommands();
	}
	
	@Override
	public void onDisable() {
		renderHandler.disable();
		clipHandler.disable();
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(renderHandler, this);
		manager.registerEvents(new ClickListener(clipHandler), this);
	}
	
	private void registerCommands() {
		ParentCommand rootCommand = new ParentCommand("tangledmaze");
		rootCommand.addAlias("maze");
		rootCommand.addAlias("tm");
		
		rootCommand.addChild(new StartMaze(clipHandler));
		rootCommand.addChild(new AddClip(clipHandler));
		rootCommand.addChild(new CutClip(clipHandler));
		
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(rootCommand);
	}
}
