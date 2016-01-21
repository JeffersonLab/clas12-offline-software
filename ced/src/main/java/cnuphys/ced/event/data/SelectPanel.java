package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.X11Colors;

public class SelectPanel extends JPanel implements ListSelectionListener {
	
	private BankList _blist;
	
	private ColumnList _clist;
	
	private JLabel _fullName;
	
	//related to expression table
	private ExpressionTableScrollPane _expressionScrollPane;
	private ExpressionTable _expressionTable;
	private DefaultListSelectionModel _expressionSelectionModel;

	public SelectPanel(String label, boolean addExpressionTable) {
		setLayout(new BorderLayout(2,4));
		addCenter(label);
//		addNorth(label);
		_fullName = new JLabel("");
		_fullName.setOpaque(true);
		_fullName.setBackground(Color.black);
		_fullName.setForeground(Color.cyan);
		add(_fullName, BorderLayout.SOUTH);
		
		if (addExpressionTable) {
			addEast();
		}
	}
	
	private void addEast() {
		JPanel eastPanel = new JPanel();
		
//		eastPanel.setBorder(new CommonBorder("Select an Expression"));
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
	
	public String getSelection() {
		return _fullName.getText();
	}
	
	public void addSelectionListener(ListSelectionListener lsl) {
		_blist.addListSelectionListener(lsl);
		_clist.addListSelectionListener(lsl);
	}
	
	//add the center component
	private void addCenter(String label) {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 8, 8));
		_blist = new BankList();
		_clist = new ColumnList();
		
		addSelectionListener(this);
		
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
		System.err.println("source = " + o.getClass().getName());

		// expression?
		if ((_expressionSelectionModel != null)
				&& (o == _expressionSelectionModel)) {
			NamedExpression ne = _expressionTable.getSelectedExpression();
			if (ne != null) {
				_clist.getSelectionModel().clearSelection();
				_blist.getSelectionModel().clearSelection();
			}
			return;
		}


		String bname = _blist.getSelectedValue();
		if ((o == _blist) && (bname != null)) {
			_clist.setList(_blist.getSelectedValue());
		}

		String cname = _clist.getSelectedValue();
		
		if ((bname == null) || (cname == null)) {
			_fullName.setText(null);
		}
		else {
			_fullName.setText(bname + "." + cname);	
			if (_expressionSelectionModel != null) {
				System.err.println("Clearing selection");
				_expressionSelectionModel.clearSelection();
			}
		}
		firePropertyChange("newname", "", _fullName.getText());
	}
	
	/**
	 * Get the full name
	 * @return the full name
	 */
	public String getFullName() {
		String fn = _fullName.getText();
		return fn;
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
		ScatterPanel hp = new ScatterPanel();
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
