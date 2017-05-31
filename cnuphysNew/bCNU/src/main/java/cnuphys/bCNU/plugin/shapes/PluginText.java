package cnuphys.bCNU.plugin.shapes;

import java.awt.geom.Point2D;

import cnuphys.bCNU.item.TextItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginText extends PluginShape {

    /**
     * Create a text shape 
     * 
     * @param plugin the plugin that will hold it.
     * @param info this will be the text
     * @param x the horizontal location of the left of the text in world (not pixel) coordinates
     * @param y the vertical location of the baseline of the text in world (not pixel) coordinates
     * @param properties extra custom properties
      */
    public PluginText(Plugin plugin, String info, double x, double y, Object ...properties) {
	super(plugin, PluginShapeType.TEXT);
	Point2D.Double location = new Point2D.Double(x, y);
	_item = new TextItem(plugin.getView().getShapeLayer(), location, PropertySupport.defaultFont,
			info, PropertySupport.defaultTextColor, null, null);
	commonInit(info, PropertySupport.fromKeyValues(properties));
	_item.setResizable(false);
	_item.setRotatable(false);
    }

}
