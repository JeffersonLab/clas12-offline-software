package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;

import cnuphys.bCNU.item.PolylineItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginSupport;
import cnuphys.bCNU.util.PropertySupport;

public class PluginPolyline extends PluginShape {
    /**
     * Create a polyline shape
     * 
     * @param plugin the plugin that will hold it.
     * @param info a descriptive string that may appear on mouseovers
     * @param points array of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @see java.awt.geom.Point2D
     */
    public PluginPolyline(Plugin plugin, String info, Point2D.Double points[], Object ...properties) {
	super(plugin, PluginShapeType.POLYLINE);	
	_item = new PolylineItem(plugin.getView().getShapeLayer(), points);
	commonInit(info, PropertySupport.fromKeyValues(properties));
    }
    
    /**
     * Create a polyline shape from separate (ordered) x and y arrays. The x and
     * y arrays should have the same length,
     * 
     * @param plugin the plugin that will hold it.
     * @param info a descriptive string that may appear on mouseovers
     * @param x horizontal values of vertex points in world (not pixel) coordinates.
     * @param y vertical values of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @see java.awt.geom.Point2D
     */
    public PluginPolyline(Plugin plugin, String info, double x[], double y[], Object ...properties) {
	this(plugin, info, PluginSupport.fromXYArrays(x, y), properties);
    }

}
