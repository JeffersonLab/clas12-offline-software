package cnuphys.cnf.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.cnf.frame.Def;
import cnuphys.splot.plot.GraphicsUtilities;

public class ColumnsDialog extends JDialog implements ListSelectionListener {
	
	//the buttons
	private JButton _okButton;
	private JButton _cancelButton;
	
	//the reason
	private int _reason;
	
	//selection panel
	private SelectColumnsPanel _columnsPanel;

	public ColumnsDialog(String title) {
		super(Def.getFrame(), title, true);
		
		setLayout(new BorderLayout(4, 4));
		setup();
		setIconImage(ImageManager.cnuIcon.getImage());

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		};
		addWindowListener(wa);

		pack();
		GraphicsUtilities.centerComponent(this);
	}
	
	
	private void setup() {
		addCenter();
		addSouth();
	}
	
	private void addCenter() {
		_columnsPanel = new SelectColumnsPanel("Choose a single bank, then multiple columns");
		_columnsPanel.addBankColumnListener(this);
		add(_columnsPanel, BorderLayout.CENTER);
		
	}
	
	//close the dialog
	private void doClose(int reason) {
		_reason = reason;
		
		setVisible(false);
	}
	
	/**
	 * Get the reason the dialog closed
	 * @return DialogUtilities.OK_RESPONSE or DialogUtilities.CANCEL_RESPONSE
	 */
	public int getReason() {
		return _reason;
	}
	
	private void addSouth() {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 4));
		
		_okButton = new JButton("  OK  ");
		//use lambda for action
		_okButton.addActionListener(e -> doClose(DialogUtilities.OK_RESPONSE));
		_okButton.setEnabled(false);

		
		_cancelButton = new JButton("Cancel");
		//use lambda for action
		_cancelButton.addActionListener(e -> doClose(DialogUtilities.CANCEL_RESPONSE));

		sp.add(_okButton);
		sp.add(_cancelButton);
		add(sp, BorderLayout.SOUTH);
		
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		boolean haveBank = (_columnsPanel.getSelectedBank() != null);
		boolean haveColumn = (_columnsPanel.getSelectedColumns() != null);
		
		_okButton.setEnabled(haveBank && haveColumn);
	}
	
	/**
	 * Get the selected bank 
	 * @return the selected bank  (or <code>null</code>
	 */
	public String getSelectedBank() {
		return _columnsPanel.getSelectedBank();
	}
	
	/**
	 * Get the selected columns 
	 * @return a list of selected columns  (or <code>null</code>
	 */
	public List<String> getSelectedColumns() {
		return _columnsPanel.getSelectedColumns();
	}


}
