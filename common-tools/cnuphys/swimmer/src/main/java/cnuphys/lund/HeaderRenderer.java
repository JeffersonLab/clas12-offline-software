package cnuphys.lund;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class HeaderRenderer extends JTextField implements TableCellRenderer {

	public HeaderRenderer() {
		setBackground(X11Colors.getX11Color("wheat"));
		setForeground(Color.black);
		setHorizontalAlignment(SwingConstants.CENTER);
		Border lineBorder = BorderFactory.createLineBorder(Color.black);
		Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean arg2, boolean arg3, int arg4,
			int arg5) {
		setText(value.toString());
		setFont(table.getFont());
		return this;
	}
}
