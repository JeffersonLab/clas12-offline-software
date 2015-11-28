package cnuphys.bCNU.plugin.shapes;

import java.awt.Color;
import java.util.Properties;
import java.util.UUID;

import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.PathBasedItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginProperties;

public class PluginShape {

    //the parent plugin
    private Plugin _plugin;
    
    //the underlying BCNU item
    protected AItem _item;
    
    //the shape type
    private PluginShapeType _shapeType;
    
    //data relevant for ced
    private int _sector;
    private int _superlayer;
    private int _layer;
    private int _component;
    private int _crate;
    private int _slot;
    private int _channel;
        
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
	
	//initial rotation angle (degrees)
	double angle = PluginProperties.getRotationAngle(props);
	if (Math.abs(angle) > 1.0e-6) {
	    if (_item instanceof PathBasedItem) {
		//minus to get ccw rotation
		((PathBasedItem)_item).rotate(-angle);
	    }
	}
	
	//ced identifications (yeah shouldn't be here)
	_sector = PluginProperties.getSector(props);
	_superlayer = PluginProperties.getSuperlayer(props);
	_layer = PluginProperties.getLayer(props);
	_component = PluginProperties.getComponent(props);
	_crate = PluginProperties.getCrate(props);
	_slot = PluginProperties.getSlot(props);
	_channel = PluginProperties.getChannel(props);
	
    }

    //common final initialization
    protected void commonInit(String info, Properties properties) {
	_item.setVisible(true);	
	_item.setName(info);
	processProperties(properties);	
	_plugin.addShape(this);
    }
    
    /**
     * Set the shapes's info string
     * @param info the info string of the shape
     */
    public void setInfoString(String info) {
	_item.setName((info == null) ? "???" : info);
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
     * Get the info string of the shape
     * @return the info string of the shape
     */
    public String getInfoString() {
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
     * Set the symbol type. Only relevant for PluginSymbol shapes.
     * This does not cause a redraw.
     * @param symbol the symbol.
     */
    public void setSymbol(SymbolType symbol) {
	if (_shapeType == PluginShapeType.SYMBOL) {
	    _item.getStyle().setSymbolType(symbol);
	}
    }
    
    /**
     * Set the symbol size. Only relevant for PluginSymbol shapes.
     * This does not cause a redraw.
     * @param size the symbol size (width and height) in pixels.
     */
    public void setSymbolSize(int size) {
	if (_shapeType == PluginShapeType.SYMBOL) {
	    //keep reasonable
	    size = Math.max(4, Math.min(40, size));
	    _item.getStyle().setSymbolSize(size);
	}
    }
    
    /**
     * Set the line style
     * @param lineStyle the style to set
     * @see cnuphys.bCNU.graphics.style.LineStyle
     */
    public void setLineStyle(LineStyle lineStyle) {
	_item.getStyle().setLineStyle(lineStyle);
    }
    
    /**
     * Get the sector id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
     * @return the sector id.
     */
    public int getSector() {
        return _sector;
    }

    /**
     * Get the superlayer id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
     * @return the superlayer id.
     */
   public int getSuperlayer() {
        return _superlayer;
    }

   /**
    * Get the layer id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
    * @return the layer id.
    */
    public int getLayer() {
        return _layer;
    }

    /**
     * Get the component id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
     * @return the component id.
     */
    public int getComponent() {
        return _component;
    }

    /**
     * Get the crate id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
     * @return the crate id.
     */
    public int getCrate() {
        return _crate;
    }

    /**
     * Get the slot id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
     * @return the slot id.
     */
    public int getSlot() {
        return _slot;
    }

    /**
     * Get the channel id. A bad value is indicated by Integer.MIN_VALUE (-2^31 = -2147483648)
     * @return the channel id.
     */
    public int getChannel() {
        return _channel;
    }

}
