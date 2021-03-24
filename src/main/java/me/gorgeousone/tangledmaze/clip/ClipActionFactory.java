package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.util.BlockUtils;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClipActionFactory {
	
	public static ClipAction addClip(Clip maze, Clip clip) {
		if (!maze.getWorld().equals(clip.getWorld())) {
			return null;
		}
		ClipAction addition = new ClipAction(maze);
		addProtrudingClipFill(addition, maze, clip);
		//return null if the clip is totally covered by the maze
		if (addition.getAddedFill().isEmpty()) {
			return addition;
		}
		addProtrudingClipBorder(addition, maze, clip);
		removeEnclosedMazeBorder(addition, maze);
		removeThickEnclosedMazeBorder(addition);
		removeMazeExitsInsideClip(maze.getExits(), clip, addition);
		return addition;
	}
	
	/**
	 * Adds all clip fill not contained by the maze clip to the clip action
	 */
	private static void addProtrudingClipFill(ClipAction addition, Clip maze, Clip clip) {
		for (Map.Entry<Vec2, Integer> fillBlock : clip.getFill().entrySet()) {
			Vec2 fillLoc = fillBlock.getKey();
			if (!maze.contains(fillLoc)) {
				addition.addFill(fillLoc, fillBlock.getValue());
			}
		}
	}
	
	private static void addProtrudingClipBorder(ClipAction addition, Clip maze, Clip clip) {
		for (Vec2 otherBorder : clip.getBorder()) {
			if (!maze.contains(otherBorder)) {
				addition.addBorder(otherBorder);
			}
		}
	}
	
	//then own outdated border is being removed. there are also cases where thicker border next to the actual clip has to be removed
	private static void removeEnclosedMazeBorder(ClipAction addition, Clip maze) {
		for (Vec2 ownBorder : maze.getBorder()) {
			if (!touchesExternal(addition, ownBorder, Direction.values())) {
				addition.removeBorder(ownBorder);
			}
		}
	}
	
	//now the recently added border needs undergo another check, if it is actually sufficient and also not too thick
	private static void removeThickEnclosedMazeBorder(ClipAction addition) {
		addition.getAddedBorder().removeIf(newBorder -> !touchesExternal(addition, newBorder, Direction.values()));
	}
	
	
	private static void removeMazeExitsInsideClip(List<Vec2> exits, Clip clip, ClipAction changes) {
		for (Vec2 exit : exits) {
			if (clip.contains(exit)) {
				changes.removeExit(exit);
			}
		}
	}
	
	/**
	 * Creates a {@link ClipAction} of the part of the passed clip that will be removed from the maze.
	 * Returns null if worlds are not matching or the clip is not intersecting the maze.
	 *
	 * @param clip Clip to be removed from the maze
	 * @return ClipAction of the deletion
	 */
	public static ClipAction removeClip(Clip maze, Clip clip) {
		if (!maze.getWorld().equals(clip.getWorld())) {
			return null;
		}
		ClipAction deletion = new ClipAction(maze);
		removeOverlappingClipFill(deletion, maze, clip);
		
		if (deletion.getRemovedFill().isEmpty()) {
			return null;
		}
		addIntersectingClipBorder(deletion, maze, clip);
		removeExcludedMazeBorder(deletion, maze, clip);
		removeMazeExitsInsideClip(maze.getExits(), clip, deletion);
		return deletion;
	}
	
	private static void removeOverlappingClipFill(ClipAction deletion, Clip maze, Clip clip) {
		for (Map.Entry<Vec2, Integer> otherFill : clip.getFill().entrySet()) {
			if (!clip.borderContains(otherFill.getKey()) && maze.contains(otherFill.getKey())) {
				deletion.removeFill(otherFill.getKey(), otherFill.getValue());
			}
		}
	}
	
	private static void addIntersectingClipBorder(ClipAction deletion, Clip maze, Clip clip) {
		for (Vec2 otherBorder : clip.getBorder()) {
			if (!maze.borderContains(otherBorder) && maze.contains(otherBorder)) {
				deletion.addBorder(otherBorder);
			}
		}
		//remove every part of the new added border, which is not functional border anyway
		Iterator<Vec2> iterator = deletion.getAddedBorder().iterator();
		
		while (iterator.hasNext()) {
			Vec2 newBorder = iterator.next();
			
			if (!touchesFill(deletion, newBorder, Direction.values())) {
				iterator.remove();
				deletion.removeFill(newBorder, maze.getY(newBorder));
			}
		}
	}
	
	private static void removeExcludedMazeBorder(ClipAction deletion, Clip maze, Clip clip) {
		for (Vec2 ownBorder : maze.getBorder()) {
			if (clip.contains(ownBorder) && !touchesFill(deletion, ownBorder, Direction.values())) {
				deletion.removeBorder(ownBorder);
				deletion.removeFill(ownBorder, maze.getY(ownBorder));
			}
		}
	}
	
	/**
	 * Creates a {@link ClipAction} with information about blocks in order to expand the maze border at the given block.
	 * Returns null if the block is not part of the maze border (see {@link Clip#isBorderBlock(Block)}).
	 *
	 * @param block the block where the maze border should be expanded
	 * @return ClipAction of the expansion
	 */
	public static ClipAction expandBorder(Clip maze, Block block) {
		if (!maze.isBorderBlock(block)) {
			return null;
		}
		Vec2 blockVec = new Vec2(block);
		ClipAction expansion = new ClipAction(maze);
		
		extendMazeBorder(expansion, maze, blockVec);
		removeIntrusiveMazeBorder(expansion, maze, blockVec);
		return expansion;
	}
	
	/**
	 * Add directly surrounding blocks to the maze border
	 *
	 * @param maze
	 * @param loc
	 * @param expansion
	 */
	private static void extendMazeBorder(ClipAction expansion, Clip maze, Vec2 loc) {
		expansion.removeBorder(loc);
		
		for (Direction dir : Direction.values()) {
			
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			int height = BlockUtils.getSurfaceY(maze.getWorld(), neighbor, maze.getY(loc));
			
			if (!maze.contains(neighbor)) {
				
				expansion.addFill(neighbor, height);
				expansion.addBorder(neighbor);
				
			} else if (maze.exitsContain(neighbor) && !sealsClipBorder(expansion, neighbor, Direction.fourCardinals())) {
				expansion.removeExit(neighbor);
			}
		}
	}
	
	//look for neighbors, that are now intruding the border unnecessarily around the expanded block
	private static void removeIntrusiveMazeBorder(ClipAction expansion, Clip maze, Vec2 loc) {
		for (Direction dir : Direction.values()) {
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			
			if (maze.borderContains(neighbor) && !sealsClipBorder(expansion, neighbor, Direction.values())) {
				expansion.removeBorder(neighbor);
			}
		}
	}
	
	/**
	 * Creates a {@link ClipAction} with information about blocks in order to reduce the maze border at the given block.
	 * Returns null if the block is not part of the maze border (see {@link Clip#isBorderBlock(Block)}).
	 *
	 * @param block the block where the maze border should be reduced/erased
	 * @return ClipAction of the expansion
	 */
	public static ClipAction eraseBorder(Clip maze, Block block) {
		if (!maze.isBorderBlock(block)) {
			return null;
		}
		Vec2 blockVec = new Vec2(block);
		ClipAction action = new ClipAction(maze);
		action.removeBorder(blockVec);
		reduceMazeBorderAroundBlock(action, maze, blockVec);
		removeProtrusiveMazeBorder(action, maze, blockVec);
		return action;
	}
	
	private static void reduceMazeBorderAroundBlock(ClipAction erasure, Clip maze, Vec2 loc) {
		if (maze.exitsContain(loc)) {
			erasure.removeExit(loc);
		}
		erasure.removeBorder(loc);
		erasure.removeFill(loc, maze.getY(loc));
		
		if (!sealsClipBorder(erasure, loc, Direction.values())) {
			return;
		}
		
		for (Direction dir : Direction.values()) {
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			
			if (maze.contains(neighbor) && !maze.borderContains(neighbor)) {
				erasure.addBorder(neighbor);
			}
			if (maze.exitsContain(neighbor) && !sealsClipBorder(erasure, neighbor, Direction.fourCardinals())) {
				erasure.removeExit(neighbor);
			}
		}
	}
	
	private static void removeProtrusiveMazeBorder(ClipAction erasure, Clip maze, Vec2 loc) {
		//detect outstanding neighbor borders of the block
		for (Direction dir : Direction.values()) {
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			//remove the neighbor if it still stands out
			if (maze.borderContains(neighbor) && !sealsClipBorder(erasure, neighbor, Direction.values())) {
				int height = maze.getY(neighbor);
				erasure.removeFill(neighbor, height);
			}
		}
	}
	
	public static boolean sealsClipBorder(ClipAction changes, Vec2 loc, Direction[] directions) {
		boolean touchesFill = false;
		boolean touchesExternal = false;
		
		for (Direction dir : directions) {
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			
			if (!changes.clipWillContain(neighbor)) {
				touchesExternal = true;
			} else if (!changes.clipBorderWillContain(neighbor)) {
				touchesFill = true;
			}
			if (touchesFill && touchesExternal) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean touchesFill(ClipAction changes, Vec2 loc, Direction[] directions) {
		for (Direction dir : directions) {
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			
			if (!changes.clipBorderWillContain(neighbor) && changes.clipWillContain(neighbor)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean touchesExternal(ClipAction changes, Vec2 loc, Direction[] directions) {
		for (Direction dir : directions) {
			Vec2 neighbor = loc.clone().add(dir.getVec2());
			
			if (!changes.clipWillContain(neighbor)) {
				return true;
			}
		}
		return false;
	}
}
