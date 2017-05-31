package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;

import cnuphys.bCNU.item.PointItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginSymbol extends PluginShape {

    /**
     * Create a symbol shape (a point with a symbol)
     * 
     * @param plugin the plugin that will hold it.
     * @param info a descriptive info that may appear on mouseovers
     * @param x the horizontal location of the symbol in world (not pixel) coordinates
     * @param y the vertical location of the symbol in world (not pixel) coordinates
     * @param properties extra custom properties
      */
    public PluginSymbol(Plugin plugin, String info, double x, double y, Object ...properties) {
	super(plugin, PluginShapeType.SYMBOL);
	Point2D.Double wp = new Point2D.Double(x, y);
	_item = new PointItem(plugin.getView().getShapeLayer(), wp);
	commonInit(info, PropertySupport.fromKeyValues(properties));
    }
}
