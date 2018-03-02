package cnuphys.ced.trigger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.ced.frame.Ced;

public class TriggerDialog extends JDialog  {
	
	private static final int NUMTODISPLAY = 3;  //1..3

	
	//singleton
	private static TriggerDialog _instance;
	
	//first display
	private static boolean first = true;
	
	//trigger panels
	private TriggerPanel triggerPanels[];
	

	//private constructor
	private TriggerDialog() {
		super(Ced.getCed(), "Trigger Bits", false);
		setLayout(new BorderLayout(0, 0));
		add(createNorthComponent(), BorderLayout.NORTH);

		
		setAlwaysOnTop(true);
		pack();
				
		DialogUtilities.upperRightComponent(this, 2, 0);
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 1, def.left, def.bottom,
				def.right);
	}

	
	@Override
	public void setVisible(boolean visible) {
		if (first) {
			Ced ced = Ced.getCed();
			if (ced != null) {
				
				Insets insets = ced.getInsets();
				int tbHeight = insets.top;
				
				
				JMenuBar mb = ced.getJMenuBar();
				
				int mbHeight = mb.getBounds().height;
				
				Point loc = getLocation();
				loc.y += (tbHeight + mbHeight);
				setLocation(loc);
				loc = getLocation();
				
			}
			first = false;
		}
		super.setVisible(visible);
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
	 * Override to create the component that goes in the north.
	 * 
	 * @return the component that is placed in the north
	 */
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
