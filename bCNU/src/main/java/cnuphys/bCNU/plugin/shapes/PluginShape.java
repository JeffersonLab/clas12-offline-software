package cnuphys.bCNU.plugin.shapes;

import java.awt.Color;
import java.util.Properties;
import java.util.UUID;

import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginProperties;

public class PluginShape {

    //the parent plugin
    private Plugin _plugin;
    
    //the underlying BCNU item
    protected AItem _item;
    
    //the shape type
    private PluginShapeType _shapeType;
        
    /**
     * Constructor
     * @param plugin the owner plugin where this shape is displayed.
     * @param shapeType the shape type
     * @see PluginShapeType
     */
    protected PluginShape(Plugin plugin, PluginShapeType shapeType) {
	_plugin = plugin;
	_shapeType = shapeType;
    }

    /**
     * Get the plugin that this shape lives on
     * @return the plugin that this shape lives on
     */
    public Plugin getPlugin() {
	return _plugin;
    }
    
    /**
     * This gets the underlying BCNU item. It shold never
     * be used by routine plugin developers.
     * @return the underlying BCNU item
     */
    public AItem getItem() {
	return _item;
    }
    
    /**
     * Get the unique UUID for this shape.
     * @return the unique UUID for this shape.
     */
    public UUID getUUID() {
	return (_item == null) ? null : _item.getUuid();
    }
    
    /**
     * Get the plugin shape type
     * @return the plugin shape type
     */
    public PluginShapeType getShapeType() {
	return _shapeType;
    }
    
    /**
     * Process the custom properties
     * @param props the properties
     */
    protected void processProperties(Properties props) {
	_item.setLocked(true);
	
	if ((props == null) || props.isEmpty()) {
	    return;
	}
	
	//fill color and line color
	Color fillColor = PluginProperties.getFillColor(props);
	if (fillColor != null) {
	    setFillColor(fillColor);
	}

	Color lineColor = PluginProperties.getLineColor(props);
	if (lineColor != null) {
	    setLineColor(lineColor);
	}
	
	//line width and style
	int lineWidth = PluginProperties.getLineWidth(props);
	setLineWidth(lineWidth);
	
	LineStyle lineStyle = PluginProperties.getLineStyle(props);
	setLineStyle(lineStyle);

	// item locked
	boolean locked = PluginProperties.getLocked(props);
	if (!locked) {
	    _item.setLocked(false);
	    _item.setRotatable(true);
	    _item.setResizable(false);
	    _item.setDraggable(true);
	}
	
    }

    //common final initialization
    protected void commonInit(String name, Properties properties) {
	_item.setVisible(true);	
	_item.setName(name);
	processProperties(properties);	
	_plugin.addShape(this);
    }
    
    /**
     * Set the line color for the shape. This does not cause a redraw.
     * @param color the line color.
     */
    public void setLineColor(Color color) {
	_item.getStyle().setLineColor(color);
    }

    /**
     * Set the fill color for the shape. This does not cause a redraw.
     * @param color the fill color.
     */
    public void setFillColor(Color color) {
	_item.getStyle().setFillColor(color);
    }
    
    /**
     * Get the name of the shape
     * @return the name of the shape
     */
    public String getName() {
	return (_item == null) ? "???" : _item.getName();
    }
    
    /**
     * Set the line width for the shape. This does not cause a redraw.
     * @param lineWidth the line width in pixels [0..10]
     */
    public void setLineWidth(int lineWidth) {
	//keep within reason
	lineWidth = Math.max(0, Math.min(10, lineWidth));
	_item.getStyle().setLineWidth(lineWidth);
    }
    
    /**
     * Set the line style
     * @param lineStyle the style to set
     * @see cnuphys.bCNU.graphics.style.LineStyle
     */
    public void setLineStyle(LineStyle lineStyle) {
	_item.getStyle().setLineStyle(lineStyle);
    }
}
