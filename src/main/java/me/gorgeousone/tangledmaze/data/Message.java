package me.gorgeousone.tangledmaze.data;

import me.gorgeousone.tangledmaze.util.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Message {
	
	public static Text
			COMMAND_WAND,
			COMMAND_START,
			COMMAND_ADD_CUT,
			COMMAND_TOOL,
			COMMAND_SETTING,
			COMMAND_UNDO,
			COMMAND_BUILD,
			COMMAND_UNBUILD;
	
	public static Text[] helpPages() {
		return new Text[] {
				COMMAND_WAND,
				COMMAND_START,
				COMMAND_ADD_CUT,
				COMMAND_TOOL,
				COMMAND_SETTING,
				COMMAND_UNDO,
				COMMAND_BUILD,
				COMMAND_UNBUILD};
	}
	
	public static void loadLanguage(FileConfiguration langConfig) {
		ConfigurationSection help = langConfig.getConfigurationSection("help-pages");
		COMMAND_WAND = createHelpText("/maze wand", help, "wand-command");
		COMMAND_START = createHelpText("/maze start", help, "start-command");
		COMMAND_ADD_CUT = createHelpText("/maze add/cut", help, "add-cut-command");
		COMMAND_TOOL = createHelpText("/maze tool <tool>", help, "tool-command");
		COMMAND_SETTING = createHelpText("/maze setting <setting> <integer>", help, "settings-command");
		COMMAND_UNDO = createHelpText("/maze undo", help, "undo-command");
		COMMAND_BUILD = createHelpText("/maze build <blocks>...", help, "build-command");
		COMMAND_UNBUILD = createHelpText("/maze unbuild", help, "-command");
		
		ConfigurationSection info = langConfig.getConfigurationSection("infos");
		
	}
	
	private static Text createHelpText(String header, ConfigurationSection help, String path) {
		return new Text(ChatColor.DARK_GREEN + header + "\\n" + ChatColor.GREEN + help.get(path));
	}
}
