package cnuphys.bCNU.graphics.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;

public class IpPortPanel extends JPanel {

	private IpField _ipField;
	private JTextField portField;

	public IpPortPanel(String defaultIP, int defaultPort) {

		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		hSpace(10);
		JLabel ipLbl = new JLabel("IP address:");
		ipLbl.setFont(Fonts.commonFont(Font.PLAIN, 12));
		add(ipLbl);
		hSpace(4);

		_ipField = new IpField(defaultIP);
		_ipField.setHorizontalAlignment(SwingConstants.RIGHT);
		add(_ipField);
		hSpace(12);

		JLabel lab = new JLabel("Port:");
		lab.setFont(Fonts.commonFont(Font.PLAIN, 12));
		add(lab);
		hSpace(4);

		portField = new JTextField("" + defaultPort, 6);
		portField.setHorizontalAlignment(SwingConstants.RIGHT);
		portField.setFont(Fonts.mono);
		add(portField);
		add(Box.createVerticalStrut(8));

	}

	/**
	 * Get the entered IP address
	 * 
	 * @return the IP address
	 */
	public String getIpAddress() {
		return _ipField.getText();
	}

	/**
	 * Set the IP address
	 * 
	 * @param ipAddress
	 *            the new IP address
	 */
	public void setIpAddress(String ipAddress) {
		_ipField.setText(ipAddress);
	}

	/**
	 * Get the port number
	 * 
	 * @return the port number
	 */
	public int getPort() {
		int port = 0;

		try {
			port = Integer.parseInt(portField.getText());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return port;
	}

	/**
	 * Set the port number
	 * 
	 * @param port
	 *            the new port
	 */
	public void setPort(int port) {
		portField.setText(String.valueOf(port));
	}

	private void hSpace(int space) {
		add(Box.createHorizontalStrut(space));
	}

	/**
	 * Main program for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame testFrame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);

		testFrame.setLayout(new BorderLayout());

		IpPortPanel ippanel = new IpPortPanel(Environment.getInstance()
				.getHostAddress(), 63215);

		testFrame.add(ippanel, BorderLayout.NORTH);

		testFrame.setSize(600, 600);
		testFrame.pack();

		testFrame.setVisible(true);
	}
}
