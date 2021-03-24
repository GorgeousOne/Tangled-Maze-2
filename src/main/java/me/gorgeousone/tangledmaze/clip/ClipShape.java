package me.gorgeousone.tangledmaze.clip;

public enum ClipShape {
	RECTANGLE(2), ELLIPSE(2), TRIANGLE(3);
	
	final int vertexCount;
	
	ClipShape(int vertexCount) {
		this.vertexCount = vertexCount;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
}
