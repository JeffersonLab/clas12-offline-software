package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class BoundVariablePanel extends JPanel implements ActionListener {

	private BoundVariableTableScrollPane _scrollPane;
	
	private JButton _remove;
	
	public BoundVariablePanel() {
		setLayout (new BorderLayout(4, 4));
		
		_scrollPane = new BoundVariableTableScrollPane("Bound Variables");
		add(_scrollPane, BorderLayout.CENTER);
		
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		_remove = new JButton(" Remove ");
		_remove.addActionListener(this);
		sp.add(_remove);
		add(sp, BorderLayout.SOUTH);

	}
	
	/**
	 * Accessor for the underlying table.
	 * 
	 * @return the underlying cut table.
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
			
		}
		
	}
}
