package cnuphys.ced.clasio;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.graphics.component.IpField;

public class ETDialog extends SimpleDialog {
	

	// button names for closeout
	private static String[] closeoutButtons = {"Connect", "Cancel"};
	
	private IpField _ipField;
	private JTextField _fileName;
	
	// reason the dialog was closed
	private int _reason = DialogUtilities.CANCEL_RESPONSE;

	/**
	 * Create the panel for selected 
	 * @param id
	 * @param level
	 */
	public ETDialog() {
		super("Connect to ET Ring", true, closeoutButtons);
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
		_ipField.setText("129.57.167.227");
		
		JPanel subpanel = new JPanel();
		subpanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
		
		JLabel labelip = new JLabel("Address: ");
		subpanel.add(labelip);
		subpanel.add(_ipField);
		
		panel.add(subpanel);
		panel.add(Box.createVerticalStrut(6));
		
		JPanel subpanel2 = new JPanel();
		subpanel2.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
		
		JLabel label = new JLabel("ET File: ");
		_fileName = new JTextField(40);
		_fileName.setText("/tmp/et_sys_clasprod2");
		subpanel2.add(label);
		subpanel2.add(_fileName);
		
		panel.add(subpanel2);
	
		Border emptyBorder = BorderFactory
				.createEmptyBorder(4, 4, 4, 4);
		
		CommonBorder cborder = new CommonBorder("Connect to ET");

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

	public String getFileName() {
		return _fileName.getText();
	}

	
	public String getIpAddress() {
		return _ipField.getText();
	}

}
