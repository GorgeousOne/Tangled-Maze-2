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
			COMMAND_UNBUILD,
			INFO_PLUGIN_RELOAD,
			INFO_MAZE_WAND_USAGE,
			INFO_TOOL_SWITCH,
			INFO_SETTING_CHANGE,
			INFO_MAZE_NOT_BUILT,
			INFO_MAZE_INACCESSIBLE,
			INFO_MAZE_BUILD,
			INFO_MAZE_UNBUILD,
			ERROR_MISSING_PERMISSION,
			ERROR_CLIPBOARD_MISSING,
			ERROR_MAZE_MISSING,
			ERROR_EXIT_MISSING,
			ERROR_INVALID_TOOL,
			ERROR_INVALID_SETTING,
			ERROR_INVALID_BLOCK_NAME,
			ERROR_INVALID_BLOCK_PROPERTY;
	
	public static Text[] helpPages() {
		return new Text[]{
				COMMAND_WAND,
				COMMAND_START,
				COMMAND_ADD_CUT,
				COMMAND_TOOL,
				COMMAND_SETTING,
				COMMAND_UNDO,
				COMMAND_BUILD,
				COMMAND_UNBUILD
		};
	}
	
	public static void loadLanguage(FileConfiguration langConfig) {
		ConfigurationSection helps = langConfig.getConfigurationSection("help-pages");
		COMMAND_WAND = createHelpText("/maze wand", helps, "wand-command");
		COMMAND_START = createHelpText("/maze start", helps, "start-command");
		COMMAND_ADD_CUT = createHelpText("/maze add/cut", helps, "add-cut-command");
		COMMAND_TOOL = createHelpText("/maze tool <tool>", helps, "tool-command");
		COMMAND_TOOL.add(createHelpText(ChatColor.YELLOW + "wallheight", helps, "settings.wall-height"));
		COMMAND_TOOL.add(createHelpText(ChatColor.YELLOW + "pathwidth", helps, "settings.path-width"));
		COMMAND_TOOL.add(createHelpText(ChatColor.YELLOW + "wallwidth", helps, "settings.wall-width"));
		COMMAND_TOOL.add(createHelpText(ChatColor.YELLOW + "roodwidth", helps, "settings.roof-width"));
		COMMAND_TOOL.add(createHelpText(ChatColor.YELLOW + "curliness", helps, "settings.curliness"));
		
		COMMAND_SETTING = createHelpText("/maze setting <setting> <integer>", helps, "settings-command");
		COMMAND_UNDO = createHelpText("/maze undo", helps, "undo-command");
		COMMAND_BUILD = createHelpText("/maze build <blocks>...", helps, "build-command");
		COMMAND_UNBUILD = createHelpText("/maze unbuild", helps, "-command");
		
		ConfigurationSection infos = langConfig.getConfigurationSection("infos");
		INFO_PLUGIN_RELOAD = createInfo("plugin-reload", infos);
		INFO_MAZE_WAND_USAGE = createInfo("maze-wand-usage", infos);
		INFO_TOOL_SWITCH = createInfo("tool-switch", infos);
		INFO_SETTING_CHANGE = createInfo("setting-change", infos);
		INFO_MAZE_NOT_BUILT = createInfo("maze-not-built", infos);
		INFO_MAZE_INACCESSIBLE = createInfo("maze-inaccessible", infos);
		INFO_MAZE_UNBUILD = createInfo("maze-unbuild", infos);
		INFO_MAZE_BUILD = createInfo("maze-build", infos);
		
		ConfigurationSection errors = langConfig.getConfigurationSection("errors");
		ERROR_MISSING_PERMISSION = createError("missing-permission", errors);
		ERROR_CLIPBOARD_MISSING = createError("clipboard-missing", errors);
		ERROR_MAZE_MISSING = createError("maze-missing", errors);
		ERROR_EXIT_MISSING = createError("no-exit-missing", errors);
		ERROR_INVALID_TOOL = createError("invalid-tool", errors);
		ERROR_INVALID_SETTING = createError("invalid-setting", errors);
		ERROR_INVALID_BLOCK_NAME = createError("invalid-block-name", errors);
		ERROR_INVALID_BLOCK_PROPERTY = createError("invalid-block-property", errors);
	}
	
	public static Text createHelpText(String header, ConfigurationSection section, String path) {
		return new Text(ChatColor.DARK_GREEN + header + "\\n" + ChatColor.GREEN + section.get(path));
	}
	
	public static Text createInfo(String path, ConfigurationSection section) {
		return new Text(Constants.prefix + section.getString(path));
	}
	
	public static Text createError(String path, ConfigurationSection section) {
		return new Text(ChatColor.RED + section.getString(path));
	}
}
