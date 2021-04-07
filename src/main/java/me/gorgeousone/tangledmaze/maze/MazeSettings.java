package me.gorgeousone.tangledmaze.maze;

import java.util.Arrays;
import java.util.HashMap;

public class MazeSettings {
	
	private final HashMap<MazeProperty, Integer> properties;
	
	public MazeSettings() {
		this.properties = new HashMap<>();
		Arrays.stream(MazeProperty.values()).forEach(property -> properties.put(property, property.getDefault()));
	}
	
	public int getValue(MazeProperty property) {
		return properties.get(property);
	}
	
	public int setValue(MazeProperty property, int newValue) {
		newValue = Math.max(1, Math.min(property.getMax(), newValue));
		properties.put(property, newValue);
		return newValue;
	}
}
