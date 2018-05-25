package cnuphys.bCNU.plugin;

import java.awt.Point;
import java.awt.geom.Point2D;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.plugin.shapes.PluginShape;

public class PluginSupport {

	/**
	 * Convenience method to covert separate x and y ordered arrays into and
	 * array of Point2D.Double objects. The x and y arrays should have the same
	 * length.
	 * 
	 * @param x
	 *            the x array
	 * @param y
	 *            the y array
	 * @return a corresponding array of Point2D objects,
	 */
	public static Point2D.Double[] fromXYArrays(double x[], double y[]) {
		if ((x == null) || (y == null)) {
			return null;
		}

		int len = Math.min(x.length, y.length);
		if (len < 1) {
			return null;
		}

		Point2D.Double[] p2D = new Point2D.Double[len];
		for (int i = 0; i < len; i++) {
			p2D[i] = new Point2D.Double(x[i], y[i]);
		}
		return p2D;
	}

	/**
	 * Get the top most shape containing the given screen point
	 * 
	 * @param plugin
	 *            the plugin in question
	 * @param p
	 *            the mouse location
	 * @return the topmost shape containing this point, or <code>null</code>.
	 */

	public static PluginShape getShapeAtPoint(Plugin plugin, Point p) {
		if ((plugin == null) || (p == null)) {
			return null;
		}

		AItem item = plugin.getView().getContainer().getItemAtPoint(p);
		if (item == null) {
			return null;
		}
		return plugin.fromItem(item);
	}
}
