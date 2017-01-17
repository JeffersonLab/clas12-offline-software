package cnuphys.ced.clasio.datatable;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.clasio.table.HeaderRenderer;

public class BankDataTable extends JTable {
	
	private JScrollPane _scrollPane;
	
	private static final int COLWIDTH = 100;

	/**
	 * Create a bank table
	 * @param bankName the name of the bank, e.g. "DC::tdc"
	 */
	public BankDataTable(String bankName) {
		super(new BankTableModel(bankName));
		getBankTableModel().setTable(this);
		
		HeaderRenderer hrender = new HeaderRenderer();
		
		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
	//		column.setCellRenderer(renderer);
			column.setHeaderRenderer(hrender);
			column.setPreferredWidth(COLWIDTH);
//			column.setMinWidth(HermesMSELTableModel.columnWidths[i]);
		}

//		this.setAutoResizeMode(AUTO_RESIZE_OFF);
		getTableHeader().setResizingAllowed(true);
		setShowGrid(true);
		setGridColor(Color.gray);

	}
	
	/**
	 * Get the underlying model
	 * @return the data model
	 */
	public BankTableModel getBankTableModel() {
		return (BankTableModel)getModel();
	}
	
	/**
	 * Set the event for table display
	 * @param event the event
	 */
	public void setEvent(DataEvent event) {
		 getBankTableModel().setData(event);
	}
	
	/**
	 * Get the table's scroll pane
	 * @return te table's scroll pane
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this) {
				@Override
				public Dimension getPreferredSize() {
					int width = getModel().getColumnCount()*COLWIDTH + 20;
					return new Dimension(Math.min(1000,  width), 550);
				}
				

			};
		}
		return _scrollPane;
	}

}
