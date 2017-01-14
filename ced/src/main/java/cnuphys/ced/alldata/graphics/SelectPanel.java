package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.X11Colors;

public class SelectPanel extends JPanel implements ListSelectionListener {
	
	//list for all known banks
	private AllBanksList _blist;
	
	//list for corresponding columns
	private ColumnList _clist;
	
	//for the column name
	private JTextField _columnName;
	
	//for the expression name
	private JTextField _expressionName;
	
	//related to expression table
	private ExpressionTableScrollPane _expressionScrollPane;
	private ExpressionTable _expressionTable;
	private DefaultListSelectionModel _expressionSelectionModel;

	/**
	 * 
	 * @param label
	 * @param addExpressionTable
	 */
	public SelectPanel(String label, boolean addExpressionTable) {
		setLayout(new BorderLayout(2,4));
		addCenter(label);
		
		boolean expNameToo = addExpressionTable && DefinitionManager.getInstance().haveExpressions();
		
		addSouth(expNameToo);
		if (expNameToo) {
			addEast();
		}
	}
	
	//add the south panel
	private void addSouth(boolean expNameToo) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));

		panel.add(new JLabel("Column"));

		_columnName = new JTextField(null, 30);
		_columnName.setEditable(true);
		_columnName.setBackground(Color.black);
		_columnName.setForeground(Color.cyan);
		panel.add(_columnName, BorderLayout.SOUTH);

		if (expNameToo) {
			panel.add(Box.createHorizontalStrut(50));

			panel.add(new JLabel("Expression"));
			_expressionName = new JTextField(null, 20);
			_expressionName.setEditable(true);
			_expressionName.setBackground(Color.black);
			_expressionName.setForeground(Color.cyan);
			panel.add(_expressionName, BorderLayout.SOUTH);
		}

		add(panel, BorderLayout.SOUTH);
	}
	
	//add the east panel which contains the expression table
	private void addEast() {
		JPanel eastPanel = new JPanel();
		JLabel orLab = new JLabel("  or  ");
		orLab.setOpaque(true);
		orLab.setBackground(X11Colors.getX11Color("sea green"));
		orLab.setForeground(Color.white);
		orLab.setBorder(BorderFactory.createEtchedBorder());
		
		eastPanel.add(orLab, BorderLayout.WEST);
		
		_expressionScrollPane = new ExpressionTableScrollPane("Expressions", ListSelectionModel.SINGLE_SELECTION);
		_expressionScrollPane.setBorder(new CommonBorder("Select an Expression"));
		_expressionTable = _expressionScrollPane.getTable();
		_expressionSelectionModel = (DefaultListSelectionModel) _expressionTable.getSelectionModel();
		_expressionSelectionModel.addListSelectionListener(this);
		
		eastPanel.add(_expressionScrollPane, BorderLayout.CENTER);
		add(eastPanel, BorderLayout.EAST);
		
	}
	
	/**
	 * Add a selection listener to the bank and column lists
	 * @param lsl the selection listener
	 */
	public void addBankColumnListener(ListSelectionListener lsl) {
		_blist.addListSelectionListener(lsl);
		_clist.addListSelectionListener(lsl);
	}
	
	//add the center component
	private void addCenter(String label) {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 8, 8));
		_blist = new AllBanksList();
		_clist = new ColumnList();
		
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

		// expression?
		if ((_expressionSelectionModel != null)
				&& (o == _expressionSelectionModel)) {
			NamedExpression ne = _expressionTable.getSelectedExpression();
			if (ne != null) {
				_clist.getSelectionModel().clearSelection();
				_blist.getSelectionModel().clearSelection();
				_expressionName.setText(ne.getExpressionName());
				firePropertyChange("expression", "", ne.getExpressionName());

			}
			else {
				_expressionName.setText(null);
			}
			return;
		}


		String bname = _blist.getSelectedValue();
		if ((o == _blist) && (bname != null)) {
			_clist.setList(_blist.getSelectedValue());
		}

		String cname = _clist.getSelectedValue();
		
		if ((bname == null) || (cname == null)) {
			_columnName.setText(null);
		}
		else {
			_columnName.setText(bname + "." + cname);	
			if (_expressionSelectionModel != null) {
				System.err.println("Clearing selection");
				_expressionSelectionModel.clearSelection();
			}
		}
		firePropertyChange("newname", "", _columnName.getText());
	}
	
	/**
	 * Get the full column name
	 * @return the full column name
	 */
	public String getFullColumnName() {
		return (_columnName == null) ? null : _columnName.getText();
	}
	
	/**
	 * Get the full bank-column name. If that is no good,
	 * get the expression name.
	 * @return the name
	 */
	public String getResolvedName() {
		String name = getFullColumnName();
		if ((name == null) || name.isEmpty()) {
			name =  getExpressionName();
			if ((name == null) || name.isEmpty()) {
				name = "???";
			}
		}
		return name;
	}
	
	/**
	 * Get the expression name
	 * @return the expression name
	 */
	public String getExpressionName() {
		return (_expressionName == null) ? null : _expressionName.getText();
	}
	
	public static void main(String arg[]) {
		DefinitionManager.getInstance().addExpression("eee", "whatever");
		DefinitionManager.getInstance().addExpression("ddd", "whatever");
		DefinitionManager.getInstance().addExpression("bbb", "whatever");
		DefinitionManager.getInstance().addExpression("ccc", "whatever");
		DefinitionManager.getInstance().addExpression("aaa", "whatever");
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
