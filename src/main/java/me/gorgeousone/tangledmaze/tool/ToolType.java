package me.gorgeousone.tangledmaze.tool;

public enum ToolType {
	EXIT_SETTER("exit setter", "exit", "entrance"),
	BRUSH("brush", "brush", "eraser");
	
	private final String name;
	private final String[] aliases;
	
	ToolType(String name, String... aliases) {
		this.name = name;
		this.aliases = aliases;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getAliases() {
		return aliases.clone();
	}
	
	public static ToolType match(String argument) {
		String name = argument.toLowerCase();
		
		for (ToolType type : ToolType.values()) {
			for (String alias : type.aliases) {
				if (alias.equals(name)) {
					return type;
				}
			}
		}
		return null;
	}
}
