package me.gorgeousone.tangledmaze.tool;

public enum ToolType {
	CLIP("clip tool"),
	BRUSH("brush tool");
	
	private final String name;
	
	ToolType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static String[] getNames() {
		String[] names = new String[values().length];
		for (ToolType value : values()) {
			names[value.ordinal()] = value.toString().toLowerCase();
		}
		return names;
	}
}
