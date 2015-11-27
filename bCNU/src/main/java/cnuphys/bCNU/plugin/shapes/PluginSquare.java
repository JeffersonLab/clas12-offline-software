package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginSupport;

public class PluginSquare extends PluginShape {

    /**
     * Create a rectangle shape
     * 
     * @param plugin the plugin that will hold it.
     * @param name a descriptive name that may appear on mouseovers
     * @param xc the horizontal center of the square in world (not pixel) coordinates
     * @param yc the vertical center of the square in world (not pixel) coordinates
     * @param length the length of a side in world coordinates. Note: the square will only look
     * square if the plugin's world system has a 1:1 aspect ratio.
     * @param properties extra custom properties
      */
    public PluginSquare(Plugin plugin, String name, double xc, double yc, double length, Object ...properties) {
	super(plugin, PluginShapeType.SQUARE);
	double halfLen = length/2.;
	double xmin = xc - halfLen;
	double ymin = yc - halfLen;
	Rectangle2D.Double wr = new Rectangle2D.Double(xmin, ymin, length, length);
	_item = new RectangleItem(plugin.getView().getShapeLayer(), wr);
	commonInit(name, PluginSupport.fromKeyValues(properties));
    }

}
