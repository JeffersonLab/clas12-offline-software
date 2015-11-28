package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;
import cnuphys.bCNU.item.LineItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginSupport;

public class PluginLine extends PluginShape {

    /**
     * Create a line shape
     * 
     * @param plugin the plugin that will hold it.
     * @param info a descriptive string that may appear on mouseovers
     * @param x1 the horizontal world coordinate (not pixels) of one end point
     * @param y1 the vertical world coordinate (not pixels) of one end point
     * @param x2 the horizontal world coordinate (not pixels) of the other end point
     * @param y2 the vertical world coordinate (not pixels) of the other end point
     * @param properties extra custom properties
      */
    public PluginLine(Plugin plugin, String info, double x1, double y1, 
	    double x2, double y2, Object ...properties) {
	super(plugin, PluginShapeType.LINE);
	Point2D.Double p1 = new Point2D.Double(x1, y1);
	Point2D.Double p2 = new Point2D.Double(x2, y2);
	_item = new LineItem(plugin.getView().getShapeLayer(), p1, p2);
	commonInit(info, PluginSupport.fromKeyValues(properties));
    }

}
