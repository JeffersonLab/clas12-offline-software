package cnuphys.bCNU.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;

public class PropertySupport {

	public static final String AZIMUTH = "AZIMUTH";
	public static final String BACKGROUND = "BACKGROUND";
	public static final String BACKGROUNDIMAGE = "BACKGROUNDIMAGE";
	public static final String BOTTOMMARGIN = "BOTTOMMARGIN";
	public static final String CHANNEL = "CHANNEL";
	public static final String CLOSABLE = "CLOSABLE";
	public static final String COMPONENT = "COMPONENT";
	public static final String CONTAINER = "CONTAINER";
	public static final String CRATE = "CRATE";
	public static final String DATADIR = "DATADIR";
	public static final String DRAGGABLE = "DRAGGABLE";
	public static final String FILLCOLOR = "FILLCOLOR";
	public static final String FONT = "FONT";
	public static final String FRACTION = "FRACTION";
	public static final String HEIGHT = "HEIGHT";
	public static final String ICONIFIABLE = "ICONIFIABLE";
	public static final String LAYER = "LAYER";
	public static final String LEFT = "LEFT";
	public static final String LEFTMARGIN = "LEFTMARGIN";
	public static final String LINECOLOR = "LINECOLOR";
	public static final String LINESTYLE = "LINESTYLE";
	public static final String LINEWIDTH = "LINEWIDTH";
	public static final String MAXIMIZE = "MAXIMIZE";
	public static final String MAXIMIZABLE = "MAXIMIZABLE";
	public static final String LOCKED = "LOCKED";
	public static final String RESIZABLE = "RESIZABLE";
	public static final String RIGHTMARGIN = "RIGHTMARGIN";
	public static final String ROTATABLE = "ROTATABLE";
	public static final String ROTATED = "ROTATED";
	public static final String SCROLLABLE = "SCROLLABLE";
	public static final String SECTOR = "SECTOR";
	public static final String SLOT = "SLOT";
	public static final String SPLITWESTCOMPONENT = "SPLITWESTCOMPONENT";
	public static final String STANDARDVIEWDECORATIONS = "STANDARDVIEWDECORATIONS";
	public static final String SUPERLAYER = "SUPERLAYER";
	public static final String SYMBOL = "SYMBOL";
	public static final String SYMBOLSIZE = "SYMBOLSIZE";
	public static final String TEXTCOLOR = "TEXTCOLOR";
	public static final String TITLE = "TITLE";
	public static final String TOOLBAR = "TOOLBAR";
	public static final String TOOLBARBITS = "TOOLBARBITS";
	public static final String TOP = "TOP";
	public static final String TOPMARGIN = "TOPMARGIN";
	public static final String USERDATA = "USERDATA";
	public static final String VISIBLE = "VISIBLE";
	public static final String VVPANEL = "VVPANEL";
	public static final String VVLOCATION = "VVLOCATION";
	public static final String WIDTH = "WIDTH";
	public static final String WORLDSYSTEM = "WORLDSYSTEM";

	// default fill color a gray
	public static Color defaultFillColor = new Color(208, 208, 208, 128);

	// default line color black
	public static Color defaultLineColor = Color.black;

	// default text color black
	public static Color defaultTextColor = Color.black;

	// default font
	public static final Font defaultFont = Fonts.commonFont(Font.PLAIN, 12);

	// default data directory
	public static String defaultDataDir = Environment.getInstance()
			.getCurrentWorkingDirectory() + File.separator + "data";

	// a default "unknown" string
	public static final String unknownString = "???";

	// a default world rectangle
	public static final Rectangle2D.Double defaultWorldRect = new Rectangle2D.Double(
			0, 0, 1, 1);

	/**
	 * Create a set of properties from the key values
	 * 
	 * @param keyValues the set of key values
	 * @return a set of properties
	 */
	public static Properties fromKeyValues(Object... keyValues) {
		if ((keyValues == null) || (keyValues.length < 2)) {
			return null;
		}

		int len = keyValues.length;
		Properties props = new Properties();

		for (int i = 0; i < (len - 1); i += 2) {
			Object key = keyValues[i];
			Object val = keyValues[i + 1];
			try {
				props.put(key, val);
			} catch (NullPointerException e) {
				System.err.println("null pointer exception");
				System.err.println("key: " + key);
				System.err.println("val: " + val);
				e.printStackTrace();
			}
		}

		return props;
	}

