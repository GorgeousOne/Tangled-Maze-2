package me.gorgeousone.tangledmaze.tool;

public enum ToolType {
	CLIP_TOOL(""),
	BRUSH("brush tool");
	
	private final String name;
	
	ToolType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
