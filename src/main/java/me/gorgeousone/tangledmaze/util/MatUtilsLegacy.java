package me.gorgeousone.tangledmaze.util;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public final class MatUtilsLegacy {
	
	@SuppressWarnings("Deprecation")
	public static MaterialData deserializeMat(String matDataString) {
		String dataString, typeString;
		byte data;
		
		if (matDataString.contains(":")) {
			String[] split = matDataString.split(":");
			typeString = split[0];
			dataString = split[1];
		} else {
			typeString = matDataString;
			dataString = "0";
		}
		Material type = Material.matchMaterial(typeString);
		
		if (type == null || !type.isBlock()) {
			throw new IllegalArgumentException("invalid block");
		}
		try {
			data = Byte.parseByte(dataString);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("invalid number");
		}
		return new MaterialData(type, data);
	}
}
