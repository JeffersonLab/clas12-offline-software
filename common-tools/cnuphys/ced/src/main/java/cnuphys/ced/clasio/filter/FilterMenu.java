package cnuphys.ced.clasio.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class FilterMenu extends JMenu implements ActionListener {
	
	private JCheckBoxMenuItem _activeItem;
	
	private JMenuItem _editItem;
	
	private AEventFilter _filter;
	
	public FilterMenu(AEventFilter filter) {
		super(filter.getName());
		_filter =filter;
		
		_activeItem = new JCheckBoxMenuItem("Active", filter.isActive());
		_editItem = new JMenuItem("Edit...");
		
		_activeItem.addActionListener(this);
		_editItem.addActionListener(this);
		
		add(_activeItem);
		add(_editItem);
		
	}
	
	/**
	 * Get the menu item that set the active state
	 * @return the menu item that set the active state
	 */
	public JMenuItem getActiveItem() {
		return _activeItem;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == _activeItem) {
			_filter.toggleActiveState();
		}
		if (source == _editItem) {
			_filter.edit();
		}

		
	}

}
