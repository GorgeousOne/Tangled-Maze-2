package me.gorgeousone.tangledmaze.generation.paving;

/**
 * Enum to differentiate in which grid cells paths can be built, are built or are blocked.
 */
public enum PathType {
	FREE, //cells where paths can be built
	PAVED, //cells where paths are built
	BLOCKED, //cells containing walls or non-maze areas
	ROOM, //cells in a room that can be connected to/replaced with paths
}
