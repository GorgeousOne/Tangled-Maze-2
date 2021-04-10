package me.gorgeousone.tangledmaze.cmdframework.argument;

public class Flag {
	
	private final String name;
	
	public Flag(String name, String shortName) {
		this.name = name.toLowerCase();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean matches(String input) {
		return input.equals(name);
	}
}
