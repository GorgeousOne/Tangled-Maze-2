package me.gorgeousone.tangledmaze.clip;

public enum ClipType {
	RECTANGLE(2), ELLIPSE(2), TRIANGLE(3);
	
	final int vertexCount;
	
	ClipType(int vertexCount) {
		this.vertexCount = vertexCount;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
}
