package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;
import cnuphys.bCNU.item.EllipseItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginCircle extends PluginShape {

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
	 *            the radius in world coordinates. Note: the circle will only
	 *            look circular if the plugin's world system has a 1:1 aspect
	 *            ratio.
	 * @param properties
	 *            extra custom properties
	 */
	public PluginCircle(Plugin plugin, String info, double xc, double yc, double radius, Object... properties) {
		super(plugin, PluginShapeType.CIRCLE);
		Point2D.Double center = new Point2D.Double(xc, yc);
		_item = new EllipseItem(plugin.getView().getShapeLayer(), 2 * radius, 2 * radius, 0, center);
		commonInit(info, PropertySupport.fromKeyValues(properties));
	}

}
