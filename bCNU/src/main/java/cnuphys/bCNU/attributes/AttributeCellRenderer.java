/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import cnuphys.bCNU.component.EnumComboBox;
import cnuphys.bCNU.graphics.style.FillStyle;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.graphics.style.TextAlignment;
import cnuphys.bCNU.log.Log;

public class AttributeCellRenderer implements TableCellRenderer {

    private static Log log = Log.getInstance();

    /**
	 * 
	 */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean isSelected, boolean hasFocus, int row, int column) {

	// get the property data for that row

	AttributeTable attributeTable = (AttributeTable) table;

	if (attributeTable == null) {
	    log.warning("Null propertyTable in PropertyCellRenderer.");
	    return null;
	}

	String attributeName = attributeTable.getAttributeNameAt(row);
	if (attributeName == null) {
	    log.warning("Null propertyName in PropertyCellRenderer.");
	    return null;
	}

	// how it is rendered depends on what type of object is stored in that
	// location

	// value might be null legitimately.
	if (value == null) {
	    return new JLabel("");
	} else if (value instanceof String) {
	    JLabel jl = new JLabel((String) value);
	    return jl;
	}

	else if (value instanceof java.rmi.dgc.VMID) {
	    java.rmi.dgc.VMID guid = (java.rmi.dgc.VMID) value;
	    String s = guid.toString();
	    // use a shortened version for display
	    String s1 = s.substring(0, 8);
	    String s2 = s.substring(s.length() - 10);
	    JLabel jl = new JLabel(s1 + "..." + s2);
	    jl.setOpaque(true);
	    return jl;
	}

	else if (value instanceof Double) {
	    String s = null;
	    s = String.format("%f", (Double) value);
	    JLabel jl = new JLabel(s);
	    return jl;
	} else if (value instanceof Integer) {
	    String s = "" + ((Integer) value).intValue();
	    JLabel jl = new JLabel(s);
	    return jl;
	} else if (value instanceof Color) {
	    JLabel jl = new JLabel("   ");
	    jl.setOpaque(true);
	    jl.setBackground((Color) value);
	    return jl;
	} else if (value instanceof Boolean) {
	    JCheckBox cb = new JCheckBox("", (Boolean) value);
	    cb.setBorder(null);
	    return cb;
	} else if (value instanceof SymbolType) {
	    EnumComboBox jcombo = new EnumComboBox(SymbolType.names,
		    (SymbolType) value);
	    jcombo.setBorder(null);
	    jcombo.setBackground(Color.white);
	    return jcombo;
	} else if (value instanceof LineStyle) {
	    EnumComboBox jcombo = new EnumComboBox(LineStyle.names,
		    (LineStyle) value);
	    jcombo.setBorder(null);
	    jcombo.setBackground(Color.white);
	    return jcombo;
	} else if (value instanceof FillStyle) {
	    EnumComboBox jcombo = new EnumComboBox(FillStyle.names,
		    (FillStyle) value);
	    jcombo.setBorder(null);
	    jcombo.setBackground(Color.white);
	    return jcombo;
	} else if (value instanceof TextAlignment) {
	    EnumComboBox jcombo = new EnumComboBox(TextAlignment.names,
		    (TextAlignment) value);
	    jcombo.setBorder(null);
	    jcombo.setBackground(Color.white);
	    return jcombo;
	} else if (value instanceof Font) {
	    JLabel jl = new JLabel("ABCD 123");
	    jl.setOpaque(true);
	    jl.setFont((Font) value);
	    // jl.setBackground((Color) value);
	    return jl;
	} else { // non standard type. Ask the helpers. If that fails, use a
		 // label.
		 // ask the helpers
	    Component component = attributeTable.askHelpersForRenderer(
		    attributeName, value);
	    if (component != null) {
		return component;
	    }

	    JLabel jl = new JLabel(" " + value.toString());
	    jl.setForeground(Color.white);
	    jl.setHorizontalAlignment(SwingConstants.LEFT);
	    return jl;
	}
    }
}
