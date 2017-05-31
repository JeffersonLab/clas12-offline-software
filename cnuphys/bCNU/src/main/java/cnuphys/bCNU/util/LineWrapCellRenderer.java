package cnuphys.bCNU.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class LineWrapCellRenderer extends JTextArea implements
		TableCellRenderer {
	protected static Border noFocusBorder;

	private Color unselectedForeground;
	private Color unselectedBackground;

	public LineWrapCellRenderer() {
		super();
		if (noFocusBorder == null) {
			noFocusBorder = new EmptyBorder(1, 2, 1, 2);
		}
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	@Override
	public void setForeground(Color c) {
		super.setForeground(c);
		unselectedForeground = c;
	}

	@Override
	public void setBackground(Color c) {
		super.setBackground(c);
		unselectedBackground = c;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setForeground(null);
		setBackground(null);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (isSelected) {
			super.setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			super.setForeground((unselectedForeground != null) ? unselectedForeground
					: table.getForeground());
			super.setBackground((unselectedBackground != null) ? unselectedBackground
					: table.getBackground());
		}

		setFont(table.getFont());

		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			if (table.isCellEditable(row, column)) {
				super.setForeground(UIManager
						.getColor("Table.focusCellForeground"));
				super.setBackground(UIManager
						.getColor("Table.focusCellBackground"));
			}
		} else {
			setBorder(noFocusBorder);
		}

		setValue(value);

		FontMetrics fm = getFontMetrics(getFont());
		int fontHeight = fm.getHeight() + table.getRowMargin();
		int textLength = fm.stringWidth(getText()); // length in pixels
		int colWidth = table.getColumnModel().getColumn(column).getWidth();
		int lines = textLength / colWidth + 1; // +1, because we need at least 1
		// row.
		int height = fontHeight * lines;

		if (table.getRowHeight() < height)
			table.setRowHeight(height);

		return this;
	}

	protected void setValue(Object value) {
		setText((value == null) ? "" : value.toString());
	}
}