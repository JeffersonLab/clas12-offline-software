/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import cnuphys.lund.X11Colors;

public class AttributeCellRenderer extends DefaultTableCellRenderer {
	
	private static final Color editableColor = X11Colors.getX11Color("Alice Blue");
	private static final Color notEditableColor = new Color(240, 240, 240);
	
	//the table
	private AttributeTable _attributeTable;
	
	public AttributeCellRenderer(AttributeTable table) {
		_attributeTable = table;
	}

	/**
	 * 
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Attribute attribute = _attributeTable.getAttribute(row);
		AttributeEditor editor = AttributeEditor.AttributeEditorFactory(_attributeTable, attribute, value);
		Component component = (editor != null) ? editor.component : null;

		if (component != null) {
			component.setFont(AttributeTable.defaultFont);
			component.setBackground(attribute.isEditable() ? editableColor : notEditableColor);
		}
		return component;
	}
}
