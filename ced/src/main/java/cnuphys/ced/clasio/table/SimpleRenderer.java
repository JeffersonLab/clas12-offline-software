package cnuphys.ced.clasio.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import cnuphys.bCNU.util.Fonts;


public class SimpleRenderer extends JTextField implements TableCellRenderer {
	
	Color bgColors[] = {Color.white, new Color(244, 244, 250)};
	
	public SimpleRenderer() {
		setEditable(false);
		setForeground(Color.black);
		setFont(Fonts.tweenFont);
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean arg2, boolean arg3, int row,
			int col) {
		setText(value.toString());
		setBackground(bgColors[row % 2]);
		return this;
	}

}
