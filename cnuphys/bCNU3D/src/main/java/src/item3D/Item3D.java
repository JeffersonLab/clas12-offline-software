package item3D;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jogamp.opengl.GLAutoDrawable;
import bCNU3D.Bad3DPropertyException;
import bCNU3D.Panel3D;

public abstract class Item3D {
    
    /** property for line width. Default is 1f */
    public static final String LINE_WIDTH = "LINE_WIDTH";

    /** property for color. Default is defined below */
    public static final String COLOR = "COLOR";
    
    /** property for text color. Default is defined below */
    public static final String TEXT_COLOR = "TEXT_COLOR";
    
    /** property string for font. Default is defined below */
    public static final String FONT = "FONT";

    //this item's properties
    private Properties _properties;
    
    //controls whether the item is visible 
    // only in the sense do we "want" to draw it
    private boolean _visible = true;
    
    //this item's child items
    private Vector<Item3D> _children;
    
    //this item's parent
    private Item3D _parent;
        
    //the owner panel
    protected Panel3D _panel3D;
    
    //defaults
    private static final float _defaultLineWidth = 1f;
    private static final Color _defaultColor = Color.yellow;
    private static final Color _defaultTextColor = Color.white;
    private static final Font _defaultFont = new Font("SansSerif", Font.PLAIN, 12);
    
    /**
     * Create a 3D item for use on a Panel3D.
     * @param panel3D the owner panel 3D object
     */
    public Item3D(Panel3D panel3D) {
	_panel3D = panel3D;
	defaultProperties();
    }

    /**
     * Set the default properties
     */
    protected void defaultProperties() {
	_properties = new Properties();
	_properties.put(LINE_WIDTH, 1f);
	_properties.put(COLOR, Color.yellow);
	_properties.put(TEXT_COLOR, Color.white);
    }
    
    /**
     * set a property
     * @param key the key
     * @param value the value
     */
    public void put(Object key, Object value) {
	_properties.put(key, value);
    }
    
    /**
     * Called by the panel3D. Do not overrwrite.
     * 
     * @param drawable the OpenGL drawable
     */
    public final void drawItem(GLAutoDrawable drawable) {
	draw(drawable);

	if (_children != null) {
	    for (Item3D item : _children) {
		if (item.isVisible()) {
		    item.drawItem(drawable);
		}
	    }
	}
    }
    
    /**
     * The custom drawing method
     * @param drawable the OpenGL drawable
     */
    public abstract void draw(GLAutoDrawable drawable);
    
    /**
     * Get a 3D property
     * @param key the name of the property
     * @return the object corresponding to that key, or null
     * @throws Bad3DPropertyException
     */
    public Object get(String key) throws Bad3DPropertyException {
	Object obj = _properties.get(key);
	if (obj == null) {
	    throw new Bad3DPropertyException("Item has no property named: " + key);
	}
	return obj;
    }
    
    /**
     * Get a String 3D property
     * @param key the name of the property
     * @return the String corresponding to that key, or null
     * @throws Bad3DPropertyException
     */
   public String getString(String key) throws Bad3DPropertyException {
	Object obj = get(key);
	
	if (obj instanceof String) {
	    return (String) obj;
	}
	else {
	    throw new Bad3DPropertyException("Property named: " + key +
	    " is not of type String");
	}
    }
    
   /**
    * Get an AWT color 3D property
    * @param key the name of the property
    * @return the (AWT) Color corresponding to that key, or null
    * @throws Bad3DPropertyException
    */
  public Color getColor(String key) throws Bad3DPropertyException {
	Object obj = get(key);
	
	if (obj instanceof Color) {
	    return (Color) obj;
	}
	else {
	    throw new Bad3DPropertyException("Property named: " + key +
	    " is not of type Color");
	}
   }
  
  /**
   * Get an a Font 3D property
   * @param key the name of the property
   * @return the Font corresponding to that key, or null
   * @throws Bad3DPropertyException
   */
 public Font getFont(String key) throws Bad3DPropertyException {
	Object obj = get(key);
	
	if (obj instanceof Font) {
	    return (Font) obj;
	}
	else {
	    throw new Bad3DPropertyException("Property named: " + key +
	    " is not of type Font");
	}
  }
 
  
  
