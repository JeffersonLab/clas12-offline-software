package cnuphys.ced.fastmc;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class ConditionList extends JList<ConditionPanel> {
	
	private JScrollPane _scrollPane;
	
	private DefaultListModel<ConditionPanel> _model = new DefaultListModel();
	
	private Dimension _size = new Dimension(400, 400);

	public ConditionList() {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setModel(_model);
		_scrollPane = new JScrollPane(this);
		_scrollPane.setPreferredSize(_size);
		_scrollPane.setBorder(new CommonBorder("Acceptance Conditions"));
		
		_model.addElement(ConditionPanel.electron());
	}

	/**
	 * Get the scroll pane
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		return _scrollPane;
	}
}
