package cnuphys.bCNU.plugin;

import java.awt.Color;
import java.util.Properties;

import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.util.X11Colors;

public class PluginProperties {

    public static final String BACKGROUND = "BACKGROUND";
    public static final String FILLCOLOR = "FILLCOLOR";
    public static final String HEIGHT = "HEIGHT";
    public static final String LINECOLOR = "LINECOLOR";
    public static final String LINESTYLE = "LINESTYLE";
    public static final String LINEWIDTH = "LINEWIDTH";
    public static final String LOCKED = "LOCKED";
    public static final String ROTATED = "ROTATED";
    public static final String SYMBOL = "SYMBOL";
    public static final String SYMBOLSIZE = "SYMBOLSIZE";
    public static final String WIDTH = "WIDTH";
    
    //related to virtual view placement
    public static final String VVPANEL = "VVPANEL";
    public static final String VVLOCATION = "VVLOCATION";
    
    //more properties for Data
    public static final String SECTOR = "SECTOR";
    public static final String SUPERLAYER = "SUPERLAYER";
    public static final String LAYER = "LAYER";
    public static final String COMPONENT = "COMPONENT";
    public static final String CRATE = "CRATE";
    public static final String SLOT = "SLOT";
    public static final String CHANNEL = "CHANNEL";
    public static final String USERDATA = "USERDATA";

    /**
     * Get a symbol from the properties
     * @param props the properties
     * @return a SymbolType, on error return SymbolType.SQUARE
     */
    public static SymbolType getSymbol(Properties props) {
	Object val = props.get(SYMBOL);
	if ((val == null) || !(val instanceof SymbolType)) {
	    return SymbolType.SQUARE;
	}
	return (SymbolType)val;
    }
    
    /**
     * Get the symbol size in pixels
     * @param props the properties
     * @return get the symbol size (width and height) in pixels. On error return 8.
     */
    public static int getSymbolSize(Properties props) {
	int size = getInt(props, SYMBOLSIZE, 8);
	return size;
    }
    
    /**
     * Get the optional user data. This is any object that the user wants
     * to attach to the shape.
     * @param props the properties
     * @return the user data (might be <code>null</code>.
     */
    public static Object getUserData(Properties props) {
	return props.get(USERDATA);
    }
    
    /**
     * Get the sector id.
     * @param props the properties
     * @return the sector Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getSector(Properties props) {
	return getInt(props, SECTOR, Integer.MIN_VALUE);
    }
    
    /**
     * Get the superlayer id.
     * @param props the properties
     * @return the suplerlayer Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getSuperlayer(Properties props) {
	return getInt(props, SUPERLAYER, Integer.MIN_VALUE);
    }
    
    /**
     * Get the layer id.
     * @param props the properties
     * @return the layer Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getLayer(Properties props) {
	return getInt(props, LAYER, Integer.MIN_VALUE);
    }
    
    /**
     * Get the component id.
     * @param props the properties
     * @return the component Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getComponent(Properties props) {
	return getInt(props, COMPONENT, Integer.MIN_VALUE);
    }
    
    /**
     * Get the crate id.
     * @param props the properties
     * @return the crate Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getCrate(Properties props) {
	return getInt(props, CRATE, Integer.MIN_VALUE);
    }
    
    /**
     * Get the slot id.
     * @param props the properties
     * @return the slot Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getSlot(Properties props) {
	return getInt(props, SLOT, Integer.MIN_VALUE);
    }
    
    /**
     * Get the channelid.
     * @param props the properties
     * @return the channel Id. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
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
	return getColor(props, BACKGROUND);
    }
    
    /**
     * Get the rotation angle assumed to be in degrees.
     * @param props the properties
     * @return the rotation angle assumed to be in degrees, 0 on error
     */
    public static double getRotationAngle(Properties props) {
	return getDouble(props, ROTATED, 0);
    }
    
    /**
     * Get the fill color from the properties
     * @param props the properties
     * @return the fill color. On error return null.
     */
    public static Color getFillColor(Properties props) {
	return getColor(props, FILLCOLOR);
    }
    
    /**
     * Get the line color from the properties
     * @param props the properties
     * @return the line color. On error return null.
     */
    public static Color getLineColor(Properties props) {
	return getColor(props, LINECOLOR);
    }
    