  /**
   * Get a integer 3D property
   * @param key the name of the property
   * @return the Integer corresponding to that key, or null
   * @throws Bad3DPropertyException
   */
 public int getInt(String key) throws Bad3DPropertyException {
	Object obj = get(key);
	
	if (obj instanceof Integer) {
	    return (Integer) obj;
	}
	else {
	    throw new Bad3DPropertyException("Property named: " + key +
	    " is not of type Integer");
	}
  }
  
  
  /**
   * Get a float 3D property
   * @param key the name of the property
   * @return the Float corresponding to that key, or null
   * @throws Bad3DPropertyException
   */
 public float getFloat(String key) throws Bad3DPropertyException {
	Object obj = get(key);
	
	if (obj instanceof Float) {
	    return (Float) obj;
	}
	else {
	    throw new Bad3DPropertyException("Property named: " + key +
	    " is not of type Float");
	}
  }

    /**
     * Convenience method to get the line width for this item
     * 
     * @return the line width. (Default is 1f) 
     */
    public float getLineWidth() {
	try {
	    return getFloat(LINE_WIDTH);
	} catch (Bad3DPropertyException e) {
	    return _defaultLineWidth;
	}
    }
    
    /**
     * Convenience method to set the line width
     * @param lineWidth the line width.
     */
    public void setLineWidth(float lineWidth) {
	put(LINE_WIDTH, lineWidth);
    }
    
    /**
     * Convenience method to get the color for this item
     * 
     * @return the color.
     */
    public Color getColor() {
	try {
	    return getColor(COLOR);
	} catch (Bad3DPropertyException e) {
	    return _defaultColor;
	}
    }
    
    /**
     * Convenience method to get the text color for this item
     * 
     * @return the text color.
     */
    public Color getTextColor() {
	try {
	    return getColor(TEXT_COLOR);
	} catch (Bad3DPropertyException e) {
	    return _defaultTextColor;
	}
    }
    
    /**
     * Convenience method to get the fontr for this item
     * 
     * @return the font.
     */
    public Font getFont() {
	try {
	    return getFont(FONT);
	} catch (Bad3DPropertyException e) {
	    return _defaultFont;
	}	
    }
    
    /**
     * Convenience method to set the color
     * @param color the color.
     */
    public void setColor(Color color) {
	put(COLOR, color);
    }
    
    /**
     * Convenience method to set the text color
     * @param textColor the text color.
     */
    public void setTextColor(Color textColor) {
	put(TEXT_COLOR, textColor);
    }
    
    /**
     * Convenience method to set the font
     * @param font the text font.
     */
    public void setFont(Font font) {
	put(FONT, font);
    }
    

   /**
     * Controls whether we want the item to be drawn. That is,
     * drawing is skipped if this is false. Whether or not the 
     * item is actually visible when drawn is not controlled by
     * this flag. This does NOT initiate a redraw,
     * @param visible the visibility flag.
     */
    public void setVisible(boolean visible) {
	_visible = visible;
    }
    
    /**
     * Checks whether we want the item to be drawn. That is,
     * drawing is skipped if this is false. Whether or not the 
     * item is actually visible when drawn is not controlled by
     * this flag. 
     * @return the visibility flag.
     */
    public boolean isVisible() {
	return _visible;
    }

    /**
     * Get a list of the item's children, can be <code>null</code>
     * @return a list of the item's children
     */
    public List<Item3D> getChildren() {
	return _children;
    }
    
    /**
     * Get the parent item (might be null)
     * @return the parent
     */
    public Item3D getParent() {
	return _parent;
    }

    
    /**
     * Add a child item
     * @param item the child item
     */
    public void addChild(Item3D item) {
	if (item != null) {
	    if (_children == null) {
		_children = new Vector<Item3D>();
	    }
	    _children.remove(item);
	    _children.addElement(item);
	    item._parent = this;
	}
    }
    
    /**
     * Remove a child item
     * @param item the chold item
     */
    public void removeChild(Item3D item) {
	if ((_children != null) && (item != null)) {
	    _children.remove(item);
	}
    }

    /**
     * Get the parent Panel3D object
     * @return the parent Panel3D object
     */
    public Panel3D getPanel3D() {
	return _panel3D;
    }

}
