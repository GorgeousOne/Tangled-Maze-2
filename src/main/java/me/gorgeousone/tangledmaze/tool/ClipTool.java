package me.gorgeousone.tangledmaze.tool;

import me.gorgeousone.tangledmaze.clip.ClipFactory;
import me.gorgeousone.tangledmaze.clip.ClipType;
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
	private ClipType type;
	private int vertexToRelocate = -1;
	
	public ClipTool(UUID playerId, ClipType type) {
		this.playerId = playerId;
		vertices = new ArrayList<>();
		this.type = type;
	}
	
	public void reset() {
		vertices.clear();
		vertexToRelocate = -1;
		Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.RESTART));
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
	
	public ClipType getShape() {
		return type;
	}
	
	public void setType(ClipType type) {
		this.type = type;
	}
	
	public void addVertex(Block clickedBlock) {
		Block newVertex = BlockUtil.getSurface(clickedBlock);
		int vertexCount = vertices.size();
		int requiredVertexCount = type.getVertexCount();
		ClipToolChangeEvent.Cause changeType;
		
		//clip is already complete
		if (vertexCount >= requiredVertexCount) {
			if (vertexToRelocate != -1) {
				vertices.set(vertexToRelocate, newVertex);
				setVertices(ClipFactory.relocateVertices(getVertices(), type, vertexToRelocate));
				Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, ClipToolChangeEvent.Cause.RESIZE_FINISH));
				vertexToRelocate = -1;
				return;
			} else if (vertices.contains(newVertex)) {
				vertexToRelocate = vertices.indexOf(newVertex);
				changeType = ClipToolChangeEvent.Cause.RESIZE_START;
			} else {
				reset();
				vertices.add(newVertex);
				changeType = ClipToolChangeEvent.Cause.PROGRESS;
			}
		} else if (vertexCount == requiredVertexCount - 1) {
			vertices.add(newVertex);
			setVertices(ClipFactory.createVertices(getVertices(), type));
			changeType = ClipToolChangeEvent.Cause.COMPLETE;
		} else {
			vertices.add(newVertex);
			changeType = ClipToolChangeEvent.Cause.PROGRESS;
		}
		Bukkit.getPluginManager().callEvent(new ClipToolChangeEvent(this, changeType));
	}
	
	public boolean isComplete() {
		return vertices.size() >= type.getVertexCount();
	}
}
