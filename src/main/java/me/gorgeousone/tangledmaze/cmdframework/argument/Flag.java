package me.gorgeousone.tangledmaze.cmdframework.argument;

public class Flag {
	
	private final String name;
	private final String shortName;
	
	public Flag(String name, String shortName) {
		this.name = name.toLowerCase();
		this.shortName = shortName.toLowerCase();
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public boolean matches(String input) {
		return input.equals(name) || input.equals(shortName);
	}
}
