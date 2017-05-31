package cnuphys.bCNU.plugin.shapes;

import java.awt.Color;
import java.awt.Font;
import java.util.Properties;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.PathBasedItem;
import cnuphys.bCNU.item.TextItem;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.PropertySupport;

public class PluginShape {

	// the parent plugin
	private Plugin _plugin;

	// the underlying BCNU item
	protected AItem _item;

	// the shape type
	private PluginShapeType _shapeType;

	// optional user data. This is any object that the user wants
	// to attach to the shape.
	private Object _userData;

	// data relevant for ced
	private int _sector;
	private int _superlayer;
	private int _layer;
	private int _component;
	private int _crate;
	private int _slot;
	private int _channel;

	/**
	 * Constructor
	 * 
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
	 * 
	 * @return the plugin that this shape lives on
	 */
	public Plugin getPlugin() {
		return _plugin;
	}

	/**
	 * This gets the underlying BCNU item. It shold never be used by routine
	 * plugin developers.
	 * 
	 * @return the underlying BCNU item
	 */
	public AItem getItem() {
		return _item;
	}

	/**
	 * Get the plugin shape type
	 * 
	 * @return the plugin shape type
	 */
	public PluginShapeType getShapeType() {
		return _shapeType;
	}

	/**
	 * Process the custom properties
	 * 
	 * @param props the properties
	 */
	protected void processProperties(Properties props) {
		_item.setLocked(true);

		if ((props == null) || props.isEmpty()) {
			return;
		}

		// fill color and line color and text color
		Color fillColor = PropertySupport.getFillColor(props);
		if (fillColor != null) {
			setFillColor(fillColor);
		}

		Color lineColor = PropertySupport.getLineColor(props);
		if (lineColor != null) {
			setLineColor(lineColor);
		}

		Color textColor = PropertySupport.getTextColor(props);
		if (textColor != null) {
			setTextColor(textColor);
		}

		// line width and style
		setLineWidth(PropertySupport.getLineWidth(props));
		setLineStyle(PropertySupport.getLineStyle(props));

		// symbol and symbol size
		setSymbol(PropertySupport.getSymbol(props));
		setSymbolSize(PropertySupport.getSymbolSize(props));

		// font and text color
		setFont(PropertySupport.getFont(props));
		setTextColor(PropertySupport.getTextColor(props));

		// item locked
		boolean locked = PropertySupport.getLocked(props);
		if (!locked) {
			_item.setLocked(false);
			_item.setRotatable(true);
			_item.setResizable(false);
			_item.setDraggable(true);
			_item.setRightClickable(true);
		}

		// initial rotation angle (degrees)
		double angle = PropertySupport.getRotationAngle(props);
		if (Math.abs(angle) > 1.0e-6) {
			if (_item instanceof PathBasedItem) {
				// minus to get ccw rotation
				((PathBasedItem) _item).rotate(-angle);
			}
		}

		// ced identifications (yeah shouldn't be here)
		_sector = PropertySupport.getSector(props);
		_superlayer = PropertySupport.getSuperlayer(props);
		_layer = PropertySupport.getLayer(props);
		_component = PropertySupport.getComponent(props);
		_crate = PropertySupport.getCrate(props);
		_slot = PropertySupport.getSlot(props);
		_channel = PropertySupport.getChannel(props);

		// optional user object that is attached to the shape
		_userData = PropertySupport.getUserData(props);

	}

	// common final initialization
	protected void commonInit(String info, Properties properties) {
		_item.setVisible(true);
		_item.setName(info);
		processProperties(properties);
		_plugin.addShape(this);
	}

	/**
	 * Set the shapes's info string
	 * 
	 * @param info the info string of the shape
	 */
	public void setInfoString(String info) {
		_item.setName((info == null) ? "???" : info);
	}

	/**
	 * Set the line color for the shape. This does not cause a redraw.
	 * 
	 * @param color the line color.
	 */
	public void setLineColor(Color color) {
		_item.getStyle().setLineColor(color);
	}

	/**
	 * Set the text color for the shape. Only relevant for PluginText shapes
	 * This does not cause a redraw.
	 * 
	 * @param color the text colot
	 */
	public void setTextColor(Color color) {
		if (_shapeType == PluginShapeType.TEXT) {
			if (_item instanceof TextItem) {
				((TextItem) _item).setTextColor(color);
			}
		}
	}

