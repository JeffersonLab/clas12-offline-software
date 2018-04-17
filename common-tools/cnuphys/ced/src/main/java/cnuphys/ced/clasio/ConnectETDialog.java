package cnuphys.ced.clasio;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Environment;

public class ConnectETDialog extends SimpleDialog {
	
	public static ImageIcon _etIcon;


	// button names for closeout
	private static String[] closeoutButtons = {"Connect", "Cancel"};
	
	
	// reason the dialog was closed
	private int _reason = DialogUtilities.CANCEL_RESPONSE;
	
	//text fields
	private JTextField _stationTF;
	private JTextField _fileTF;
	
	//machine combobox
	private JComboBox _machineCombo;

	/**
	 * Create the panel for selected 
	 * @param id
	 * @param level
	 */
	public ConnectETDialog() {
		super("Connect to ET Ring", true, closeoutButtons);
	}
	
	/**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createWestComponent() {
		if (_etIcon == null) {
			_etIcon = ImageManager.getInstance().loadImageIcon("images/etlogo.png");
		}
		if (_etIcon == null) {
			return null;
		}
		JLabel label = new JLabel(_etIcon);
		
		Border emptyBorder = BorderFactory
				.createEmptyBorder(4, 4, 4, 4);

		label.setBorder(emptyBorder);
		return label;
	}
	
	public String getFile() {
		return _fileTF.getText();
	}
	
	public String getStation() {
		return _stationTF.getText();
	}
	
	/**
	 * Get the machine to connect to
	 * @return the machine to connect to
	 */
	public String getMachine() {
		String machineStr = (String)(_machineCombo.getSelectedItem());
		
		if ((machineStr != null) && (machineStr.contains("local host"))) {
			machineStr = localHost();
		}
		return machineStr;
	}
	
	private String localHost() {
		return Environment.getInstance().getHostAddress();
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
		panel.setLayout(new VerticalFlowLayout());
		
		JPanel npanel = new JPanel();
		npanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		npanel.add(new JLabel("CLON cpu: "));
		
		String localHost = "local host: " + localHost();
		String[] machineStrings = { "clondaq2", "clondaq3", "clondaq4", "clondaq5", "clondaq6", localHost };

		//Create the combo box, select item at index 4.
		//Indices start at 0, so 4 specifies the pig.
		_machineCombo = new JComboBox(machineStrings);
		_machineCombo.setSelectedIndex(4);
		npanel.add(_machineCombo);
		
		JPanel cpanel = new JPanel();
		cpanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		cpanel.add(new JLabel("Station: "));
		_stationTF = new JTextField(14);
		_stationTF.setText("ced_station");
		cpanel.add(_stationTF);
		
		JPanel spanel = new JPanel();
		spanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));		
		spanel.add(new JLabel("File: "));
		_fileTF = new JTextField(20);
		_fileTF.setText("/et/clasprod");
		spanel.add(_fileTF);
		
		panel.add(npanel);
		panel.add(cpanel);
		panel.add(spanel);
		
		panel.setBorder(CommonBorder.withEmptyBorder("Connection Parameters", 4));
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
	
}
