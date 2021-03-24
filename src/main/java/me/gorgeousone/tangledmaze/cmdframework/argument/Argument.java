package me.gorgeousone.tangledmaze.cmdframework.argument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class to convert string inputs in commands into ArgValues according to given ArgType and to throw exceptions
 * if wrong inputs were given
 */
public class Argument {
	
	private final String name;
	private final ArgType type;
	private final List<String> tabList;
	private ArgValue defaultValue;
	
	public Argument(String name, ArgType type) {
		this(name, type, new String[]{});
	}
	
	public Argument(String name, ArgType type, String... tabList) {
		this.name = name;
		this.type = type;
		this.tabList = new ArrayList<>();
		this.tabList.addAll(Arrays.asList(tabList));
	}
	
	public ArgValue createValue(String input) {
		if (input != null) {
			return new ArgValue(input);
		}else if (defaultValue != null) {
			return defaultValue;
		}else {
			throw new IllegalArgumentException("");
		}
	}
	
	public ArgValue getDefault() {
		return defaultValue;
	}
	
	public String getName() {
		return name;
	}
	
	public ArgType getType() {
		return type;
	}
	
	public List<String> getTabList() {
		return tabList;
	}
	
	public Argument setDefault(String value) {
		defaultValue = new ArgValue(getType(), value);
		return this;
	}
}