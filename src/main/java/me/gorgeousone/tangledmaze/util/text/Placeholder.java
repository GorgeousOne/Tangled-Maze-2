package me.gorgeousone.tangledmaze.util.text;

public class Placeholder {
	
	public final static char markerChar = '%';
	private final String key;
	private final Object value;
	
	public Placeholder(String key, Object value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value.toString();
	}
	
	public String apply(String message) {
		return message.replace(markerChar + getKey() + markerChar, getValue());
	}
}
