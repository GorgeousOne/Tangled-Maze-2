package me.gorgeousone.tangledmaze.generation.building;


import me.gorgeousone.tangledmaze.util.blocktype.BlockLocType;

import java.util.Set;

public interface BuildCallBack {
	void onBuildFinish(Set<BlockLocType> backupBlocks);
}
