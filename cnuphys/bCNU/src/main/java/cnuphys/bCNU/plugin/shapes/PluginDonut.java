package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;

import cnuphys.bCNU.item.DonutItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginDonut extends PluginShape {

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
	 * @param radiusInner the inner radius
	 * @param radiusOuter the outer radius
	 * @param startAngle the startAngle in degrees measured like the usual polar angle theta
	 * @param arcAngle
	 *            the opening angle COUNTERCLOCKWISE in degrees.
	 * @param properties
	 *            extra custom properties
	 */
	public PluginDonut(Plugin plugin, String info, double xc, double yc,
			double radiusInner, double radiusOuter, double startAngle, double openingAngle, Object... properties) {
		super(plugin, PluginShapeType.DONUT);
		Point2D.Double center = new Point2D.Double(xc, yc);
		_item = new DonutItem(plugin.getView().getShapeLayer(), center, radiusInner, radiusOuter, startAngle, openingAngle);
		commonInit(info, PropertySupport.fromKeyValues(properties));
	}

}
