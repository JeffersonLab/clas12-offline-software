package cnuphys.tinyMS.table;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import cnuphys.tinyMS.server.ProxyClient;
import cnuphys.tinyMS.server.TinyMessageServer;

public class ConnectionTableModel extends DefaultTableModel {
	

	/** Constant used to designate id column */
	public static final int ID = 0;

	/** Constant used to designate client name column */
	public static final int CLIENT_NAME = 1;

	/** Constant used to designate event user name column */
	public static final int USER_NAME = 2;

	/** Constant used to designate host name column */
	public static final int HOST_NAME = 3;

	/** Constant used to designate operating system name column */
	public static final int OS_NAME = 4;

	/** Constant used to designate time since last ping column */
	public static final int LAST_PING = 5;

	/** Constant used to designate ping duration column */
	public static final int PING_DURATION = 6;

	/** Constant used to designate the message count column */
	public static final int MESSAGE_COUNT = 7;
	

	// the names of the columns
	protected static final String colNames[] = { "ID", "Client\nName", "User\nName", "Host\nName", 
			"OS\nName", "Last\nPing (ago)", "Ping\nDuration", "Message\nCount"};
	
	// the widths of the columns
	protected static final int columnWidths[] = {40, 80, 90, 150, 110, 80, 80, 70};

	//the server
	private TinyMessageServer _server;
	
	//the table
	private ConnectionTable _table;
	
	/**
	 * Create a model for the table data
	 * @param server the controlling server
	 */
	public ConnectionTableModel(TinyMessageServer server) {
		super(colNames, 0);
		_server = server;
	}
	
	/**
	 * Set the client table
	 * @param table the client table
	 */
	protected void setTable(ConnectionTable table) {
		_table = table;
	}
	
	// the data is the list maintained by the server
	protected List<ProxyClient> getData() {
		return (_server == null) ? null : _server.getProxyClients();
	}
	
	/**
	 * Get the client for a given row
	 * @param row the zero-cased row
	 * @return the client for the given row, or null
	 */
	protected ProxyClient getProxyClient(int row) {
		if (row < 0) {
			return null;
		}
		
		List<ProxyClient> clients = getData();
		if (row < clients.size()) {
			return clients.get(row);
		}
		return null;
	}
	

	@Override
	public int getRowCount() {
		List<ProxyClient> clients = getData();
		return (clients == null) ? 0 : clients.size();
	}
	
	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	
	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		ProxyClient item = getProxyClient(row);

		if (item == null) {
			return null;
		}

		switch (col) {

		case ID:
			return "" + item.getId();

		case CLIENT_NAME:
			return item.getClientName();

		case USER_NAME:
			return item.getUserName();

		case HOST_NAME:
			return item.getHostName();

		case OS_NAME:
			return item.getOSName();

		case LAST_PING:
			return item.getTimeSinceLastPing();

		case PING_DURATION:
			return item.getLastPingDurationSmall();

		case MESSAGE_COUNT:
			return "" + item.getMessageCount();
		}
		return null;
	}


}
