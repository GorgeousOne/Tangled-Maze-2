package me.gorgeousone.tangledmaze.cmdframework.argument;

import org.bukkit.ChatColor;

public class ArgValue {
	
	private static final String exceptionText = ChatColor.RED + "'%value%' is not a %type%.";
	
	private int intVal;
	private double decimalVal;
	private String stringVal;
	private boolean booleanVal;
	
	public ArgValue(String stringValue) {
		this(stringValue, ArgType.STRING);
	}
	
	public ArgValue(String value, ArgType type) {
		setValue(value, type);
	}
	
	public String get() {
		return stringVal;
	}
	
	public int getInt() {
		return intVal;
	}
	
	public double getDouble() {
		return decimalVal;
	}
	
	public boolean getBoolean() {
		return booleanVal;
	}
	
	protected void setValue(String value, ArgType type) {
		try {
			switch (type) {
				case INTEGER:
					intVal = Integer.parseInt(value);
				case DECIMAL:
					decimalVal = Double.parseDouble(value);
				case STRING:
					stringVal = value;
					break;
				case BOOLEAN:
					booleanVal = Boolean.parseBoolean(value);
					break;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(exceptionText.replace("%value%", value).replace("%type%", type.simpleName()));
		}
	}
	
	@Override
	public String toString() {
		return "ArgValue{" +
		       "stringVal='" + stringVal + '\'' +
		       '}';
	}
}
