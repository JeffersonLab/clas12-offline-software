package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;

/**
 * The Attributes class is used to store variable length arguments for creation
 * of applications, internal frames (views), drawables, etc. Note that you can
 * use either the predefined attribute types in AttributeType or create you own
 * as long as the names are unique.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class Attributes extends Hashtable<String, Object> {

    public static final Color NULLCOLOR = new Color(254, 253, 252, 0);

    /**
     * Create an empty Attributes object.
     */
    public Attributes() {
	this(false);
	// super(193);
    }

    public Attributes(boolean loadStandardDefaults) {
	super(193);
	if (loadStandardDefaults) {
	    AttributeType.setDefaults(this);
	}

    }

    /**
     * Creates an attributes object (and loads standard defaults) generally from
     * an object array that is a variable length argument list to a method. E.g,
     * <p>
     * <code>public void yomama(param1, param2, Object... objects)</code>
     * <p>
     * There should be an even number of objects and they are taken in pairs.
     * The first typically matches one of the AttributeType enum types, but it
     * can be a user defined string. The second is the value that will override
     * the default. For example: <code>
     * 		Diddy.yomama(Param1, param2,(<br>
     *          AttributeType.NAME,         "My Application",<br> 
     *          AttributeType.CENTER,       true,<br>
     * 			AttributeType.FRACTION,     0.95, <br>
     *          MyEnum.WHATEVER,            Color.red, <br>
     * 		    "My Int Attribute",         8, <br>
     *          "My Boolean Attribute",     true, <br>
     * 		    AttributeType.TILE,         true,  <br>
     *          AttributeType.TILESTRING,  "This is a two line\ntile string");<br>
     * </code> In the method <code>yomama</code> we create an
     * <code>Attributes</code> object:
     * <p>
     * <code>Attributes attributes = new Attributes(objects);
     * <p>
     * We then access the values via:
     * <p><code>
     * String tileString = attributes.stringValue(AttributeType.TILESTRING); <br>
     * int myint = attributes.intValue("My Int Attribute"); <br>
     * etc.
     * </code>
     * <p>
     * 
     * @param keyVals
     */
    public Attributes(Object... keyVals) {
	this(true, keyVals);
    }

    /**
     * Create an Attributes from a JAVA properties object. JAVA properties and
     * Attributes are very similar. Converting a properties to an Attributes
     * object allows you to edit properties in the Attribute Editor.
     * 
     * @param properties
     *            the JAVA properties object.
     */
    public Attributes(Properties properties) {
	super(2 * properties.size() + 1);

	Enumeration<?> e = properties.keys();
	while (e.hasMoreElements()) {
	    add(e.nextElement(), properties.get(e));
	}
    }

    /**
     * Creates a Attributes object.
     * 
     * @param loadStandardDefaults
     *            if <code>true</code> the standard defaults for the enum values
     *            in AttributeType are loaded.
     * @param keyVals
     *            the variable length list of attributes.
     */
    public Attributes(boolean loadStandardDefaults, Object... keyVals) {
	super(193);
	// the only think special about AttributeType is defaults are set for
	// those enum values.

	if (loadStandardDefaults) {
	    AttributeType.setDefaults(this);
	}
	stuffAttributes(keyVals);
    }

    /**
     * Stuff the attributes from the input array. This is an array of key-value
     * pairs, therefore is should have an even number of entries.
     * 
     * @param attributes
     *            the input array.
     */
    public void stuffAttributes(Object[] attributes) {
	if (attributes == null) {
	    return;
	}

	int length = attributes.length;

	// should be even, they come in name value pairs

	if ((length % 2) != 0) {
	    System.err.println("Odd number of attributes.");
	}

	for (int i = 0; i < (length - 1); i = i + 2) {
	    if (attributes[i + 1] != null) {
		put(attributes[i].toString(), attributes[i + 1]);
	    }
	}

    }

    /**
     * Puts a value based on a name object. Note a user attribute can also be
     * added just by <code>put(String, object)</code>.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @param value
     *            the value to add to the hashtable.
     */
    public void add(Object nameObject, Object value) {
	if (value != null) {
	    super.put(nameObject.toString(), value);
	}
    }

    /**
     * Removes a key from the hashtable
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the obeject removed, or <code>null</code>.
     */
    @Override
    public Object remove(Object nameObject) {
	if (nameObject == null) {
	    return null;
	}
	return super.remove(nameObject.toString());
    }

    /**
     * Get the attribute object (value) for a given attribute type. Note for
     * user defined attributes, the normal get inherited from Hashtable will
     * work just fine, e.g., call
     * <code>attributes.get("My Special Attribute");</code> Of course, user
     * definined enums can also be used as keys.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the corresponding object (value).
     */
    @Override
    public Object get(Object nameObject) {
	return super.get(nameObject.toString());
    }

    /**
     * Checks whether a key is in the Attributes object.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return <code>true</code> if the key is in the Attributes object.
     */
    @Override
    public boolean containsKey(Object nameObject) {
	return super.containsKey(nameObject.toString());
    }

    /**
     * Gets a world rectangle from the attribute. If the attribute does not
     * exist, it returns null. If the attribute is not really a
     * Rectangle2D.Double, it returns null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the Color value of the attribute.
     */
    public Rectangle2D.Double worldRectangleValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof Rectangle2D.Double) {
	    return (Rectangle2D.Double) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets a container from the attribute. If the attribute does not exist, it
     * returns null. If the attribute is not really an IContainer, it returns
     * null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the Color value of the attribute.
     */
    public IContainer containerValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof IContainer) {
	    return (IContainer) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the color value of the attribute. If the attribute does not exist,
     * it returns null. If the attribute is not really a color, it returns null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the Color value of the attribute.
     */
    public Color colorValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof Color) {
	    if (NULLCOLOR.equals(object)) {
		return null;
	    }
	    return (Color) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the UUID value of the attribute. If the attribute does not exist, it
     * returns null. If the attribute is not really a UUID, it returns null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the UUID value of the attribute.
     */
    public UUID UuidValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof UUID) {
	    return (UUID) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the line style value of the attribute. If the attribute does not
     * exist, it returns null. If the attribute is not really a line style, it
     * returns null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the LineStyle value of the attribute.
     */
    public LineStyle lineStyleValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof LineStyle) {
	    return (LineStyle) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the symbol type value of the attribute. If the attribute does not
     * exist, it returns null. If the attribute is not really a symbol type, it
     * returns null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the SymbolType value of the attribute.
     */
    public SymbolType symbolTypeValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof SymbolType) {
	    return (SymbolType) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the font value of the attribute. If the attribute does not exist, it
     * returns null. If the attribute is not really a font, it returns null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the Font value of the attribute.
     */
    public Font fontValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof Font) {
	    return (Font) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the string value of the attribute. If the attribute does not exist,
     * it returns null. If the attribute is not really a string, it returns
     * null.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the String value of the attribute.
     */
    public String stringValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof String) {
	    return (String) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the double value of the attribute. If the attribute does not exist,
     * it returns NaN. If the attribute is not really a double, it returns NaN.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the double value of the attribute.
     */
    public double doubleValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return Double.NaN;
	}

	if (object instanceof Double) {
	    return ((Double) object).doubleValue();
	} else {
	    return Double.NaN;
	}
    }

    /**
     * Gets the JComponent value of the attribute. If the attribute does not
     * exist, it returns <code>null</code>. If the attribute is not really a
     * JComponent, it returns <code>null</code>.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the integer value of the attribute.
     */
    public JComponent componentValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return null;
	}

	if (object instanceof JComponent) {
	    return ((JComponent) object);
	} else {
	    return null;
	}
    }

    /**
     * Gets the integer value of the attribute. If the attribute does not exist,
     * it returns the smallest possible value. If the attribute is not really an
     * integer, it returns the smallest possible value.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the integer value of the attribute.
     */
    public int intValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return Integer.MIN_VALUE;
	}

	if (object instanceof Integer) {
	    return ((Integer) object).intValue();
	} else {
	    return Integer.MIN_VALUE;
	}
    }

    /**
     * Gets the boolean value of the attribute. If the attribute does not exist,
     * it returns false. If the attribute is not really a boolean, it returns
     * false.
     * 
     * @param nameObject
     *            the name object, the key is from the toString() method.
     * @return the boolean value of the attribute.
     */
    public boolean booleanValue(Object nameObject) {

	Object object = get(nameObject.toString());

	if (object == null) {
	    return false;
	}

	if (object instanceof Boolean) {
	    return ((Boolean) object).booleanValue();
	} else {
	    return false;
	}
    }

    /**
     * Overwrite my attributes with any found in another attribute set.
     * 
     * @param otherAttributes
     *            the other set of attributes.
     */
    public void overwrite(Attributes otherAttributes) {
	if (otherAttributes != null) {
	    for (String name : otherAttributes.keySet()) {
		put(name, otherAttributes.get(name));
	    }
	}
    }

    /**
     * Returns a string representation of the Attributes object.
     * 
     * @return a string representation of the Attributes object.
     */
    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer(512);

	for (String name : keySet()) {

	    Object object = get(name);
	    String value = null;

	    if (object != null) {
		value = object.toString();
		sb.append(String.format("%-12s %s\n", name, value));
	    }
	}

	return sb.toString();
    }

    /**
     * Converts the attributes into an object array of (key, value) pairs. This
     * is the inverse of <code>stuffAttributes</code>.
     * 
     * @return an object array with all the keys and values placed sequentially
     *         [key1, val1, key2, val2, ...]
     */
    public Object[] toObjectArray() {
	Object array[] = new Object[2 * size()];
	Enumeration<?> e = keys();
	int index = 0;
	while (e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    Object val = get(key);
	    array[index] = key;
	    array[index + 1] = val;
	    index += 2;
	}
	return array;
    }
}
