/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cnuphys.bCNU.util.Fonts;

/**
 * A general table for displaying and editing attributes.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AttributeTable extends JTable {
	
	//default and actual column widths
	private static final int NAME_WIDTH = 70;
	private static final int VAL_WIDTH = 140;
	
	private int _nameWidth;
	private int _valueWidth;
	

	//to render keys (names)
	private DefaultTableCellRenderer _nameRenderer;

	//to render the values
	private AttributeCellRenderer _valRenderer;

	//to edit the values
	private AttributeCellEditor _valEditor;
	
	//the scroll pane
	private JScrollPane _scrollPane;

	//default font
	public static final Font defaultFont = Fonts.mediumFont;

	//columns
	private TableColumn c1, c2;
	
	/**
	 * Create an attribute table with default column widths
	 */
	public AttributeTable() {
		this(NAME_WIDTH, VAL_WIDTH);
	}

	/**
	 * Create an attribute table
	 * @param nw width of name column
	 * @param vw width of value column
	 */
	public AttributeTable(int nw, int vw) {
		_nameWidth = nw;
		_valueWidth = vw;
		
		putClientProperty("terminateEditOnFocusLost", true);
		
		_nameRenderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				c.setFont(defaultFont);
				return c;
			}
		};

		_valEditor = new AttributeCellEditor(this);
		_valRenderer = new AttributeCellRenderer(this);

		Dimension dim = getIntercellSpacing();
		dim.width += 2;
		setIntercellSpacing(dim);
		setAutoCreateColumnsFromModel(false);
		setModel(new AttributeTableModel(this));
		_nameRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		c1 = new TableColumn(0, _nameWidth, _nameRenderer, null);
		c2 = new TableColumn(1, _valueWidth, _valRenderer, _valEditor);

		addColumn(c1);
		addColumn(c2);
		getTableHeader().setReorderingAllowed(false);
		
		setGridColor(Color.lightGray);

		setRowHeight(24);
	}
	
	
	/**
	 * Get the attribute data model
	 * @return the data model
	 */
	public AttributeTableModel getAttributeTableModel() {
		return (AttributeTableModel)getModel();
	}

	/**
	 * Get the data
	 * @return the table data
	 */
	public List<Attribute> getData() {
		AttributeTableModel model = getAttributeTableModel();
		return (model == null) ? null : model.getData();
	}
	
	/**
	 * Set the model data
	 * @param attributes the data
	 */
	public void setData(Attributes attributes) {
		System.err.println("ATTRIBUTE COUNT: " + attributes.size());
		AttributeTableModel model = getAttributeTableModel();
		if (model != null) {
			model.setData(attributes);
		}
		resizeAndRepaint();
	}
	
	/**
	 * Clear the table
	 */
	public void clear() {
		AttributeTableModel model = getAttributeTableModel();
		if (model != null) {
			model.clear();
		}
		removeEditor();
		resizeAndRepaint();
	}
	
	/**
	 * Tries to find the attribute with the given key
	 * 
	 * @param attributeKey match to the key
	 * @return the Attribute, or null.
	 */
	public Attribute getAttribute(String attributeKey) {
		return this.getAttributeTableModel().getAttribute(attributeKey);
	}
	
	/**
	 * Get the Attribute at the given row
	 * @param row the row
	 * @return the Attribute
	 */
	public Attribute getAttribute(int row) {
		AttributeTableModel model = getAttributeTableModel();
		
		return (model != null) ? model.getAttribute(row) : null;
	}
	

    @Override
	public void removeEditor() {
		super.removeEditor();
	}

	
	/**
	 * Get the scroll pane.
	 * @return the scroll pane.
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this) {
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					d.width = _nameWidth + _valueWidth + 20;
					return d;
				}
			};
		}
		
		return _scrollPane;
	}
	
	
	public static void main(String arg[]) {
		//create some attributes
		//public Attribute(String key, Object value, boolean editable, boolean hidden) {

		
		Attributes attributes = new Attributes();
		attributes.add(new Attribute("HEY", "Hey man"));
		attributes.add(new Attribute("DUDE", "Dude", false));
		attributes.add(new Attribute("INT1", -9999));
		attributes.add(new Attribute("INT2", 77, false));
		attributes.add(new Attribute("INT3", 88));
		attributes.add(new Attribute("BOOL1", true));
		attributes.add(new Attribute("BOOL2", false, false));
		attributes.add(new Attribute("DOUBLE1", Double.MAX_VALUE));
		attributes.add(new Attribute("DOUBLE2", Double.MIN_VALUE, false));
		attributes.add(new Attribute("FLOAT", Double.MIN_VALUE));
		attributes.add(new Attribute("LONG", 88L));
		attributes.add(new Attribute("FLOAT", 123f));
		attributes.add(new Attribute("BYTE", (byte)120));
		attributes.add(new Attribute("SHORT", (short)-32000));
		attributes.add(new Attribute("LONG", 88L));
		
		
		JSlider slider = new JSlider(-10, 10, 0);
		
		slider.setMajorTickSpacing((slider.getMaximum()-slider.getMinimum())/2);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		attributes.add(new Attribute("SLIDER", slider));
		
		//make the table
		
		AttributeTable table = new AttributeTable();
		
		table.setData(attributes);
		AttributePanel panel = new AttributePanel(table);
		
		//now make the frame to display
		JFrame testFrame = new JFrame("Attributes");
		
		testFrame.setLayout(new BorderLayout(8, 8));
		testFrame.add(panel, BorderLayout.CENTER);
		
		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}
}