	/**
	 * Convert a properties object into an object array. This is the inverse of
	 * the fromKeyValues method.
	 * 
	 * @param props the properties
	 * @return the corresponding object array.
	 */
	public static Object[] toObjectArray(Properties props) {
		if ((props == null) || (props.isEmpty())) {
			return null;
		}

		int size = props.size();
		Object[] o = new Object[2 * size];

		int j = 0;
		for (Enumeration<Object> e = props.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = props.get(key);
			o[j] = key;
			o[j + 1] = value;
			j += 2;
		}
		return o;
	}

	/**
	 * Get an azimuth angle in degrees. Generally 0 is due north, 90 due east,
	 * etc.
	 * 
	 * @param props the properties
	 * @return the aximuth in degrees. On error, return 0.
	 */
	public static double getAzimuth(Properties props) {
		return getDouble(props, AZIMUTH, 0);
	}

	/**
	 * Get the full path of an image file to be used as a background mage.
	 * 
	 * @param props the properties
	 * @return the file path. On error return <code>null</code>.
	 */
	public static String getBackgroundImage(Properties props) {
		return getString(props, BACKGROUNDIMAGE, null);
	}

	/**
	 * Get the bottom margin in pixels.
	 * 
	 * @param props the properties
	 * @return the bottom margin. On error, return 0.
	 */
	public static int getBottomMargin(Properties props) {
		return getInt(props, BOTTOMMARGIN, 0);
	}

	/**
	 * Get the top margin in pixels.
	 * 
	 * @param props the properties
	 * @return the top margin. On error, return 0.
	 */
	public static int getTopMargin(Properties props) {
		return getInt(props, TOPMARGIN, 0);
	}

	/**
	 * Get the left margin in pixels.
	 * 
	 * @param props the properties
	 * @return the left margin. On error, return 0.
	 */
	public static int getLeftMargin(Properties props) {
		return getInt(props, LEFTMARGIN, 0);
	}

	/**
	 * Get the right margin in pixels.
	 * 
	 * @param props the properties
	 * @return the right margin. On error, return 0.
	 */
	public static int getRightMargin(Properties props) {
		return getInt(props, RIGHTMARGIN, 0);
	}

	/**
	 * Get the left location in pixels.
	 * 
	 * @param props the properties
	 * @return the left location. On error, return 0.
	 */
	public static int getLeft(Properties props) {
		return getInt(props, LEFT, 0);
	}

	/**
	 * Get the top location in pixels.
	 * 
	 * @param props the properties
	 * @return the top location. On error, return 0.
	 */
	public static int getTop(Properties props) {
		return getInt(props, TOP, 0);
	}

	/**
	 * Get the "is visible" boolean flag.
	 * 
	 * @param props the properties
	 * @return the visible flag. On error, return true.
	 */
	public static boolean getVisible(Properties props) {
		return getBoolean(props, VISIBLE, true);
	}
	
	/**
	 * Get the "scrollable" boolean flag.
	 * 
	 * @param props the properties
	 * @return the scrollable flag. On error, return false.
	 */
	public static boolean getScrollable(Properties props) {
		return getBoolean(props, SCROLLABLE, false);
	}


	/**
	 * Get the "use toolbar" boolean flag.
	 * 
	 * @param props the properties
	 * @return the toolbar flag. On error, return true.
	 */
	public static boolean getToolbar(Properties props) {
		return getBoolean(props, TOOLBAR, true);
	}

	/**
	 * Get the tool bar bits.
	 * 
	 * @param props the properties
	 * @return the toolbar bits. On error, return 0.
	 */
	public static int getToolbarBits(Properties props) {
		return getInt(props, TOOLBARBITS, 0);
	}

	/**
	 * Get the "use standard view decorations" boolean flag.
	 * 
	 * @param props the properties
	 * @return the decorations flag. On error, return true.
	 */
	public static boolean getStandardViewDecorations(Properties props) {
		return getBoolean(props, STANDARDVIEWDECORATIONS, true);
	}

	/**
	 * Get the closable boolean flag.
	 * 
	 * @param props the properties
	 * @return the closable flag. On error, return true.
	 */
	public static boolean getClosable(Properties props) {
		return getBoolean(props, CLOSABLE, true);
	}

	/**
	 * Get the draggable boolean flag.
	 * 
	 * @param props the properties
	 * @return the draggable flag. On error, return false.
	 */
	public static boolean getDraggable(Properties props) {
		return getBoolean(props, DRAGGABLE, false);
	}

