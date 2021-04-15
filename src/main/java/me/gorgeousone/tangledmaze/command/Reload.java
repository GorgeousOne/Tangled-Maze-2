package me.gorgeousone.tangledmaze.command;

import me.gorgeousone.tangledmaze.TangledMazePlugin;
import me.gorgeousone.tangledmaze.cmdframework.command.BaseCommand;
import org.bukkit.command.CommandSender;

public class Reload extends BaseCommand {
	
	private final TangledMazePlugin tangledMazePlugin;
	
	public Reload(TangledMazePlugin tangledMazePlugin) {
		super("reload");
		addAlias("rl");
		
		this.tangledMazePlugin = tangledMazePlugin;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		tangledMazePlugin.reload();
		sender.sendMessage("reloaded tangled maze");
	}
}
