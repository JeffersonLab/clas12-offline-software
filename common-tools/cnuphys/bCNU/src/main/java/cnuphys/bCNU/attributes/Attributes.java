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


	@Override
	public int compare(Attribute a1, Attribute a2) {
		return a1.compareTo(a2);
	}

	


}
