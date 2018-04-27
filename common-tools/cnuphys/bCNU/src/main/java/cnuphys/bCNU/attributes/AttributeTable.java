/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A general table for displaying and editing attributes.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AttributeTable extends JTable {


	private DefaultTableCellRenderer def_renderer = new DefaultTableCellRenderer();

	//to render the values
	private AttributeCellRenderer val_renderer;

	private AttributeCellEditor val_editor;
	
	//the scroll pane
	private JScrollPane _scrollPane;


	private TableColumn c1, c2;

	public AttributeTable() {

		val_editor = new AttributeCellEditor(this);
		val_renderer = new AttributeCellRenderer(this);

		Dimension dim = getIntercellSpacing();
		dim.width += 2;
		setIntercellSpacing(dim);
		setAutoCreateColumnsFromModel(false);
		setModel(new AttributeTableModel());
		def_renderer.setHorizontalAlignment(SwingConstants.LEFT);

		c1 = new TableColumn(0, 65, def_renderer, null);
		c2 = new TableColumn(1, 140, val_renderer, val_editor);

		addColumn(c1);
		addColumn(c2);
		getTableHeader().setReorderingAllowed(false);

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
		AttributeTableModel model = getAttributeTableModel();
		if (model != null) {
			model.setData(attributes);
		}
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
	 * Set the attributes that will be displayed,
	 * 
	 * @param atributes the Attributes object to display and edit.
	 */
	public void setAttributes(Attributes attributes) {
		AttributeTableModel model = getAttributeTableModel();
		if (model != null) {
			model.setData(attributes);
		}

		resizeAndRepaint();
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

	
	/**
	 * Get the scroll pane.
	 * @return the scroll pane.
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
		}
		
		return _scrollPane;
	}
	
	
	public static void main(String arg[]) {
		//create some attributes
		//public Attribute(String key, Object value, boolean editable, boolean hidden) {

		
		Attributes attributes = new Attributes();
		attributes.add(new Attribute("HEY", "Hey man", true, false));
		attributes.add(new Attribute("INT1", -9999, true, false));
		attributes.add(new Attribute("INT2", 77, true, false));
		attributes.add(new Attribute("INT2", 88, true, false));
		attributes.add(new Attribute("BOOL1", true, true, false));
		attributes.add(new Attribute("BOOL2", false, true, false));
		attributes.add(new Attribute("DOUBLE1", Double.MAX_VALUE, true, false));
		attributes.add(new Attribute("DOUBLE2", Double.MIN_VALUE, true, false));
		
		//make the table
		
		AttributeTable table = new AttributeTable();
		table.setData(attributes);
		
		//now make the frame to display
		JFrame testFrame = new JFrame("Attributes");
		
		testFrame.setLayout(new BorderLayout(8, 8));
		testFrame.add(table.getScrollPane(), BorderLayout.CENTER);
		
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
