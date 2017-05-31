package cnuphys.bCNU.drawable;

import java.awt.Graphics;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.visible.IVisible;

/**
 * Used to draw things. Typically extra user supplied drawing methods.
 * 
 * @author heddle
 * 
 */
public interface IDrawable extends IVisible {

	/**
	 * Draw the drawable.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	public void draw(Graphics g, IContainer container);

	/**
	 * This tells the drawable, for example that any thing is has cached, such
	 * as a pixel based polygon, needs to be recomputed.
	 * 
	 * @param dirty
	 *            the value of the dirty flag.
	 */
	public void setDirty(boolean dirty);

	/**
	 * Called when the drawable is about to be removed from a list.
	 */
	public void prepareForRemoval();

}
