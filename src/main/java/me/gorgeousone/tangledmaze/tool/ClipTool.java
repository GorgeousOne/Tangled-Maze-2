package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.clip.ClipShape;
import me.gorgeousone.tangledmaze.event.ClipToolChangeEvent;
import me.gorgeousone.tangledmaze.util.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClipTool {
	
	private final UUID playerId;
	private final ArrayList<Block> vertices;
	private ClipShape shape;
	private int vertexToRelocate = -1;
	
	public ClipTool(UUID playerId, ClipShape shape) {
		this.playerId = playerId;
		vertices = new ArrayList<>();
		this.shape = shape;
	}
	
	public void reset() {
		vertices.clear();
		vertexToRelocate = -1;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public ArrayList<Block> getVertices() {
		return new ArrayList<>(vertices);
	}
	
	public void setVertices(List<Block> newVertices) {
		vertices.clear();
		vertices.addAll(newVertices);
	}
	
	public int getVertexToRelocate() {
		return vertexToRelocate;
	}
	
	public ClipShape getShape() {
		return shape;
	}
	
	public void setShape(ClipShape shape) {
		this.shape = shape;
	}
	
	public void addVertex(Block clickedBlock) {
		Block newVertex = BlockUtil.getSurface(clickedBlock);
		int vertexCount = vertices.size();
		int requiredVertexCount = shape.getVertexCount();
		ClipToolChangeEvent.Cause changeType;
		
		//clip is already complete
		if (vertexCount >= requiredVertexCount) {
			if (vertexToRelocate != -1) {
				vertices.set(vertexToRelocate, newVertex);
				setVertices(ClipFactory.relocateVertices(getVertices(), shape, vertexToRelocate));
				Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.RESIZE_FINISH));
				vertexToRelocate = -1;
				return;
			} else if (vertices.contains(newVertex)) {
				vertexToRelocate = vertices.indexOf(newVertex);
				changeType = ClipToolChangeEvent.Cause.RESIZE_START;
			} else {
				reset();
				Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.RESTART));
				vertices.add(newVertex);
				return;
			}
		} else if (vertexCount == requiredVertexCount - 1) {
			vertices.add(newVertex);
			setVertices(ClipFactory.createVertices(getVertices(), shape));
			changeType = ClipToolChangeEvent.Cause.COMPLETE;
		} else {
			vertices.add(newVertex);
			changeType = ClipToolChangeEvent.Cause.PROGRESS;
		}
		Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, changeType));
	}
	
	public boolean isComplete() {
		return vertices.size() >= shape.getVertexCount();
	}
}
