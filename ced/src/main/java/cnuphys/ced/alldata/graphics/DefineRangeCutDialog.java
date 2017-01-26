package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.ced.alldata.DataManager;

public class DefineRangeCutDialog extends JDialog implements ActionListener, PropertyChangeListener {
	
	private JButton _okButton;
	private JButton _cancelButton;
	private int _reason = DialogUtilities.CANCEL_RESPONSE;
	private RangeCutPanel _rangeCutPanel;

	public DefineRangeCutDialog() {
		setTitle("Define a Cut Range");
		setModal(true);
		setLayout(new BorderLayout(4, 4));
		setIconImage(ImageManager.cnuIcon.getImage());
		
		_rangeCutPanel = new RangeCutPanel();
		add(_rangeCutPanel, BorderLayout.CENTER);

		_rangeCutPanel.getSelectPanel().addPropertyChangeListener(this);
		
		addSouth();
		pack();
		DialogUtilities.centerDialog(this);
	}
	
	//add the buttons
	private void addSouth(){
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 200, 10));
		
		_okButton = new JButton("  OK  ");
		_okButton.setEnabled(false);
		_cancelButton = new JButton("Cancel");
		
		_okButton.addActionListener(this);
		_cancelButton.addActionListener(this);
		
		sp.add(_okButton);
		sp.add(_cancelButton);
		add(sp, BorderLayout.SOUTH);
	}
	
	/**
	 * Get the reason the dialog closed
	 * @return the reason the dialog closed
	 */
	public int getReason() {
		return _reason;
	}
	
	/**
	 * Return a RangeCut if the user hit ok
	 * @return a Range or <code>null</code>.
	 */
	public RangeCut getRangeCut() {
		if (_reason == DialogUtilities.OK_RESPONSE) {
			return _rangeCutPanel.getRangeCut();
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _okButton) {
			_reason = DialogUtilities.OK_RESPONSE;
			setVisible(false);
		}
		else if (o == _cancelButton) {
			_reason = DialogUtilities.CANCEL_RESPONSE;
			setVisible(false);
		}
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		Object o = evt.getSource();
		String prop = evt.getPropertyName();
		if (o == _rangeCutPanel.getSelectPanel()) {
			if (prop.equals("newname")) {
				String fn = (String) (evt.getNewValue());
				_okButton.setEnabled(DataManager.getInstance().validColumnName(fn));
			} //newname (column)
			else if (prop.equals("expression")) {
				_okButton.setEnabled(true);
			} //expression
		} 
	}
	
	public static void main(String arg[]) {
		DefineRangeCutDialog dialog = new DefineRangeCutDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			System.err.println("OK");
		}
		else {
			System.err.println("CANCEL");
		}
		
		System.exit(0);
	}

}