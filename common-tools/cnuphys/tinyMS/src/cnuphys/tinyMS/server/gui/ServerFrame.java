package cnuphys.tinyMS.server.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.tinyMS.Environment.Environment;
import cnuphys.tinyMS.graphics.Fonts;
import cnuphys.tinyMS.graphics.GraphicsUtilities;
import cnuphys.tinyMS.graphics.ImageManager;
import cnuphys.tinyMS.graphics.MemoryStripChart;
import cnuphys.tinyMS.log.SimpleLogPane;
import cnuphys.tinyMS.server.ProxyClient;
import cnuphys.tinyMS.server.TinyMessageServer;
import cnuphys.tinyMS.table.ConnectionTable;

@SuppressWarnings("serial")
public class ServerFrame extends JFrame implements ActionListener, ListSelectionListener {

	//the server
	private TinyMessageServer _server;

	//buttons
	private JButton _clearButton;
	private JButton _stopButton;
	private JButton _shutdownButton;

	//holds the log
	private SimpleLogPane _logPane;
	
	//when the gui (and approximately the server) started
	private long _startTime;
	
	// maintenance timer
	private Timer _timer;

	//how long we've been running
	private JLabel _durationLabel;
	
	// average bandwidth
	private JLabel _bandwidthLabel;
	
	//table stuff
	private ConnectionTable _table;
	
	//Memory strip chart
	private MemoryStripChart _chart;
	
	//list of topics
	private TopicList _topicList;
	
