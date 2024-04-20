package me.gorgeousone.tangledmaze.util;

public class MathUtil {
	
	public static int clamp(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}
	
	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static double floorMod(double d1, double d2) {
		return d1 - d2 * Math.floor(d1 / d2);
	}

}
