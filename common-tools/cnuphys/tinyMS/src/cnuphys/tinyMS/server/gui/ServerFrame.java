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

import cnuphys.tinyMS.Environment.Environment;
import cnuphys.tinyMS.graphics.GraphicsUtilities;
import cnuphys.tinyMS.graphics.ImageManager;
import cnuphys.tinyMS.graphics.MemoryStripChart;
import cnuphys.tinyMS.log.SimpleLogPane;
import cnuphys.tinyMS.server.TinyMessageServer;
import cnuphys.tinyMS.table.ClientTable;

@SuppressWarnings("serial")
public class ServerFrame extends JFrame implements ActionListener {

	//the server
	private TinyMessageServer _server;

	//buttons
	private JButton _clearButton;
	private JButton _stopButton;
	private JButton _logoutButton;

	//holds the log
	private SimpleLogPane _logPane;
	
	//when the gui (and approcimately the server) started
	private long _startTime;
	
	// maintenance timer
	private Timer _timer;

	//how long we've been running
	private JLabel _durationLabel;
	
	//table stuff
	private ClientTable _table;
	
	//Memory strip chart
	private MemoryStripChart _chart;
	
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
		_logoutButton = makeButton("Logout Client");
		panel.add(_clearButton);
		panel.add(_stopButton);
		panel.add(_logoutButton);
		
		add(panel, BorderLayout.SOUTH);
	}

	//add the components in the center
	private void addCenter() {
		_logPane = new SimpleLogPane();
		Dimension d = _logPane.getPreferredSize();
		d.height = 350;
		_logPane.setPreferredSize(d);
				
		//the table
		_table = new ClientTable(_server);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, _logPane, _table.getScrollPane());
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

		JLabel hostLabel = new JLabel("Host: " + hostName);
		lPanel.add(hostLabel);
		
		lPanel.add(Box.createVerticalStrut(6));
		JLabel nameLabel = new JLabel("Server: " + _server.getName());
		lPanel.add(nameLabel);
		
		lPanel.add(Box.createVerticalStrut(6));
		_durationLabel = new JLabel("Running:");
		lPanel.add(_durationLabel);


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
		_logoutButton.setEnabled(rowSelected);
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
		else if (source == _logoutButton) {
			
		}

		fixGuiState();
	}
	
	/**
	 * Tell the GUI to do its houseKeeping
	 * @param count running index of housekeeping calls.
	 */
	public void houseKeeping(long count) {	
		uptime();
		
		if (_server.isShutDown()) {
			_timer.cancel();
			System.err.println("Frame detected server shutdown.");
		}
		
//		if (_table != null) {
//			_table.fireTableDataChanged();
//		}
		
		//do something every two seconds
		if ((count % 2) == 0) {
			if (_table != null) {
				_table.fireTableDataChanged();
			}
		}
	}
	
	private void uptime() {
		
		if (_server.isShutDown()) {
			_durationLabel.setText("Server has stopped.");
			return;
		}
		
		long del = (System.currentTimeMillis() - _startTime)/1000;
		
		long days = del / 86400;
		del = del % 86400;
		
		long hours = del / 3600;
		del = del % 3600;
		
		long minutes = del / 60;
		long seconds = del % 60;
		
		String s = String.format("Running: %d days and %02d:%02d:%02d", days, hours, minutes, seconds);
		_durationLabel.setText(s);
	}
	

	/**
	 * Convenience routine to fire a data changed event
	 */
	public void fireTableDataChanged() {
		if (_table != null) {
			_table.fireTableDataChanged();
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

}
