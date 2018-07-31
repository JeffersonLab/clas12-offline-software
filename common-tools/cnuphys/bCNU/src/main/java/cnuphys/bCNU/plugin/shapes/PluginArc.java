package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;

import cnuphys.bCNU.item.ArcItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginArc extends PluginShape {

	/**
	 * Create a circle shape
	 * 
	 * @param plugin
	 *            the plugin that will hold it.
	 * @param info
	 *            a descriptive info that may appear on mouseovers
	 * @param xc
	 *            the horizontal center of the circle in world (not pixel)
	 *            coordinates
	 * @param yc
	 *            the vertical center of the circle in world (not pixel)
	 *            coordinates
	 * @param radius
	 *            the radius in world coordinates. Note: the arc will only look
	 *            circular if the plugin's world system has a 1:1 aspect ratio.
	 * @param startAngle
	 *            start angle in degrees measured
	 * @param openingAngle
	 * @param properties
	 *            extra custom properties
	 */
	public PluginArc(Plugin plugin, String info, double xc, double yc, double radius, double startAngle,
			double openingAngle, Object... properties) {
		super(plugin, PluginShapeType.ARC);
		Point2D.Double center = new Point2D.Double(xc, yc);
		_item = new ArcItem(plugin.getView().getShapeLayer(), center, radius, startAngle, openingAngle);
		commonInit(info, PropertySupport.fromKeyValues(properties));
	}

}
