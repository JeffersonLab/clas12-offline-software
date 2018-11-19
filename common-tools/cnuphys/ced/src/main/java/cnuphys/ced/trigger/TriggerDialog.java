package cnuphys.ced.trigger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.ced.frame.Ced;

public class TriggerDialog extends JDialog implements ActionListener  {
	
	private static final int NUMTODISPLAY = 3;  //1..3

	
	//singleton
	private static TriggerDialog _instance;
	
	//first display
	private static boolean first = true;
	
	//trigger panels
	private TriggerPanel triggerPanels[];
	
	//is the trigger active
	private JCheckBox _triggerActiveCB;
	
	//the bit editor for the filter pattern
	private BitEditor _bitEditor;
	
	//private constructor
	private TriggerDialog() {
		super(Ced.getCed(), "Trigger Bits", false);
		setLayout(new BorderLayout(0, 0));
		add(createNorthComponent(), BorderLayout.NORTH);

		setResizable(false);
		
		setAlwaysOnTop(true);
		pack();
				
		DialogUtilities.upperRightComponent(this, 2, 0);
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 1, def.left+4, def.bottom+4,
				def.right+4);
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
		JPanel nPanel = new JPanel();
		nPanel.setLayout(new BorderLayout(0, 8));
		
		_triggerActiveCB = new JCheckBox("Trigger Filter Active", false);
		_triggerActiveCB.addActionListener(this);
		nPanel.add(_triggerActiveCB, BorderLayout.NORTH);
		
		triggerPanels = new TriggerPanel[NUMTODISPLAY];
		for (int i = 0; i < NUMTODISPLAY; i++) {
			triggerPanels[i] = new TriggerPanel();
		}
		
		JPanel panel = new JPanel();
	//	panel.setLayout(new GridLayout(NUMTODISPLAY, 1, 4, 4));
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//add the editor
		
		_bitEditor = new BitEditor();
		panel.add(_bitEditor);
		panel.add(Box.createVerticalStrut(10));
		
		for (int i = 0; i < NUMTODISPLAY; i++) {
			panel.add(triggerPanels[i]);
			
			if (i < 2) {
				panel.add(Box.createVerticalStrut(10));
			}

		}
		
		nPanel.add(panel, BorderLayout.CENTER);
		return nPanel;
	}

	
	/**
	 * Set the data for the current event
	 * @param id the ids from the trigger bank
	 * @param trigger the words from the trigger bank
	 */
	public void setCurrentEvent(int[] id, int[] trigger) {
		
		if (triggerPanels == null) {
			return;
		}
		
		for (int i = 0; i < NUMTODISPLAY; i++) {
			triggerPanels[i].setBits(0,  0);
		}

	
		if ((id != null) && (trigger != null))  {
			
			int len = Math.min(id.length, trigger.length);
			len = Math.min(len,  NUMTODISPLAY);
			
			for (int i = 0; i < len; i++) {
				triggerPanels[i].setBits(id[i],  trigger[i]);
			}
			
		}
	}
	
	public JCheckBox getTriggerActiveCheckBox() {
		return _triggerActiveCB;
	}
	
	public void fixActiveBox() {
		boolean selected = _triggerActiveCB.isSelected();
		if (isActive() != selected) {
			_triggerActiveCB.setSelected(!selected);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == _triggerActiveCB) {
			TriggerManager.getInstance().setFilterActive(_triggerActiveCB.isSelected());
			Ced.getCed().fixEventFilteringLabel();
		}
		
	}

}
