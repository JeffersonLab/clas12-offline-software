package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;

@SuppressWarnings("serial")
	public class Attributes extends Vector<Attribute> implements Comparator<Attribute>{

	public static final Color NULLCOLOR = new Color(254, 253, 252, 0);
	
	

	/**
	 * Create an empty Attributes object.
	 */
	public Attributes() {
		super(100);
	}
	
	/**
	 * See if there is an attribute with the given key
	 * @param attributeKey the key
	 * @return <code>true</code> if this collection contains the key
	 */
	public boolean contains (String attributeKey) {
		for (Attribute attribute : this) {
			String key = attribute.getKey();
			if (attributeKey.equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tries to find the attribute with the given key
	 * 
	 * @param attributeKey match to the key
	 * @return the Attribute, or null.
	 */
	public Attribute getAttribute(String attributeKey) {
		
		for (Attribute attribute : this) {
			String key = attribute.getKey();
			if (attributeKey.equals(key)) {
				return attribute;
			}
		}
		return null;
	}
	
	/**
	 * Tries to set the attribute with the given value
	 * 
	 * @param attributeKey match to the key
	 * @param value the value to set
	 * @return <code>true</code> if the operation was successful
	 */
	public boolean setValue(String attributeKey, Object value) {
		
		Attribute attribute = getAttribute(attributeKey);
		if (attribute == null) {
			return false;
		}
		attribute.setValue(value);
		return true;
	}
	


	@Override
	public boolean add(Attribute attribute) {
		if (attribute == null) {
			return false;
		}

		int index = Collections.binarySearch(this, attribute);
		if (index >= 0) { // duplicate
			remove(index);
		} else {
			index = -(index + 1); // now the insertion point.
		}

		add(index, attribute);
		return true;
	}
	
	/**
	 * Add an attribute
	 * @param key the key (name)
	 * @param value the value
	 * @param editable whether it is editable
	 * @param hidden whether it is hidden (not on the table)
	 * @return
	 */
	public boolean add(String key, Object value, boolean editable, boolean hidden) {
		Attribute attribute = new Attribute(key, value, editable, hidden);
		return add(attribute);
	}
	
	/**
	 * Add an attribute that is editable and not hidden
	 * @param key the key (name)
	 * @param value the value
	 * @return
	 */
	public boolean add(String key, Object value) {
		return add(key, value, true, false);
	}



	@Override
	public int compare(Attribute a1, Attribute a2) {
		return a1.compareTo(a2);
	}

}
