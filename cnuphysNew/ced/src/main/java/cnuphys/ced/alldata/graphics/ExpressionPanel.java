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

public class ExpressionPanel extends JPanel implements ActionListener, ListSelectionListener {

	private ExpressionTableScrollPane _scrollPane;
	
	//remove bindings from the table
	private JButton _remove;
	
	public ExpressionPanel(int selectionMode) {
		setLayout (new BorderLayout(4, 4));
		
		_scrollPane = new ExpressionTableScrollPane("Expressions", selectionMode);
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
	 * @return the underlying expression table.
	 */
	public ExpressionTable getTable() {
		return _scrollPane.getTable();
	}

	/**
	 * Simple accessor for underlying model.
	 * 
	 * @return The underlying table model.
	 */
	public ExpressionTableModel getExpressionModel() {
		return _scrollPane.getExpressionModel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _remove) {
			int rows[] = getTable().getSelectedRows();
			if ((rows != null) && (rows.length > 0)) {
				int len = rows.length;
				Vector<NamedExpression> nbv = new Vector<NamedExpression>();
				for (int i = 0; i < len; i++) {
					nbv.add(getExpressionModel().getNamedExpression(rows[i]));
				}
				
				getExpressionModel().getData().removeAll(nbv);
				getExpressionModel().fireTableRowsDeleted(0, 0);

			}
		}
		
	}
	
	/**
	 * Remove a row from the table
	 * @param row the zero based row
	 * @return the removed NamedExpression, or null
	 */
	public NamedExpression removeRow(int row) {
		NamedExpression ne = getExpressionModel().getNamedExpression(row);
		if (ne != null) {
			getExpressionModel().getData().remove(ne);
			getExpressionModel().fireTableRowsDeleted(0, 0);
		}
		return ne;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int row = getTable().getSelectedRow();
			_remove.setEnabled(row >= 0);
		}
	}


}
