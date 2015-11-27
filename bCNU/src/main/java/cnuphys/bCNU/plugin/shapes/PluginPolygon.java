package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginSupport;

public class PluginPolygon extends PluginShape {

    /**
     * Create a closed polygon shape
     * 
     * @param plugin the plugin that will hold it.
     * @param name a descriptive name that may appear on mouseovers
     * @param points array of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @see java.awt.geom.Point2D
     */
    public PluginPolygon(Plugin plugin, String name, Point2D.Double points[], Object ...properties) {
	super(plugin, PluginShapeType.POLYGON);	
	_item = new PolygonItem(plugin.getView().getShapeLayer(), points);
	commonInit(name, PluginSupport.fromKeyValues(properties));
    }
    
    /**
     * Create a closed polygon shape from separate (ordered) x and y arrays. The x and
     * y arrays should have the same length,
     * 
     * @param plugin the plugin that will hold it.
     * @param name a descriptive name that may appear on mouseovers
     * @param x horizontal values of vertex points in world (not pixel) coordinates.
     * @param y vertical values of vertex points in world (not pixel) coordinates.
     * @param properties extra custom properties
     * @see java.awt.geom.Point2D
     */
    public PluginPolygon(Plugin plugin, String name, double x[], double y[], Object ...properties) {
	this(plugin, name, PluginSupport.fromXYArrays(x, y), properties);
    }

}
