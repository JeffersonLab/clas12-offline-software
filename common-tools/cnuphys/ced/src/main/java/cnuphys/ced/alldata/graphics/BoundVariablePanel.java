package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class BoundVariablePanel extends JPanel implements ActionListener, ListSelectionListener {

	private BoundVariableTableScrollPane _scrollPane;
	
	//remove bindings from the table
	private JButton _remove;
	
	public BoundVariablePanel() {
		setLayout (new BorderLayout(4, 4));
		
		_scrollPane = new BoundVariableTableScrollPane("Bound Variables");
		add(_scrollPane, BorderLayout.CENTER);
		
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		_remove = new JButton(" Remove ");
		_remove.addActionListener(this);
		
		_remove.setEnabled(false);
		sp.add(_remove);
		add(sp, BorderLayout.SOUTH);
		
		getTable().getSelectionModel().addListSelectionListener(this);

	}
	
	/**
	 * Accessor for the underlying table.
	 * 
	 * @return the underlying bound variable table.
	 */
	public BoundVariableTable getTable() {
		return _scrollPane.getTable();
	}

	/**
	 * Simple accessor for underlying model.
	 * 
	 * @return The underlying table model.
	 */
	public BoundVariableTableModel getBoundVariableModel() {
		return _scrollPane.getBoundVariableModel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _remove) {
			int rows[] = getTable().getSelectedRows();
			if ((rows != null) && (rows.length > 0)) {
				int len = rows.length;
				Vector<NameBinding> nbv = new Vector<NameBinding>();
				for (int i = 0; i < len; i++) {
					nbv.add(getBoundVariableModel().getNameBinding(rows[i]));
				}
				
				getBoundVariableModel().getData().removeAll(nbv);
				getBoundVariableModel().fireTableRowsDeleted(0, 0);

			}
		}
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int row = getTable().getSelectedRow();
			_remove.setEnabled(row >= 0);
		}
	}


}
