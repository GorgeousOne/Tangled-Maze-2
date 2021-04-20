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
	
	public static void main(String[] args) {
		for (int dx = -1; dx <= 1; ++dx) {
			for (int dy = -1; dy <= 1; ++dy) {
				
				if (dx == 0 ^ dy == 0) {
					System.out.println(dx + ", " + dy);
				}
			}
		}
	}
}