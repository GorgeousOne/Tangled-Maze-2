package me.gorgeousone.tangledmaze.generation.building;

import org.bukkit.block.BlockState;

import java.util.Set;

public interface BuildCallBack {
	void onBuildFinish(Set<BlockState> backupBlocks);
}
