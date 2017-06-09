/**
 * 
 */
package cnuphys.tinyMS.graphics;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.table.*;

/**
 * A Renderer for multiline table header labels to save space when the label is
 * bigger than the data being displayed
 * 
 * @version 1.0 11/09/98
 */
@SuppressWarnings("serial")
public class MultilineHeaderRenderer extends JList implements TableCellRenderer {

	public MultilineHeaderRenderer() {
		setOpaque(true);
		setForeground(X11Colors.getX11Color("dark blue"));
		setBackground(new Color(212, 212, 212));
		
		
		Border bevelBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
		Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		Border compoundBorder = new CompoundBorder(bevelBorder, emptyBorder);

		
		setBorder(compoundBorder);
		ListCellRenderer renderer = getCellRenderer();
		((JLabel) renderer).setHorizontalAlignment(SwingConstants.CENTER);
		setCellRenderer(renderer);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setFont(table.getFont());
		String str = (value == null) ? "" : value.toString();
		BufferedReader br = new BufferedReader(new StringReader(str));
		String line;
		Vector<String> v = new Vector<String>();
		try {
			while ((line = br.readLine()) != null) {
				v.addElement(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		setListData(v);
		return this;
	}
}