	/**
	 * Create a frame that will monitor the given server
	 */
	public ServerFrame(TinyMessageServer server) {
		super((server != null) ? server.getName() : "Tiny Message Server");
		
		_startTime = System.currentTimeMillis();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				shutDown();
				System.exit(0);
			}
		};
		addWindowListener(windowAdapter);

		if (server != null) {
			setServer(server);
		}

		addContent();
		fixGuiState();
		
		//listen for table selections
		_table.getSelectionModel().addListSelectionListener(this);
		
		//setup a housekeeping maintenance thread
		setupMaintenanceTimer();
	}
	
	// houskeeping timer
	private long _houseKeepingCount;
	private void setupMaintenanceTimer() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				houseKeeping(++(_houseKeepingCount));
			}

		};
		_timer = new Timer();
		_timer.scheduleAtFixedRate(task, 10000, 1000);
	}

	// add the content to the frame
	private void addContent() {
		setLayout(new BorderLayout(2, 2));

//		_mainPanel = new JPanel();
//		_mainPanel.setLayout(new BorderLayout(2, 2));

		addNorth();
		addCenter();
		addSouth();
		addEast();

		// add a split pane with a capture pane
		

//		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, _mainPanel, _logPane);
//		splitPane.setResizeWeight(0);
//
//		add(splitPane, BorderLayout.CENTER);
	}
	
	private void addSouth() {
		//button panel
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 4));
		
		_clearButton = makeButton("Clear Log");
		_stopButton = makeButton("Stop the Server");
		_shutdownButton = makeButton("Shutdown Client");
		panel.add(_clearButton);
		panel.add(_stopButton);
		panel.add(_shutdownButton);
		
		add(panel, BorderLayout.SOUTH);
	}
	
	//add the component in the east
	private void addEast() {
	}

	//add the components in the center
	private void addCenter() {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		_logPane = new SimpleLogPane();
		Dimension d = _logPane.getPreferredSize();
		d.height = 350;
		_logPane.setPreferredSize(d);
		
		panel.add(_logPane, BorderLayout.CENTER);
		_topicList = new TopicList(_server);
		panel.add(_topicList.getScrollPane(), BorderLayout.EAST);
				
		//the table
		_table = new ConnectionTable(_server);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, panel, _table.getScrollPane());
		splitPane.setResizeWeight(0.8);

		add(splitPane, BorderLayout.CENTER);
	}
	
	// add the component in the north
	private void addNorth() {
		JPanel nPanel = new JPanel();
		nPanel.setLayout(new BorderLayout(15, 6));

		// labels

		JPanel lPanel = new JPanel();
		lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.Y_AXIS));
		lPanel.add(Box.createVerticalStrut(4));

		Environment env = Environment.getInstance();
		String hostName = env.getHostName() + " (" +
				env.getHostAddress() + ":" + _server.getLocalPort() + ")";

		addLabel(lPanel, "Host: " + hostName, 0);
		addLabel(lPanel, "Server: " + _server.getName() + 
				"   Port: " + _server.getPort() +
				"   Version: " + _server.getVersion(), 6);
		_durationLabel = addLabel(lPanel, "Running:" + _server.getName(), 6);
		_bandwidthLabel = addLabel(lPanel, "Average bandwidth:", 6);

		nPanel.add(lPanel, BorderLayout.CENTER);
		
		//memory strip chart
		_chart = new MemoryStripChart(_server);
		nPanel.add(_chart, BorderLayout.WEST);
		
		Border emptyBorder = BorderFactory
				.createEmptyBorder(4, 4, 4, 4);
		nPanel.setBorder(emptyBorder);
		
	//	CommonBorder cborder = new CommonBorder("");

	//	nPanel.setBorder(BorderFactory.createCompoundBorder(emptyBorder, cborder));

		add(nPanel, BorderLayout.NORTH);
	}

	//add a label
	private JLabel addLabel(JPanel lPanel, String s, int gap) {
		if (gap > 0) {
			lPanel.add(Box.createVerticalStrut(gap));
		}
		
		JLabel label = new JLabel(s);
		label.setFont(Fonts.mediumBoldFont);
		lPanel.add(label);

		return label;
	}
	
	// convenience function to create a button
	private JButton makeButton(String label) {
		JButton button = new JButton(label);
		button.addActionListener(this);
		return button;
	}

	// fix the states of buttons, etc
	private void fixGuiState() {
		boolean haveServer = (_server != null);
		_stopButton.setEnabled(haveServer);
		
		boolean rowSelected = false;
		_shutdownButton.setEnabled(rowSelected);
	}

	/**
	 * Convenience method that creates the frame that will monitor the given
	 * server and also make the frame visible.
	 * 
	 * @param server
	 *            the server to monitor (can be <code>null</code>)
	 * @return the frame, which will be popped open.
	 */
	public static ServerFrame createServerFrame(TinyMessageServer server) {
		final ServerFrame frame = new ServerFrame(server);
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					frame.pack();
					GraphicsUtilities.centerComponent(frame);
					frame.setVisible(true);
//					frame.setLocationRelativeTo(null);
				}
			});
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		return frame;
	}

	public void setServer(TinyMessageServer server) {
		_server = server;
	}

	// shutdown the server
	private void shutDown() {
		if (_server != null) {
			try {
				System.out.println("\n===========================================");
				System.out.println("***** Server is shutting down from the server GUI. *****");
				System.out.println("\n===========================================");
				_server.shutdown();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		reset();
	}

	private void reset() {
		_server = null;
		fixGuiState();
	}

	@Override
	public void actionPerformed(ActionEvent aev) {

		Object source = aev.getSource();

		if (source == _clearButton) {
			_logPane.clear();

		}
		else if (source == _stopButton) {
			if (reallyStop()) {
				shutDown();
			}
		}
		else if (source == _shutdownButton) {
			shutdownClient();
		}

		fixGuiState();
	}
	
	//shutdown a client
	private void shutdownClient() {
		
		if (_table != null) {
			ProxyClient client = _table.getSelectedClient();
			if (client != null) {
				ImageIcon icon = ImageManager.getInstance().loadImageIcon("images/cnuicon.png");
				int answer = JOptionPane.showConfirmDialog(null,
						"Do you really want to shutdown client: " + client.getClientName() + "?",
						"Shutdown a client?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
						icon);
				
				if (answer == JFileChooser.APPROVE_OPTION) {
					client.shutdown();
				}
			}
		}


	}
	
	/**
	 * Tell the GUI to do its houseKeeping
	 * @param count running index of housekeeping calls.
	 */
	public void houseKeeping(long count) {	
		uptime();
		
		if (_server.isShutDown()) {
			_timer.cancel();
			System.out.println("Application GUI (Server Frame) detected server shutdown.");
		}
		
		int n = 2;
		//do something every n seconds
		if ((count % n) == 0) {
			if (_table != null) {
				_table.fireTableDataChanged();
			}
		}
	}
	
	//how long has the server been running?
	private void uptime() {
		
		if (_server.isShutDown()) {
			_durationLabel.setText("Server has stopped.");
			return;
		}
		
		//uptime in seconds
		long del = (System.currentTimeMillis() - _startTime)/1000;
		
		//compute bandwidth
		int bandwidth = (int)((double)_server.getBytesTransferred()/((double)Math.max(1, del)));
		
		long days = del / 86400;
		del = del % 86400;
		
		long hours = del / 3600;
		del = del % 3600;
		
		long minutes = del / 60;
		long seconds = del % 60;
		
		String s = String.format("Running: %d days and %02d:%02d:%02d", days, hours, minutes, seconds);
		_durationLabel.setText(s);
		
		String bwstr = NumberFormat.getNumberInstance(Locale.US).format(bandwidth);
		_bandwidthLabel.setText("Average bandwidth: " + bwstr + " bytes/s");
	}
	

	/**
	 * Convenience routine to fire a data changed event
	 */
	public void fireTableDataChanged() {
		if (_table != null) {
			_table.fireTableDataChanged();
		}
		else {
			System.out.println("DID NOT FIRE TABLE DATA CHANGE (Frame)");
		}
	}
	
	/**
	 * Do i really want to stop the server?
	 * @return <code>true</code> if I really want to stop
	 */
	public boolean reallyStop() {
		ImageIcon icon = ImageManager.getInstance().loadImageIcon("images/cnuicon.png");
		int answer = JOptionPane.showConfirmDialog(null,
				"Do you really want to stop the server?",
				"Stop the server?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
				icon);
		
		return (answer == JFileChooser.APPROVE_OPTION);

	}

	@Override
	public void valueChanged(ListSelectionEvent lse) {
		if (!lse.getValueIsAdjusting()) {
			_shutdownButton.setEnabled(_table.getSelectedRow() >= 0);
		}
	}

	/**
	 * Get the topic list
	 * @return the topic list
	 */
	public TopicList getTopicList() {
		return _topicList;
	}

	/**
	 * Get the client table
	 * @return the client table
	 */
	public ConnectionTable getClientTable() {
		return _table;
	}

}