	/**
	 * Set the font for the shape. Only relevant for PluginText shapes This does
	 * not cause a redraw.
	 * 
	 * @param font the Font
	 */
	public void setFont(Font font) {
		if (_shapeType == PluginShapeType.TEXT) {
			if (_item instanceof TextItem) {
				((TextItem) _item).setFont(font);
			}
		}
	}

	/**
	 * Set the fill color for the shape. This does not cause a redraw.
	 * 
	 * @param color the fill color.
	 */
	public void setFillColor(Color color) {
		_item.getStyle().setFillColor(color);
	}
	/**
	 * Returns the shapes fill color
	 * 
	 * @return FillColor
	 */
	public Color getFillColor(){
		return _item.getStyle().getFillColor();
	}

	/**
	 * Get the info string of the shape
	 * 
	 * @return the info string of the shape
	 */
	public String getInfoString() {
		return (_item == null) ? "???" : _item.getName();
	}

	/**
	 * Set the line width for the shape. This does not cause a redraw.
	 * 
	 * @param lineWidth the line width in pixels [0..10]
	 */
	public void setLineWidth(int lineWidth) {
		// keep within reason
		lineWidth = Math.max(0, Math.min(10, lineWidth));
		_item.getStyle().setLineWidth(lineWidth);
	}

	/**
	 * Set the symbol type. Only relevant for PluginSymbol shapes. This does not
	 * cause a redraw.
	 * 
	 * @param symbol the symbol.
	 */
	public void setSymbol(SymbolType symbol) {
		if (_shapeType == PluginShapeType.SYMBOL) {
			_item.getStyle().setSymbolType(symbol);
		}
	}

	/**
	 * Set the symbol size. Only relevant for PluginSymbol shapes. This does not
	 * cause a redraw.
	 * 
	 * @param size the symbol size (width and height) in pixels.
	 */
	public void setSymbolSize(int size) {
		if (_shapeType == PluginShapeType.SYMBOL) {
			// keep reasonable
			size = Math.max(4, Math.min(40, size));
			_item.getStyle().setSymbolSize(size);
		}
	}

	/**
	 * Set the line style
	 * 
	 * @param lineStyle the style to set
	 * @see cnuphys.bCNU.graphics.style.LineStyle
	 */
	public void setLineStyle(LineStyle lineStyle) {
		_item.getStyle().setLineStyle(lineStyle);
	}

	/**
	 * Get the sector id. A bad value is indicated by Integer.MIN_VALUE (-2^31 =
	 * -2147483648)
	 * 
	 * @return the sector id.
	 */
	public int getSector() {
		return _sector;
	}

	/**
	 * Get the superlayer id. A bad value is indicated by Integer.MIN_VALUE
	 * (-2^31 = -2147483648)
	 * 
	 * @return the superlayer id.
	 */
	public int getSuperlayer() {
		return _superlayer;
	}

	/**
	 * Get the layer id. A bad value is indicated by Integer.MIN_VALUE (-2^31 =
	 * -2147483648)
	 * 
	 * @return the layer id.
	 */
	public int getLayer() {
		return _layer;
	}

	/**
	 * Get the component id. A bad value is indicated by Integer.MIN_VALUE
	 * (-2^31 = -2147483648)
	 * 
	 * @return the component id.
	 */
	public int getComponent() {
		return _component;
	}

	/**
	 * Get the crate id. A bad value is indicated by Integer.MIN_VALUE (-2^31 =
	 * -2147483648)
	 * 
	 * @return the crate id.
	 */
	public int getCrate() {
		return _crate;
	}

	/**
	 * Get the slot id. A bad value is indicated by Integer.MIN_VALUE (-2^31 =
	 * -2147483648)
	 * 
	 * @return the slot id.
	 */
	public int getSlot() {
		return _slot;
	}

	/**
	 * Get the channel id. A bad value is indicated by Integer.MIN_VALUE (-2^31
	 * = -2147483648)
	 * 
	 * @return the channel id.
	 */
	public int getChannel() {
		return _channel;
	}

	/**
	 * Get the user data. This is any object that the user wants to attach to
	 * the shape. return the user data (or <code>null</code> if none).
	 */
	public Object getUserData() {
		return _userData;
	}

	/**
	 * Get the user data. This is any object that the user wants to attach to
	 * the shape.
	 * 
	 * @param userData the new user data.
	 */
	public void setUserData(Object userData) {
		_userData = userData;
	}

}
