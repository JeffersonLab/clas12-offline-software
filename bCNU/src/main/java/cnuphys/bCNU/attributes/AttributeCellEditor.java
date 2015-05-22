/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import cnuphys.bCNU.dialog.ColorDialog;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.FontDialog;
import cnuphys.bCNU.graphics.style.FillStyle;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.graphics.style.TextAlignment;
import cnuphys.bCNU.log.Log;

public class AttributeCellEditor implements TableCellEditor {

    protected AttributeTable attributeTable = null;

    private static Log log = Log.getInstance();

    /**
     * Create an attribute cell editor.
     * 
     * @param attributeTable
     *            the owner table.
     */
    public AttributeCellEditor(AttributeTable attributeTable) {
	this.attributeTable = attributeTable;
    }

    /**
     * Get the component that will do the editing. For complicates edits (such
     * as color) the editing is done "in-situ" and this returns null.
     * 
     * @param table
     *            The underlying table.
     * @param value
     *            The object being edited.
     * @param isSelected
     *            <code>true</code> if selected
     * @param row
     *            the row
     * @param column
     *            the column
     */

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
	    boolean isSelected, int row, int column) {

	if (attributeTable == null) {
	    log.warning("Null attributeTable in AttributeCellEditor.");
	    return null;
	}

	attributeTable.removeEditor();

	String attributeName = attributeTable.getAttributeNameAt(row);
	if (attributeName == null) {
	    log.warning("Null attributeName in AttributeCellEditor.");
	    return null;
	}

	// string, use text field

	if (value instanceof String) {
	    AttributeStringEditor pse = new AttributeStringEditor(
		    attributeTable, attributeName, (String) value);
	    return pse;
	}

	// double, use double editor

	else if (value instanceof Double) {
	    AttributeDoubleEditor pde = new AttributeDoubleEditor(
		    attributeTable, attributeName, (Double) value);
	    return pde;
	}

	// integer, use int editor

	else if (value instanceof Integer) {
	    AttributeIntegerEditor pie = new AttributeIntegerEditor(
		    attributeTable, attributeName, (Integer) value);
	    return pie;
	}

	// color? use the font dialog in-place
	else if (value instanceof Font) {

	    Font oldFont = (Font) value;
	    FontDialog fd = new FontDialog(oldFont);
	    fd.setVisible(true);
	    Font newFont = fd.getSelectedFont();
	    if ((newFont != null) && !newFont.equals(oldFont)) {
		if (attributeTable != null) {
		    attributeTable.setAttribute(attributeName, newFont);
		}
	    }

	    // editing is actually done, so just return null.
	    return null;
	}

	// color? use the color dialog in-place
	else if (value instanceof Color) {

	    Color oldColor = (Color) value;

	    ColorDialog cd = new ColorDialog(oldColor, true, true);

	    cd.setVisible(true);

	    if (cd.getAnswer() == DialogUtilities.OK_RESPONSE) {
		Color newColor = cd.getColor();
		if (attributeTable != null) {
		    attributeTable.setAttribute(attributeName, newColor);
		}

	    }

	    // editing is actually done, so just return null.
	    return null;
	}

	// boolean? Use a check box

	else if (value instanceof Boolean) {
	    AttributeBooleanEditor pbe = new AttributeBooleanEditor(
		    attributeTable, attributeName, isSelected);
	    return pbe;
	}

	// Symbol? Use a combo box
	else if (value instanceof SymbolType) {
	    AttributeEnumEditor pse = new AttributeEnumEditor(attributeTable,
		    SymbolType.names, attributeName, (SymbolType) value);
	    return pse;
	}

	// LineStyle? Use a combo box
	else if (value instanceof LineStyle) {
	    AttributeEnumEditor pse = new AttributeEnumEditor(attributeTable,
		    LineStyle.names, attributeName, (LineStyle) value);
	    return pse;
	}

	// FillStyle? Use a combo box
	else if (value instanceof FillStyle) {
	    AttributeEnumEditor pse = new AttributeEnumEditor(attributeTable,
		    FillStyle.names, attributeName, (FillStyle) value);
	    return pse;
	}

	// Text Alignment? Use a combo box
	else if (value instanceof TextAlignment) {
	    AttributeEnumEditor pse = new AttributeEnumEditor(attributeTable,
		    TextAlignment.names, attributeName, (TextAlignment) value);
	    return pse;
	}

	// ask the helpers for an editor
	Component component = attributeTable.askHelpersForEditor(attributeName,
		value);
	if (component != null) {
	    return component;
	}
	return null;
    }

    @Override
    public Object getCellEditorValue() {
	return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
	return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
	return true;
    }

    @Override
    public boolean stopCellEditing() {
	attributeTable.removeEditor();
	return true;
    }

    @Override
    public void cancelCellEditing() {
	attributeTable.removeEditor();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
    }

}
