package cnuphys.ced.fastmc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cnuphys.ced.frame.Ced;
import cnuphys.splot.edit.DialogUtilities;

public class AcceptanceDialog extends JDialog implements ActionListener {
	

	//buttons
	private JButton _addButton;    //add a condition
	private JButton _deleteButton; //delete a condition
	private JButton _applyButton;  //apply the changes
	private JButton _closeButton;  //close the dialog
	
	private ConditionList _conditionList;  //list of conditions

	public AcceptanceDialog() {
		super(Ced.getCed(), "Acceptance", false);
		prepare();
		pack();
		DialogUtilities.centerDialog(this);
	}
	
	private void prepare() {
		setLayout(new BorderLayout(4, 4));
		safeAdd(createNorthComponent(), BorderLayout.NORTH);
		safeAdd(createSouthComponent(), BorderLayout.SOUTH);
		safeAdd(createEastComponent(), BorderLayout.EAST);
		safeAdd(createWestComponent(), BorderLayout.WEST);
		safeAdd(createCenterComponent(), BorderLayout.CENTER);
	}
	
	private void safeAdd(Component comp, String constraint) {
		if (comp != null) {
			add(comp, constraint);
		}
	}
	
	/**
	 *create the component that goes in the north.
	 * 
	 * @return the component that is placed in the east
	 */
	protected Component createNorthComponent() {
		return null;
	}

	/**
	 *create the component that goes in the south.
	 * 
	 * @return the component that is placed in the east
	 */
	protected Component createSouthComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		_addButton = makeButton("Add", panel, true);
		_deleteButton = makeButton("Delete", panel, false);
		_applyButton = makeButton("Apply", panel, true);
		_closeButton = makeButton("Close", panel, true);
		return panel;
	}


	//make a button
	private JButton makeButton(String label, JPanel p, boolean enabled) {
		JButton b = new JButton(label);
		b.addActionListener(this);
		b.setEnabled(enabled);
		p.add(b);
		return b;
	}
	
	/**
	 *create the component that goes in the east.
	 * 
	 * @return the component that is placed in the east
	 */
	protected Component createEastComponent() {
		return null;
	}

	/**
	 * create the component that goes in the west.
	 * 
	 * @return the component that is placed in the west.
	 */
	protected Component createWestComponent() {
		return null;
	}

	/**
	 * create the component that goes in the center. Usually this is
	 * the "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	protected Component createCenterComponent() {
		_conditionList = new ConditionList();
		return _conditionList;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == _addButton) {
			
		}
		else if (o == _deleteButton) {
			
		}
		else if (o == _applyButton) {
			
		}
		else if (o == _closeButton) {
			setVisible(false);
		}
		
	}

}
