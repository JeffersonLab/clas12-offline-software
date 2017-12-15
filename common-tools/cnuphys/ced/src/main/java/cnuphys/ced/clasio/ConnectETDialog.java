package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.graphics.ImageManager;

public class ConnectETDialog extends SimpleDialog {
	
	public static ImageIcon _etIcon;


	// button names for closeout
	private static String[] closeoutButtons = {"Connect", "Cancel"};
	
	
	// reason the dialog was closed
	private int _reason = DialogUtilities.CANCEL_RESPONSE;

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
		return label;
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
		panel.setLayout(new BorderLayout(8,8));
		
		JPanel npanel = new JPanel();
		npanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		
		npanel.add(new JLabel("CLON cpu: "));

		
		JPanel spanel = new JPanel();
		spanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		
		spanel.add(new JLabel("File: "));
		
		panel.add(npanel, BorderLayout.NORTH);
		panel.add(spanel, BorderLayout.SOUTH);
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
