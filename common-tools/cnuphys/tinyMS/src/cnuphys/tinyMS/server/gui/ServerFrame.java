package cnuphys.tinyMS.server.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cnuphys.tinyMS.server.TinyMessageServer;
import cnuphys.tinyMS.streamCapture.StreamCapturePane;

@SuppressWarnings("serial")
public class ServerFrame extends JFrame implements ActionListener {
	
	private TinyMessageServer _server;
	
	private JButton _startButton;
	private JButton _stopButton;
	
	private JTextField _nameField;
	
	private JPanel _mainPanel;

	/**
	 * Create a frame that is not yet monitoring any server.
	 */
	public ServerFrame() {
		this(null);
	}
	
	/**
	 * Create a frame that will monitor the given server
	 */
	public ServerFrame(TinyMessageServer server) {
		super((server != null) ? server.getName() : "Tiny Message Server");
		
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
	}
	
	// add the content to the frame
	private void addContent() {
		setLayout(new BorderLayout(2, 2));
		
		_mainPanel = new JPanel();
		_mainPanel.setLayout(new BorderLayout(2, 2));
		
		addNorth();
		
		// add a split pane with a capture pane
		StreamCapturePane scp = new StreamCapturePane();
		Dimension d = scp.getPreferredSize();
		d.height = 150;
		scp.setPreferredSize(d);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				false, _mainPanel, scp);
		splitPane.setResizeWeight(0.4);
        
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	//add the component in the north
	private void addNorth() {
		JPanel nPanel = new JPanel();
		nPanel.setLayout(new BorderLayout(2, 2));
		
		//the buttons
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.Y_AXIS));
		
		_startButton = makeButton("Start");
		_stopButton = makeButton("Stop");
		bPanel.add(_startButton);
		bPanel.add(_stopButton);
		nPanel.add(bPanel, BorderLayout.EAST);
		
		//labels
		
		JPanel lPanel = new JPanel();
		lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.Y_AXIS));
		lPanel.add(Box.createVerticalStrut(4));
		
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName() + " (" +
					InetAddress.getLocalHost().getHostAddress() + ")";
		} catch (UnknownHostException e) {
			hostName = "???";
		}
		
		JLabel hostLabel = new JLabel("Host: " + hostName);
		lPanel.add(hostLabel);
		lPanel.add(Box.createVerticalStrut(6));
		
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout(2, 2));
		namePanel.add(new JLabel("Server Name: "), BorderLayout.WEST);
		_nameField = new JTextField(20);
		namePanel.add(_nameField, BorderLayout.CENTER);

		lPanel.add(namePanel);
		
		nPanel.add(lPanel, BorderLayout.WEST);
		
		_mainPanel.add(nPanel, BorderLayout.NORTH);
	}
		
	//convenience function to create a button
	private JButton makeButton(String label) {
		JButton button = new JButton(label);
		button.addActionListener(this);
		return button;
	}
	
	//fix the states of buttons, etc
	private void fixGuiState() {
		boolean haveServer = (_server != null);
		_startButton.setEnabled(!haveServer);
		_stopButton.setEnabled(haveServer);
		
		_nameField.setEditable(_server == null);
	}
	
	/**
	 * Convenience method that creates the frame that will monitor the given server
	 * and also make the frame visible.
	 * @param server the server to monitor (can be <code>null</code>)
	 * @return the frame, which will be popped open.
	 */
	public static ServerFrame createServerFrame(TinyMessageServer server) {
		final ServerFrame frame = new ServerFrame(server);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});
		
		return frame;
	}
	
	public void setServer(TinyMessageServer server) {
		_server = server;
	}
	
	//shutdown the server
	private void shutDown() {
		if (_server != null) {
			try {
				_server.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		reset();
	}
	
	private void reset() {
		_server = null;
		_nameField.setText("Tiny Message Server");		
		fixGuiState();
	}

	@Override
	public void actionPerformed(ActionEvent aev) {
		
		Object source = aev.getSource();
		
		if (source == _startButton) {
			
		}
		else if (source == _stopButton) {
			shutDown();
		}
		
		fixGuiState();
	}

}
