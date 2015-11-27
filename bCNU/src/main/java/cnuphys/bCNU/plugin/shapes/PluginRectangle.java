package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginSupport;

public class PluginRectangle extends PluginShape {

    /**
     * Create a rectangle shape
     * 
     * @param plugin the plugin that will hold it.
     * @param name a descriptive name that may appear on mouseovers
     * @param x the left of the rectangle in world (not pixel) coordinates
     * @param y the bottom of the rectangle  in world (not pixel) coordinates
     * @param w the width of the rectangle in world coordinates
     * @param h the height of the rectangle in world coordinates
     * @param properties extra custom properties
     */
    public PluginRectangle(Plugin plugin, String name, double x, double y, double w, double h, Object ...properties) {
	super(plugin, PluginShapeType.RECTANGLE);	
	Rectangle2D.Double wr = new Rectangle2D.Double(x, y, w, h);
	_item = new RectangleItem(plugin.getView().getShapeLayer(), wr);
	commonInit(name, PluginSupport.fromKeyValues(properties));
    }

}
