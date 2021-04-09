package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.clip.ClipShape;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.UUID;

public class ClipTool {
	
	private final UUID playerId;
	private final ArrayList<Block> vertices;
	private final ClipShape shape;
	
	public ClipTool(UUID playerId, ClipShape shape) {
		this.playerId = playerId;
		vertices = new ArrayList<>();
		this.shape = shape;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public ArrayList<Block> getVertices() {
		return new ArrayList<>(vertices);
	}
	
	public void setVertices(ArrayList<Block> newVertices) {
		vertices.clear();
		vertices.addAll(newVertices);
	}
	
	public ClipShape getShape() {
		return shape;
	}
	
	public void addVertex(Block vertex) {
		vertex = BlockUtil.getSurface(vertex);
		int vertexCount = vertices.size();
		int requiredVertexCount = shape.getVertexCount();
		
		if (vertexCount >= requiredVertexCount) {
			vertices.clear();
			vertices.add(vertex);
			Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.RESTART));
		} else if (vertexCount == requiredVertexCount - 1) {
			vertices.add(vertex);
			setVertices(ClipFactory.createVertices(getVertices(), shape));
			Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.COMPLETE));
		} else {
			vertices.add(vertex);
			Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.PROGRESS));
		}
	}
	
	public boolean isComplete() {
		return vertices.size() >= shape.getVertexCount();
	}
}
