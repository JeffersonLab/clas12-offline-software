package cnuphys.tinyMS.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import cnuphys.tinyMS.graphics.CommonBorder;
import cnuphys.tinyMS.graphics.Fonts;
import cnuphys.tinyMS.graphics.MultilineHeaderRenderer;
import cnuphys.tinyMS.graphics.RowHeightController;
import cnuphys.tinyMS.server.TinyMessageServer;


public class ClientTable extends JTable {
	
	private static final Font _tableFont = Fonts.defaultFont;
	
	private JScrollPane _scrollPane;
	
	//grid and border color
	private  Color _gridColor = Color.lightGray;
	
	//Controls row heights
	private RowHeightController _rowHeightControl;
	
	//the server
	private TinyMessageServer _server;
	
	/**
	 * Create the client table
	 * @param server the servwer owner
	 */
	public ClientTable(TinyMessageServer server) {
		super(new ClientTableModel(server));
		_server = server;
		getClientModel().setTable(this);
		setFont(_tableFont);

		// single selection
		getSelectionModel().addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(true);
		
		MultilineHeaderRenderer headerRenderer = new MultilineHeaderRenderer();
		
		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
//			column.setCellRenderer(renderer);
			column.setHeaderRenderer(headerRenderer);
			column.setPreferredWidth(ClientTableModel.columnWidths[i]);
//			column.setMinWidth(HermesMSELTableModel.columnWidths[i]);
		}

		
		setDragEnabled(false);

		setGridColor(_gridColor);
		showVerticalLines = true;

		// no reordering
		getTableHeader().setReorderingAllowed(false);
		
		setBorder(BorderFactory.createLineBorder(_gridColor));
		
	//	setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	}
	
	/**
	 * Get the scroll pane
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane() {
//				@Override
//				public Dimension getPreferredSize() {
//					return OrionFrame.getInstance().getTabFullSize();
//				}
			};
			_scrollPane.getViewport().add(this);
			_scrollPane.setBorder(new CommonBorder("Connected Clients"));
		}
		return _scrollPane;
	}
	
	//Fix the row heights
	protected void fixRowHeights() {
		_rowHeightControl.fixRowHeights();
	}

	/**
	 * Get the ClientDataModel
	 * @return he ClientDataModel
	 */
	protected ClientTableModel getClientModel() {
		return (ClientTableModel)getModel();
	}
	

	/**
	 * Convenience routine to fire a data changed event
	 */
	public void fireTableDataChanged() {
		getClientModel().fireTableDataChanged();
	}

}
