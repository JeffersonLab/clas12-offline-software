package cnuphys.snr.test;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TestSupport {

	/**
	 * This converts a screen or pixel point to a world point.
	 * 
	 * @param localSystem a rectangle defining the local (screen-pixel) coordinate
	 *                    system.
	 * @param pp          contains the local (screen-pixel) point.
	 * @param wp          will hold the resultant world point.
	 */
	public static void toWorld(Rectangle2D.Double wr, Rectangle localSystem, Point pp, Point2D.Double wp) {
		if ((localSystem != null) && (localSystem.height > 0) && (localSystem.width > 0)) {
			double xx = wr.x + pp.getX() * wr.width / localSystem.getWidth();
			double yy = wr.y + (localSystem.height - pp.getY()) * wr.height / localSystem.getHeight();
			wp.setLocation(xx, yy);
		}
	}

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param localSystem a rectangle defining the local (screen-pixel) coordinate
	 *                    system.
	 * @param pp          will hold the resultant local (screen-pixel) point.
	 * @param wp          contains world point.
	 */
	public static void toLocal(Rectangle2D.Double wr, Rectangle localSystem, Point pp, Point2D.Double wp) {
		toLocal(wr, localSystem, pp, wp.x, wp.y);
	}

	/**
	 * This converts a world rectangle to a screen or pixel rect.
	 * 
	 * @param localSystem a rectangle defining the local (screen-pixel) coordinate
	 *                    system.
	 * @param r           will hold the resultant local (screen-pixel) rectangle.
	 * @param wr          contains the world rectangle.
	 */
	public static void toLocal(Rectangle2D.Double world, Rectangle localSystem, Rectangle r, Rectangle.Double wr) {
		Point2D.Double wp0 = new Point2D.Double(wr.x, wr.y);
		Point2D.Double wp1 = new Point2D.Double(wr.x + wr.width, wr.y + wr.height);
		Point p0 = new Point();
		Point p1 = new Point();
		toLocal(world, localSystem, p0, wp0);
		toLocal(world, localSystem, p1, wp1);
		int x = Math.min(p0.x, p1.x);
		int y = Math.min(p0.y, p1.y);
		int w = Math.abs(p1.x - p0.x);
		int h = Math.abs(p1.y - p0.y);
		r.setBounds(x, y, w, h);
	}

	/**
	 * This converts a screen or local rectangle to world rect.
	 * 
	 * @param localSystem a rectangle defining the local (screen-pixel) coordinate
	 *                    system.
	 * @param r           contains the local (screen-pixel) rectangle.
	 * @param wr          will hold the resultant world rectangle.
	 */
	public static void toWorld(Rectangle2D.Double world, Rectangle localSystem, Rectangle r, Rectangle.Double wr) {
		Point p0 = new Point(r.x, r.y);
		Point p1 = new Point(r.x + r.width, r.y + r.height);
		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		toWorld(world, localSystem, p0, wp0);
		toWorld(world, localSystem, p1, wp1);
		double x = Math.min(wp0.x, wp1.x);
		double y = Math.min(wp0.y, wp1.y);
		double w = Math.abs(wp1.x - wp0.x);
		double h = Math.abs(wp1.y - wp0.y);
		wr.setFrame(x, y, w, h);
	}

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param localSystem a rectangle defining the local (screen-pixel) coordinate
	 *                    system.
	 * @param pp          will hold the resultant local (screen-pixel) point.
	 * @param wx          the world x coordinate.
	 * @param wy          the world y coordinate.
	 */
	public static void toLocal(Rectangle2D.Double wr, Rectangle localSystem, Point pp, double wx, double wy) {
		if ((localSystem != null) && (localSystem.height > 0) && (localSystem.width > 0)) {
			double xx = (wx - wr.x) * localSystem.getWidth() / wr.width;
			double yy = localSystem.height - (wy - wr.y) * localSystem.getHeight() / wr.height;
			pp.setLocation(xx, yy);
		}
	}

}
