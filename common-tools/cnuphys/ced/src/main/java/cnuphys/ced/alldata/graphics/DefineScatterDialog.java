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
import cnuphys.splot.pdata.DataSet;

/**
 * Used to define a scatter plot
 * @author heddle
 *
 */
public class DefineScatterDialog extends JDialog implements ActionListener, PropertyChangeListener {
	private JButton _okButton;
	private JButton _cancelButton;
	private int _reason = DialogUtilities.CANCEL_RESPONSE;
	private ScatterPanel _scatterPanel;

	public DefineScatterDialog() {
		setTitle("Define a Scatter Plot");
		setModal(true);
		setLayout(new BorderLayout(4, 4));
		setIconImage(ImageManager.cnuIcon.getImage());
		
		_scatterPanel = new ScatterPanel();
		add(_scatterPanel, BorderLayout.CENTER);
		
		SelectPanel sp[] = _scatterPanel.getScatterPanels();
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
		if ((prop.equals("newname")) || (prop.equals("expression"))) {

			SelectPanel sp[] = _scatterPanel.getScatterPanels();
			boolean xvalid = false;
			boolean yvalid = false;

			String xname = sp[0].getFullColumnName();
			if (DataManager.getInstance().validColumnName(xname)) {
				xvalid = true;
			} else {
				xname = sp[0].getExpressionName();
				xvalid = ((xname != null) && !xname.isEmpty());
			}

			String yname = sp[1].getFullColumnName();
			if (DataManager.getInstance().validColumnName(yname)) {
				yvalid = true;
			} else {
				yname = sp[1].getExpressionName();
				yvalid = ((yname != null) && !yname.isEmpty());
			}

			_okButton.setEnabled(xvalid && yvalid);
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
