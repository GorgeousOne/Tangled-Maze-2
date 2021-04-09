package me.gorgeousone.tangledmaze.util;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.Set;

public final class MaterialUtil {
	
	private static final Set<String> BLOCK_NAMES = new HashSet<>();
	
	static {
		for (Material mat : Material.values()) {
			if (mat.isBlock()) {
				BLOCK_NAMES.add(mat.name().toLowerCase());
			}
		}
	}
	
	private static boolean isLegacyServer;
	
	private MaterialUtil() {}
	
	public static Set<String> getBlockNames() {
		return BLOCK_NAMES;
	}
	
	public static BlockData read(String argument) {
	
		String[] stringParts = argument.split(":");
		String typeString = stringParts[0];
		Material type = Material.matchMaterial(typeString);
		
		if (type == null || !type.isBlock()) {
			throw new IllegalArgumentException("invalid block");
		}
		BlockData blockData = type.createBlockData();
		
		
		if (stringParts.length <= 1) {
			return blockData;
		}
		for (int i = 1; i < stringParts.length; i++) {
			String blockProperty = stringParts[i];
			
//			try {
			blockData = blockData.merge(type.createBlockData("[" + blockProperty + "]"));
//			} catch (IllegalArgumentException ex) {
//				createPlayerMessageFromException(ex.getCause().getLocalizedMessage(), stringMat, blockProperty.split("="));
//			}
		}
		return blockData;
	}
	
	
//	private static void createPlayerMessageFromException(String exceptionMessage, String material,
//	                                                     String[] blockProperty) throws TextException {
//		if (exceptionMessage.contains("does not have property")) {
//			throw new TextException(
//					Messages.ERROR_INVALID_BLOCK_PROPERTY,
//					new PlaceHolder("block", material),
//					new PlaceHolder("property", blockProperty[0]));
//		} else if (exceptionMessage.contains("Expected value for property")) {
//			throw new TextException(
//					Messages.ERROR_MISSING_BLOCK_PROPERTY_VALUE,
//					new PlaceHolder("property", blockProperty[0]));
//		} else if (exceptionMessage.contains("does not accept")) {
//			throw new TextException(
//					Messages.ERROR_INVALID_BLOCK_PROPERTY_VALUE,
//					new PlaceHolder("block", material),
//					new PlaceHolder("property", blockProperty[0]),
//					new PlaceHolder("value", blockProperty.length > 1 ? blockProperty[1] : ""));
//		}
//	}
}