    /**
     * Get the line style from the properties.
     * @param props the properties.
     * @return the linestyle, LineStyle.SOLID on error.
     */
    public static LineStyle getLineStyle(Properties props) {
	LineStyle lineStyle = LineStyle.SOLID;
	Object val = props.get(LINESTYLE);

	if ((val != null) && (val instanceof LineStyle)) {
	    return (LineStyle)val;
	}
	return lineStyle;
    }

    /**
     * Get a line width from the properties
     * @param props the properties
     * @return the width. On error return 0.
     */
    public static int getLineWidth(Properties props) {
	return getInt(props, LINEWIDTH, 0);
    }
   
    /**
     * Get a width from the properties
     * @param props the properties
     * @return the width. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getWidth(Properties props) {
	return getInt(props, WIDTH, Integer.MIN_VALUE);
    }

    /**
     * Get a height from the properties
     * @param props the properties
     * @return the height. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getHeight(Properties props) {
	return getInt(props, HEIGHT, Integer.MIN_VALUE);
    }
    
    /**
     * Get the virtual view panel from the properties
     * @param props the properties
     * @return the virtual view panel. On error return Integer.MIN_VALUE (-2^31 = -2147483648)
     */
    public static int getVVPanel(Properties props) {
	return getInt(props, VVPANEL, Integer.MIN_VALUE);
    }
    
    /**
     * Get a virtual view placement location from the properties. Only 
     * meaningful values are the constants defined in VirtualView.
     * @param props the properties
     * @return the virtual view location. On error return VirtualView.CENTER
     */
    public static int getVVLocation(Properties props) {
	return getInt(props, VVLOCATION, Plugin.CENTER);
    }
    
    /**
     * Get the locked flag. (Default is true)
     * @param props the properties
     * @return the locked flag
     */
    public static boolean getLocked(Properties props) {
	return getBoolean(props, LOCKED, true);
    }
    
    /**
     * Get a color from a properties. Tries to handle both a String (from the
     * X11 database, e.g. "coral", "red", "powder blue") and a Java Color value.
     * @param props the properties
     * @param key the key
     * @return the color, or null upon failure.
     */
    public static Color getColor(Properties props, String key) {
	Object val = props.get(key);
	if (val == null) {
	    return null;
	}
	
	if (val instanceof String) {
	    return X11Colors.getX11Color((String)val);
	}
	
	if (val instanceof Color) {
	    return (Color)val;
	}
	return null;
    }
    
    /**
     * Get an int from properties. Tries to handle both a String (e.g., "67") and 
     * Integer value.
     * @param props the properties
     * @param key the key
     * @return the double value, or on error return defaultValue  
     * @return the integer value, or on error the default value). 
     */
    public static int getInt(Properties props, String key, int defaultValue) {
	Object val = props.get(key);
	if (val == null) {
	    return defaultValue;
	}
	
	if (val instanceof String) {
	    try {
		return Integer.parseInt((String)val);
	    }
	    catch (Exception e) {
		return defaultValue;
	    }
	    
	}
	
	if (val instanceof Integer) {
	    return (Integer)val;
	}
	return defaultValue;
	
    }
    
    /**
     * Get a double from properties. Tries to handle both a String (e.g., "67.0") and 
     * Double value.
     * @param props the properties
     * @param key the key
     * @param defaultValue the default value
     * @return the double value, or on error return defaultValue  
     */
    public static double getDouble(Properties props, String key, double defaultValue) {
	Object val = props.get(key);
	if (val == null) {
	    return defaultValue;
	}
	
	if (val instanceof String) {
	    try {
		return Double.parseDouble((String)val);
	    }
	    catch (Exception e) {
		return defaultValue;
	    }
	    
	}
	
	if (val instanceof Double) {
	    return (Double)val;
	}

	
	if (val instanceof Float) {
	    return ((Float)val);
	}
	
	if (val instanceof Integer) {
	    return ((Integer)val);
	}

	return defaultValue;
	
    }
    
    /**
     * Get a boolean from properties. Tries to handle both a String (e.g., "true") and 
     * Boolean value.
     * @param props the properties
     * @param key the key
     * @param boolean defaultVal
     * @return the boolean value, or on error the default  
     */
    public static boolean getBoolean(Properties props, String key, boolean defaultVal) {
	Object val = props.get(key);
	if (val == null) {
	    return defaultVal;
	}
	
	if (val instanceof String) {
	    try {
		return Boolean.parseBoolean((String)val);
	    }
	    catch (Exception e) {
		return defaultVal;
	    }
	    
	}
	
	if (val instanceof Boolean) {
	    return (Boolean)val;
	}
	return defaultVal;
	
    }
}
