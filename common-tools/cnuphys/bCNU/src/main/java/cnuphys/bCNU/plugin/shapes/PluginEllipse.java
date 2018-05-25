package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;
import cnuphys.bCNU.item.EllipseItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginEllipse extends PluginShape {

	/**
	 * Create a rectangle shape
	 * 
	 * @param plugin
	 *            the plugin that will hold it.
	 * @param info
	 *            a descriptive string that may appear on mouseovers
	 * @param x
	 *            the left of the bounding rectangle in world (not pixel)
	 *            coordinates
	 * @param y
	 *            the bottom of the bounding rectangle in world (not pixel)
	 *            coordinates
	 * @param w
	 *            the width of the bounding rectangle in world coordinates
	 * @param h
	 *            the height of the bounding rectangle in world coordinates
	 * @param properties
	 *            extra custom properties
	 */
	public PluginEllipse(Plugin plugin, String info, double x, double y, double w, double h, Object... properties) {
		super(plugin, PluginShapeType.ELLIPSE);
		Point2D.Double center = new Point2D.Double(x + w / 2, y + h / 2);
		_item = new EllipseItem(plugin.getView().getShapeLayer(), w, h, 0, center);
		commonInit(info, PropertySupport.fromKeyValues(properties));
	}

}
