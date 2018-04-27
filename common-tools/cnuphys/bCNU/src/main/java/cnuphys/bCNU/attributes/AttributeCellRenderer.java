/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class AttributeCellRenderer implements TableCellRenderer {
	
	private AttributeTable _attributeTable;
	
	public AttributeCellRenderer(AttributeTable table) {
		_attributeTable = table;
	}

	/**
	 * 
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		
		Attribute attribute = _attributeTable.getAttribute(row);
		AttributeEditor editor = AttributeEditor.AttributeEditorFactory(_attributeTable, attribute, value);
		return  (editor != null) ? editor.component : null;
	}
}
