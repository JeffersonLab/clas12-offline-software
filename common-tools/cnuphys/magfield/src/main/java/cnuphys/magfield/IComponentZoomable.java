package cnuphys.magfield;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;

public interface IComponentZoomable {

	/**
	 * Get the component being zoomed
	 * 
	 * @return the component being zoomed
	 */
	public JComponent getComponent();

	/**
	 * Get the current world system
	 * 
	 * @return the current world system
	 */
	public Rectangle.Double getWorldSystem();

	/**
	 * Set the world system
	 * 
	 * @param wr the world system
	 */
	public void setWorldSystem(Rectangle.Double wr);

	/**
	 * Convert a screen point to a world point
	 * 
	 * @param pp the screen point
	 * @param wp the world point
	 */
	public void localToWorld(Point pp, Point.Double wp);

	/**
	 * Convert a world point to a screen point
	 * 
	 * @param pp the screen point
	 * @param wp the world point
	 */
	public void worldToLocal(Point pp, Point.Double wp);

}
