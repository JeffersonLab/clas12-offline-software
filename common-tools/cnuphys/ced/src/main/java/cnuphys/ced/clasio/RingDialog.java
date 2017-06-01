package cnuphys.ced.clasio;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.graphics.component.IpField;

public class  RingDialog extends SimpleDialog {
	
	public static final int CONNECTSPECIFIC = 1;
	public static final int CONNECTDAQ = 2;
	

	// button names for closeout
	private static String[] closeoutButtons = {"Connect", "Cancel"};
	
	private JRadioButton _directConnect;
	private JRadioButton _connectToDAQ;
	private IpField _ipField;
	
	// reason the dialog was closed
	private int _reason = DialogUtilities.CANCEL_RESPONSE;

	/**
	 * Create the panel for selected 
	 * @param id
	 * @param level
	 */
	public RingDialog() {
		super("Connect to Hipo Ring", true, closeoutButtons);
	}
	
	/**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createCenterComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,
				BoxLayout.Y_AXIS));
		
		_ipField = new IpField();
		_ipField.setText("129.57.114.253");
		
		panel.add(_ipField);
		
		ButtonGroup bg = new ButtonGroup();
		_directConnect = new JRadioButton("Connect to Specified Address", true);
		_connectToDAQ = new JRadioButton("Connect to DAQ Ring (Counting House Only)", false);
		bg.add(_directConnect);
		bg.add(_connectToDAQ);
		panel.add(_directConnect);
		panel.add(Box.createVerticalStrut(6));
		panel.add(_connectToDAQ);
	
		Border emptyBorder = BorderFactory
				.createEmptyBorder(4, 4, 4, 4);
		
		CommonBorder cborder = new CommonBorder("Connection Options");

		panel.setBorder(BorderFactory.createCompoundBorder(emptyBorder, cborder));
		return panel;
	}
	
	/*
	 * Returns the reason that the dialog was closed
	 * 
	 * @return <code>DialogUtilities.OK_RESPONSE</code> or
	 * 			<code>DialogUtilities.CANCEL_RESPONSE</code> 
	 */
	public int reason() {
		return _reason;
	}
	
	@Override
	public void handleCommand(String command) {
		if ("Connect".equals(command)) {
			_reason = DialogUtilities.OK_RESPONSE;
		}
		setVisible(false);
	}

	public int getConnectionType() {
		if (_directConnect.isSelected()) {
			return CONNECTSPECIFIC;
		}
		else if (_connectToDAQ.isSelected()) {
			return CONNECTDAQ;
		}
		
		return -1;
	}
	
	public String getIpAddress() {
		return _ipField.getText();
	}

}