	/**
	 * Get the view iconifiable boolean flag.
	 * 
	 * @param props the properties
	 * @return the iconifiable flag. On error, return true.
	 */
	public static boolean getIconifiable(Properties props) {
		return getBoolean(props, ICONIFIABLE, true);
	}

	/**
	 * Get the view maximize boolean flag. For views.
	 * 
	 * @param props the properties
	 * @return the maximize flag. On error, return false.
	 */
	public static boolean getMaximize(Properties props) {
		return getBoolean(props, MAXIMIZE, false);
	}

	/**
	 * Get the view maximizable boolean flag. For views.
	 * 
	 * @param props the properties
	 * @return the maximizable flag. On error, return true.
	 */
	public static boolean getMaximizable(Properties props) {
		return getBoolean(props, MAXIMIZABLE, true);
	}

	/**
	 * Get the view resizable boolean flag. For views.
	 * 
	 * @param props the properties
	 * @return the resizable flag. On error, return true.
	 */
	public static boolean getResizable(Properties props) {
		return getBoolean(props, RESIZABLE, true);
	}

	/**
	 * Get the item rotatable boolean flag. For views.
	 * 
	 * @param props the properties
	 * @return the rotatable flag. On error, return true.
	 */
	public static boolean getRotatable(Properties props) {
		return getBoolean(props, ROTATABLE, false);
	}

	/**
	 * Get a world coordinate system
	 * 
	 * @param props the properties
	 * @return a world rectangle. On error, return defaultWorldRect
	 */
	public static Rectangle2D.Double getWorldSystem(Properties props) {
		return getWorldRectangle(props, WORLDSYSTEM, defaultWorldRect);
	}

	/**
	 * Get a title
	 * 
	 * @param props the properties
	 * @return a title On error return unknownString.
	 */
	public static String getTitle(Properties props) {
		return getString(props, TITLE, unknownString);
	}

	/**
	 * Get the path of the data directory
	 * 
	 * @param props the properties
	 * @return the path of the data directory. On error return defaultDataDir.
	 */
	public static String getDataDir(Properties props) {
		return getString(props, DATADIR, defaultDataDir);
	}

	/**
	 * Get a container from the properties
	 * 
	 * @param props the properties
	 * @return an IContainer, on error return null
	 */
	public static IContainer getContainer(Properties props) {
		Object val = props.get(CONTAINER);
		if ((val == null) || !(val instanceof IContainer)) {
			return null;
		}
		return (IContainer) val;
	}

	/**
	 * Get a (screen) fraction from the properties
	 * 
	 * @param props the properties
	 * @return a (screen) fraction, on error return Double.NaN.
	 */
	public static double getFraction(Properties props) {
		return getDouble(props, FRACTION, Double.NaN);
	}

	/**
	 * Get an optional component used to split the west part of a view
	 * 
	 * @param props the properties
	 * @return the component
	 */
	public static JComponent getSplitWestComponent(Properties props) {
		return getJComponent(props, SPLITWESTCOMPONENT);
	}

	/**
	 * Get a font from the properties
	 * 
	 * @param props the properties
	 * @return a Font, on error return _defaultFont
	 */
	public static Font getFont(Properties props) {
		Object val = props.get(FONT);
		if ((val == null) || !(val instanceof Font)) {
			return defaultFont;
		}
		return (Font) val;
	}

	/**
	 * Get a symbol from the properties
	 * 
	 * @param props the properties
	 * @return a SymbolType, on error return SymbolType.SQUARE
	 */
	public static SymbolType getSymbol(Properties props) {
		Object val = props.get(SYMBOL);
		if ((val == null) || !(val instanceof SymbolType)) {
			return SymbolType.SQUARE;
		}
		return (SymbolType) val;
	}

	/**
	 * Get the symbol size in pixels
	 * 
	 * @param props the properties
	 * @return get the symbol size (width and height) in pixels. On error return
	 *         8.
	 */
	public static int getSymbolSize(Properties props) {
		int size = getInt(props, SYMBOLSIZE, 8);
		return size;
	}

	/**
	 * Get the optional user data. This is any object that the user wants to
	 * attach to the shape.
	 * 
	 * @param props the properties
	 * @return the user data (might be <code>null</code>).
	 */
	public static Object getUserData(Properties props) {
		return props.get(USERDATA);
	}

