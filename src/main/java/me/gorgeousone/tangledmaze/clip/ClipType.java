package me.gorgeousone.tangledmaze.clip;

public enum ClipType {
	RECTANGLE(2, "rectangle", "rect", "rectangle"),
	ELLIPSE(2, "circle", "circ", "ellipse"),
	TRIANGLE(3, "triangle", "tri");
	
	private final int vertexCount;
	private final String name;
	private final String[] aliases;
	
	ClipType(int vertexCount, String name, String... aliases) {
		this.vertexCount = vertexCount;
		this.name = name;
		this.aliases = aliases;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getAliases() {
		return aliases.clone();
	}
	
	public static ClipType match(String argument) {
		String name = argument.toLowerCase();
		
		for (ClipType type : ClipType.values()) {
			for (String alias : type.aliases) {
				if (alias.equals(name)) {
					return type;
				}
			}
		}
		return null;
	}
}
