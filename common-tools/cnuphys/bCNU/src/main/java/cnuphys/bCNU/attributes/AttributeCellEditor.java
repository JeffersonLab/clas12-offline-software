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

	//the table
	protected AttributeTable _attributeTable;

	/**
	 * Create an attribute cell editor.
	 * 
	 * @param attributeTable the owner table.
	 */
	public AttributeCellEditor(AttributeTable attributeTable) {
		_attributeTable = attributeTable;
	}

	/**
	 * Get the component that will do the editing. For complicates edits (such
	 * as color) the editing is done "in-situ" and this returns null.
	 * 
	 * @param table The underlying table.
	 * @param value The object being edited.
	 * @param isSelected
	 * @param row
	 * @param col
	 */

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		if (_attributeTable == null) {
			return null;
		}
		
		Attribute attribute = _attributeTable.getAttribute(row);
		AttributeEditor editor = AttributeEditor.AttributeEditorFactory(_attributeTable, attribute, value);
		
		Component component = (editor != null) ? editor.component : null;

		if (component != null) {
			component.setFont(AttributeTable.defaultFont);
		}
		return component;
	}

	@Override
	public Object getCellEditorValue() {
//		System.err.println("getCellEditorValue");
		return null;
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
//		System.err.println("isCellEditable");
		return true;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
	//	System.err.println("shouldSelectCell");
		return true;
	}

	@Override
	public boolean stopCellEditing() {
	//	System.err.println("stopCellEditing");
		_attributeTable.removeEditor();
		return true;
	}

	@Override
	public void cancelCellEditing() {
	//	System.err.println("cancelCellEditing");
		_attributeTable.removeEditor();
	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
//		System.err.println("addCellEditorListener: " + l.getClass().getName());
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
//		System.err.println("removeCellEditorListener: " + l.getClass().getName());
	}
	

}