	/**
	 * Get the sector id.
	 * 
	 * @param props the properties
	 * @return the sector Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getSector(Properties props) {
		return getInt(props, SECTOR, Integer.MIN_VALUE);
	}

	/**
	 * Get the superlayer id.
	 * 
	 * @param props the properties
	 * @return the suplerlayer Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getSuperlayer(Properties props) {
		return getInt(props, SUPERLAYER, Integer.MIN_VALUE);
	}

	/**
	 * Get the layer id.
	 * 
	 * @param props the properties
	 * @return the layer Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getLayer(Properties props) {
		return getInt(props, LAYER, Integer.MIN_VALUE);
	}

	/**
	 * Get the component id.
	 * 
	 * @param props the properties
	 * @return the component Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getComponent(Properties props) {
		return getInt(props, COMPONENT, Integer.MIN_VALUE);
	}

	/**
	 * Get the crate id.
	 * 
	 * @param props the properties
	 * @return the crate Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getCrate(Properties props) {
		return getInt(props, CRATE, Integer.MIN_VALUE);
	}

	/**
	 * Get the slot id.
	 * 
	 * @param props the properties
	 * @return the slot Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getSlot(Properties props) {
		return getInt(props, SLOT, Integer.MIN_VALUE);
	}

	/**
	 * Get the channelid.
	 * 
	 * @param props the properties
	 * @return the channel Id. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getChannel(Properties props) {
		return getInt(props, CHANNEL, Integer.MIN_VALUE);
	}

	/**
	 * Get the background color from the properties
	 * 
	 * @param props the properties
	 * @return the background color. On error return null.
	 */
	public static Color getBackground(Properties props) {
		return getColor(props, BACKGROUND, null);
	}

	/**
	 * Get the text color from the properties
	 * 
	 * @param props the properties
	 * @return the text color. On error return _defaultTextColor.
	 */
	public static Color getTextColor(Properties props) {
		return getColor(props, TEXTCOLOR, defaultTextColor);
	}

	/**
	 * Get the rotation angle assumed to be in degrees.
	 * 
	 * @param props the properties
	 * @return the rotation angle assumed to be in degrees. On error return 0.
	 */
	public static double getRotationAngle(Properties props) {
		return getDouble(props, ROTATED, 0);
	}

	/**
	 * Get the fill color from the properties
	 * 
	 * @param props the properties
	 * @return the fill color. On error return _defaultFillColor.
	 */
	public static Color getFillColor(Properties props) {
		return getColor(props, FILLCOLOR, defaultFillColor);
	}

	/**
	 * Get the line color from the properties
	 * 
	 * @param props the properties
	 * @return the line color. On error return _defaultLineColor.
	 */
	public static Color getLineColor(Properties props) {
		return getColor(props, LINECOLOR, defaultLineColor);
	}

	/**
	 * Get the line style from the properties.
	 * 
	 * @param props the properties.
	 * @return the linestyle. On error return LineStyle.SOLID.
	 */
	public static LineStyle getLineStyle(Properties props) {
		LineStyle lineStyle = LineStyle.SOLID;
		Object val = props.get(LINESTYLE);

		if ((val != null) && (val instanceof LineStyle)) {
			return (LineStyle) val;
		}
		return lineStyle;
	}

	/**
	 * Get a line width from the properties
	 * 
	 * @param props the properties
	 * @return the width. On error return 0.
	 */
	public static int getLineWidth(Properties props) {
		return getInt(props, LINEWIDTH, 0);
	}

	/**
	 * Get a width from the properties
	 * 
	 * @param props the properties
	 * @return the width. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getWidth(Properties props) {
		return getInt(props, WIDTH, Integer.MIN_VALUE);
	}

	/**
	 * Get a height from the properties
	 * 
	 * @param props the properties
	 * @return the height. On error return Integer.MIN_VALUE (-2^31 =
	 *         -2147483648)
	 */
	public static int getHeight(Properties props) {
		return getInt(props, HEIGHT, Integer.MIN_VALUE);
	}

	/**
	 * Get the virtual view panel from the properties
	 * 
	 * @param props the properties
	 * @return the virtual view panel. On error return Integer.MIN_VALUE (-2^31
	 *         = -2147483648)
	 */
	public static int getVVPanel(Properties props) {
		return getInt(props, VVPANEL, Integer.MIN_VALUE);
	}

	/**
	 * Get the locked flag. (Default is true)
	 * 
	 * @param props the properties
	 * @return the locked flag
	 */
	public static boolean getLocked(Properties props) {
		return getBoolean(props, LOCKED, true);
	}

