package me.gorgeousone.tangledmaze.maze;

public class MazeSettings {
	
	private int wallHeight;
	private int wallWidth;
	private int pathWidth;
	private int roofWidth;
	private int curliness;
	
	public MazeSettings() {
		wallHeight = 2;
		wallWidth = 1;
		pathWidth = 1;
		roofWidth = 1;
		curliness = 3;
	}
	
	public int getWallHeight() {
		return wallHeight;
	}
	
	public void setWallHeight(int wallHeight) {
		this.wallHeight = wallHeight;
	}
	
	public int getWallWidth() {
		return wallWidth;
	}
	
	public void setWallWidth(int wallWidth) {
		this.wallWidth = wallWidth;
	}
	
	public int getPathWidth() {
		return pathWidth;
	}
	
	public void setPathWidth(int pathWidth) {
		this.pathWidth = pathWidth;
	}
	
	public int getRoofWidth() {
		return roofWidth;
	}
	
	public void setRoofWidth(int roofWidth) {
		this.roofWidth = roofWidth;
	}
	
	public int getCurliness() {
		return curliness;
	}
	
	public void setCurliness(int curliness) {
		this.curliness = curliness;
	}
}
