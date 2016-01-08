package cnuphys.ced.event.data;

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
import cnuphys.splot.pdata.DataSet;

public class DefineScatterDialog extends JDialog implements ActionListener, PropertyChangeListener {
	private JButton _okButton;
	private JButton _cancelButton;
	private int _reason = DialogUtilities.CANCEL_RESPONSE;
	private ScatterPanel _scatterPanel;

	public DefineScatterDialog() {
		setTitle("Define a Scatter Plot");
		setModal(true);
		setLayout(new BorderLayout(4, 4));
		
		_scatterPanel = new ScatterPanel();
		add(_scatterPanel, BorderLayout.CENTER);
		
		SelectPanel sp[] = _scatterPanel.getScatterPanel();
		sp[0].addPropertyChangeListener(this);
		sp[1].addPropertyChangeListener(this);
		
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

	/**
	 * Get the reason the dialog closed
	 * @return the reason the dialog closed
	 */
	public int getReason() {
		return _reason;
	}
	
	/**
	 * Return a DataSet ready for filling if the user hit ok
	 * @return a DataSet or <code>null</code>.
	 */
	public DataSet getDataSeta() {
		if (_reason == DialogUtilities.OK_RESPONSE) {
			return _scatterPanel.createDataSet();
		}
		return null;
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		String prop = evt.getPropertyName();
		
		if (prop.equals("newname")) {
			SelectPanel sp[] = _scatterPanel.getScatterPanel();
			if ((o == sp[0]) || (o == sp[1])) {
				String xfn = sp[0].getFullName();
				String yfn = sp[1].getFullName();
				boolean xvalid = ((xfn != null) && (xfn.length() > 4) && xfn.contains(":") && xfn.contains("."));
				boolean yvalid = ((yfn != null) && (yfn.length() > 4) && yfn.contains(":") && yfn.contains("."));
				_okButton.setEnabled(xvalid && yvalid);
			}
		}

	}
	
	public static void main(String arg[]) {
		DefineScatterDialog dialog = new DefineScatterDialog();
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
