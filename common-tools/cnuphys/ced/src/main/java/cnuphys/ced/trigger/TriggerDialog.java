package cnuphys.ced.trigger;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;

public class TriggerDialog extends SimpleDialog  {
	
	private static final int NUMTODISPLAY = 1;  //1..3

	// button names for closeout
	private static String[] closeoutButtons = {};
	
	// reason the dialog was closed
	private int _reason = DialogUtilities.CANCEL_RESPONSE;
	
	//singleton
	private static TriggerDialog _instance;
	
	//trigger panels
	private TriggerPanel triggerPanels[];
	

	//private constructor
	private TriggerDialog() {
		super("Trigger Bits", false, closeoutButtons);
	
		checkButtons();
		setAlwaysOnTop(true);
		pack();
		DialogUtilities.upperRightComponent(this, 2, 50);
	}
	
	public static void showDialog() {
		getInstance().setVisible(true);
	}
	
	public static TriggerDialog getInstance() {
		if (_instance == null) {
			_instance = new TriggerDialog();
		}
		return _instance;
	}
	
	/**
	 * Check the enabled state of all the buttons. Default implementation does
	 * nothing.
	 */
	@Override
	protected void checkButtons() {
	}
	
	/**
	 * Override to create the component that goes in the north.
	 * 
	 * @return the component that is placed in the north
	 */
	@Override
	protected Component createNorthComponent() {
		triggerPanels = new TriggerPanel[NUMTODISPLAY];
		for (int i = 0; i < NUMTODISPLAY; i++) {
			triggerPanels[i] = new TriggerPanel();
		}
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(NUMTODISPLAY, 1, 4, 4));
		
		
		for (int i = 0; i < NUMTODISPLAY; i++) {
			panel.add(triggerPanels[i]);
		}
		return panel;
	}

	
	public void setCurrentEvent(int[] id, int[] trigger) {
		
		if (triggerPanels == null) {
			return;
		}
		
		for (int i = 0; i < NUMTODISPLAY; i++) {
			triggerPanels[i].set(0,  0);
		}

	
		if ((id != null) && (trigger != null))  {
			
			int len = Math.min(id.length, trigger.length);
			len = Math.min(len,  NUMTODISPLAY);
			
			for (int i = 0; i < len; i++) {
				triggerPanels[i].set(id[i],  trigger[i]);
			}
			
		}
	}

}