	/**
	 * Get a world rectangle
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the world rectangle, or defaultValue upon failure.
	 */
	public static Rectangle2D.Double getWorldRectangle(Properties props,
			String key, Rectangle2D.Double defaultValue) {
		Object val = props.get(key);
		if (val == null) {
			return defaultValue;
		}

		if (val instanceof Rectangle2D.Double) {
			return (Rectangle2D.Double) val;
		}
		return null;
	}

	/**
	 * Get a color from a properties. Tries to handle both a String (from the
	 * X11 database, e.g. "coral", "red", "powder blue") and a Java Color value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the color, or defaultValue upon failure.
	 */
	public static Color getColor(Properties props, String key,
			Color defaultValue) {
		Object val = props.get(key);
		if (val == null) {
			return defaultValue;
		}

		if (val instanceof String) {
			return X11Colors.getX11Color((String) val);
		}

		if (val instanceof Color) {
			return (Color) val;
		}
		return defaultValue;
	}

	/**
	 * Get a JComponent from properties.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the JComponent value, or on error <code>null</code>.
	 */
	public static JComponent getJComponent(Properties props, String key) {
		Object val = props.get(key);
		if ((val != null) && (val instanceof JComponent)) {
			return (JComponent) val;
		}
		return null;
	}

	/**
	 * Get a String from properties.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the String value, or on error the default value.
	 */
	public static String getString(Properties props, String key,
			String defaultValue) {
		Object val = props.get(key);
		if (val == null) {
			return defaultValue;
		}

		if (val instanceof String) {
			return (String) val;
		}
		return defaultValue;
	}

	/**
	 * Get an int from properties. Tries to handle both a String (e.g., "67")
	 * and Integer value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the integer value, or on error the default value.
	 */
	public static int getInt(Properties props, String key, int defaultValue) {
		Object val = props.get(key);
		if (val == null) {
			return defaultValue;
		}

		if (val instanceof String) {
			try {
				return Integer.parseInt((String) val);
			} catch (Exception e) {
				return defaultValue;
			}

		}

		if (val instanceof Integer) {
			return (Integer) val;
		}
		return defaultValue;

	}

	/**
	 * Get a double from properties. Tries to handle both a String (e.g.,
	 * "67.0") and Double value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @return the double value, or on error return Double.NaN.
	 */
	public static double getDouble(Properties props, String key) {
		return getDouble(props, key, Double.NaN);
	}

	/**
	 * Get a double from properties. Tries to handle both a String (e.g.,
	 * "67.0") and Double value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the double value, or on error return defaultValue
	 */
	public static double getDouble(Properties props, String key,
			double defaultValue) {
		Object val = props.get(key);
		if (val == null) {
			return defaultValue;
		}

		if (val instanceof String) {
			try {
				return Double.parseDouble((String) val);
			} catch (Exception e) {
				return defaultValue;
			}

		}

		if (val instanceof Double) {
			return (Double) val;
		}

		if (val instanceof Float) {
			return ((Float) val);
		}

		if (val instanceof Integer) {
			return ((Integer) val);
		}

		return defaultValue;
	}

	/**
	 * Get a float from properties. Tries to handle both a String (e.g., "67.0")
	 * and Float value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @return the double value, or on error return Float.NaN.
	 */
	public static double getFloat(Properties props, String key) {
		return getFloat(props, key, Float.NaN);
	}

	/**
	 * Get a float from properties. Tries to handle both a String (e.g., "67.0")
	 * and Float value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the double value, or on error return defaultValue
	 */
	public static double getFloat(Properties props, String key,
			double defaultValue) {
		Object val = props.get(key);
		if (val == null) {
			return defaultValue;
		}

		if (val instanceof String) {
			try {
				return Float.parseFloat((String) val);
			} catch (Exception e) {
				return defaultValue;
			}

		}

		if (val instanceof Float) {
			return (Float) val;
		}

		if (val instanceof Integer) {
			return ((Integer) val);
		}

		return defaultValue;

	}

	/**
	 * Get a boolean from properties. Tries to handle both a String (e.g.,
	 * "true") and Boolean value.
	 * 
	 * @param props the properties
	 * @param key the key
	 * @param boolean defaultVal
	 * @return the boolean value, or on error the default
	 */
	public static boolean getBoolean(Properties props, String key,
			boolean defaultVal) {
		Object val = props.get(key);
		if (val == null) {
			return defaultVal;
		}

		if (val instanceof String) {
			try {
				return Boolean.parseBoolean((String) val);
			} catch (Exception e) {
				return defaultVal;
			}

		}

		if (val instanceof Boolean) {
			return (Boolean) val;
		}
		return defaultVal;

	}

}
