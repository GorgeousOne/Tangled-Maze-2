package me.gorgeousone.tangledmaze.clip;

import me.gorgeousone.tangledmaze.util.BlockUtils;
import me.gorgeousone.tangledmaze.util.Direction;
import me.gorgeousone.tangledmaze.util.Vec2;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Map;

/**
 * A factory for creating clip actions
 */
public class ClipActionFactory {
	
	/**
	 * Creates a clip action for merging the given clip into the given maze
	 */
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
		removeMazeExits(maze.getExits(), addition);
		return addition;
	}
	
	/**
	 * Adds all fill of the clip that is not contained by the maze to the addition
	 */
	private static void addProtrudingClipFill(ClipAction addition, Clip maze, Clip clip) {
		for (Map.Entry<Vec2, Integer> fillBlock : clip.getFill().entrySet()) {
			Vec2 fillLoc = fillBlock.getKey();
			if (!maze.contains(fillLoc)) {
				addition.addFill(fillLoc, fillBlock.getValue());
			}
		}
	}
	
	/**
	 * Adds all border of the clip that is not contained by the maze to the addition
	 */
	private static void addProtrudingClipBorder(ClipAction addition, Clip maze, Clip clip) {
		for (Vec2 clipBorder : clip.getBorder()) {
			if (!maze.contains(clipBorder)) {
				addition.addBorder(clipBorder);
			}
		}
	}
	
	/**
	 * Removes maze border that won't seal the maze after merging with the clip anymore.
	 */
	private static void removeEnclosedMazeBorder(ClipAction addition, Clip maze) {
		for (Vec2 mazeBorder : maze.getBorder()) {
			if (!touchesExternal(addition, mazeBorder, Direction.values())) {
				addition.removeBorder(mazeBorder);
			}
		}
		addition.getAddedBorder().removeIf(newBorder -> !touchesExternal(addition, newBorder, Direction.values()));
	}
	
	/**
	 * Removes maze exits that won't be lying on maze border after the clip action is applied
	 */
	private static void removeMazeExits(List<Vec2> exits, ClipAction changes) {
		for (Vec2 exit : exits) {
			if (!changes.clipBorderWillContain(exit)) {
				changes.removeExit(exit);
			}
		}
	}
	
	/**
	 * Creates a clip action for cutting away the area inside this clip from the given maze
	 */
	public static ClipAction removeClip(Clip maze, Clip clip) {
		if (!maze.getWorld().equals(clip.getWorld())) {
			return null;
		}
		ClipAction deletion = new ClipAction(maze);
		removeOverlappingClipFill(deletion, maze, clip);
		
		if (deletion.getRemovedFill().isEmpty()) {
			return deletion;
		}
		addIntersectingClipBorder(deletion, maze, clip);
		removeExcludedMazeBorder(deletion, maze, clip);
		removeMazeExits(maze.getExits(), deletion);
		return deletion;
	}
	
	/**
	 * Removes all fill of the clip (without border) that overlaps with the maze from the deletion
	 */
	private static void removeOverlappingClipFill(ClipAction deletion, Clip maze, Clip clip) {
		for (Map.Entry<Vec2, Integer> clipFill : clip.getFill().entrySet()) {
			if (!clip.borderContains(clipFill.getKey()) && maze.contains(clipFill.getKey())) {
				deletion.removeFill(clipFill.getKey(), clipFill.getValue());
			}
		}
	}
	
	/**
	 * Adds new border to the deletion where the clips edge cuts into the maze
	 */
	private static void addIntersectingClipBorder(ClipAction deletion, Clip maze, Clip clip) {
		for (Vec2 clipBorder : clip.getBorder()) {
			if (!maze.borderContains(clipBorder) && maze.contains(clipBorder)) {
				deletion.addBorder(clipBorder);
			}
		}
		//remove every part of the new added border not sealing the maze anyway
//		Iterator<Vec2> iterator = deletion.getAddedBorder().iterator();
//
//		while (iterator.hasNext()) {
//			Vec2 newBorder = iterator.next();
//
//			if (!touchesFill(deletion, newBorder, Direction.values())) {
//				iterator.remove();
//				deletion.removeFill(newBorder, maze.getY(newBorder));
//			}
//		}
	}
	
	/**
	 * Removes maze border lying inside the cut away clip area from the deletion
	 */
	private static void removeExcludedMazeBorder(ClipAction deletion, Clip maze, Clip clip) {
		for (Vec2 mazeBorder : maze.getBorder()) {
			if (clip.contains(mazeBorder) && !touchesFill(deletion, mazeBorder, Direction.values())) {
				deletion.removeBorder(mazeBorder);
				deletion.removeFill(mazeBorder, maze.getY(mazeBorder));
			}
		}
	}
	
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
