package cnuphys.cnf.alldata.graphics;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class SelectColumnsPanel extends JPanel implements ListSelectionListener {

	// list for all known banks
	private AllBanksList _blist;

	// list for corresponding columns
	private ColumnList _clist;

	// for the expression name
	private JTextField _expressionName;

	/**
	 * 
	 * @param label
	 * @param addExpressionTable
	 */
	public SelectColumnsPanel(String label) {
		setLayout(new BorderLayout(2, 4));
		addCenter(label);
	}


	/**
	 * Add a selection listener to the bank and column lists
	 * 
	 * @param lsl the selection listener
	 */
	public void addBankColumnListener(ListSelectionListener lsl) {
		_blist.addListSelectionListener(lsl);
		_clist.addListSelectionListener(lsl);
	}

	// add the center component
	private void addCenter(String label) {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 8, 8));
		_blist = new AllBanksList();
		_clist = new ColumnList(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		addBankColumnListener(this);

		p.add(_blist.getScrollPane());
		p.add(_clist.getScrollPane());
		p.setBorder(new CommonBorder(label));

		add(p, BorderLayout.CENTER);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		Object o = e.getSource();


		String bname = _blist.getSelectedValue();
		if ((o == _blist) && (bname != null)) {
			_clist.setList(_blist.getSelectedValue());
		}

	}
	
	/**
	 * Get the selected bank 
	 * @return the selected bank  (or <code>null</code>
	 */
	public String getSelectedBank() {
		return _blist.getSelectedValue();
	}
	
	/**
	 * Get the selected columns 
	 * @return a list of selected columns  (or <code>null</code>
	 */
	public List<String> getSelectedColumns() {
		List<String> slist = _clist.getSelectedValuesList();
		if ((slist == null) || slist.isEmpty()) {
			return null;
		}
		return slist;
	}

	/**
	 * Get the expression name
	 * 
	 * @return the expression name
	 */
	public String getExpressionName() {
		return (_expressionName == null) ? null : _expressionName.getText();
	}

	public static void main(String arg[]) {
		final JFrame frame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				System.exit(1);
			}

			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};

		frame.addWindowListener(windowAdapter);

		frame.setLayout(new BorderLayout());

//		HistoPanel hp = new HistoPanel();
		SelectPanel hp = new SelectPanel("Select", true);
		frame.add(hp);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});

	}
}