package me.gorgeousone.tangledmaze.util;

import java.util.Objects;

public class Version {
	
	int major;
	int minor;
	int patch;
	
	public Version(String versionString) {
		this(versionString, "\\.");
	}
	
	public Version(String versionString, String delimiter) {
		String[] split = versionString.split(delimiter);
		major = Integer.parseInt(split[0]);
		if (split.length >= 1) {
			minor = Integer.parseInt(split[1]);
		} else {
			minor = 0;
		}
		if (split.length >= 2) {
			patch = Integer.parseInt(split[2]);
		} else {
			patch = 0;
		}
	}
	
	public boolean isBelow(Version other) {
		int[] intArray = new int[]{major, minor, patch};
		int[] otherIntArray = new int[]{other.major, other.minor, other.patch};
		
		for (int i = 0; i < intArray.length; i++) {
			int versionDiff = intArray[i] - otherIntArray[i];
			
			if (versionDiff > 0) {
				return false;
			} else if (versionDiff < 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Version)) {
			return false;
		}
		Version version = (Version) o;
		return major == version.major && minor == version.minor && patch == version.patch;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch);
	}
	
	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}
}
