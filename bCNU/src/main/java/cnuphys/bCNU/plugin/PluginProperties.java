package cnuphys.bCNU.plugin;

import java.awt.Color;
import java.util.Properties;

import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.util.X11Colors;

public class PluginProperties {

    public static final String BACKGROUND = "BACKGROUND";
    public static final String FILLCOLOR = "FILLCOLOR";
    public static final String HEIGHT = "HEIGHT";
    public static final String LINECOLOR = "LINECOLOR";
    public static final String LINESTYLE = "LINESTYLE";
    public static final String LINEWIDTH = "LINEWIDTH";
    public static final String LOCKED = "LOCKED";
    public static final String WIDTH = "WIDTH";

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
