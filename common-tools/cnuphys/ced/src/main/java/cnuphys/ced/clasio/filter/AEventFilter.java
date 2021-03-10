package cnuphys.ced.clasio.filter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.frame.Ced;

/**
 * An abstract class for an event filter
 * 
 * @author heddle
 *
 */
public abstract class AEventFilter implements IEventFilter {

	// a name for the filter
	private String _name = "???";

	// the active flag
	private boolean _isActive;

	// used primarily for event filter menu
	protected JMenuItem _menuComponent;
	
	//optional editor
	protected AFilterDialog _editor;

	/**
	 * Created a filter that can not be edited,only activated and deactivated
	 */
	public AEventFilter() {
		this(false);
	}

	public AEventFilter(boolean editable) {

		if (editable) { //get a submenu
			_menuComponent = new FilterMenu(this);
		} else {
			ActionListener al = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					toggleActiveState();
				}

			};

			_menuComponent = new JMenuItem();
			_menuComponent.addActionListener(al);
		}

		_menuComponent.setOpaque(true);
	}

	/**
	 * Toggle the active state of the filter
	 */
	protected void toggleActiveState() {
		_isActive = !_isActive;
		fixMenuComponent();
		Ced.getCed().fixEventFilteringLabel();
	}

	@Override
	public void setActive(boolean active) {
		_isActive = active;
		fixMenuComponent();
	}

	@Override
	public boolean isActive() {
		return _isActive;
	}

	@Override
	public void setName(String name) {
		_name = name;
		fixMenuComponent();
	}

	@Override
	public String getName() {
		return _name;
	}
	
	/**
	 * Edit the filter
	 */
	@Override
	public void edit() {
	}
	
	/**
	 * Create the filter editor (optional)
	 * @return the filter editor
	 */
	public AFilterDialog createEditor() {
		return null;
	}

	/**
	 * Get the menu component
	 * 
	 * @return the menu component
	 */
	@Override
	public JComponent getMenuComponent() {
		return _menuComponent;
	}

	// fix the label
	private void fixMenuComponent() {
		if (isActive()) {
			_menuComponent.setBackground(Color.black);
			_menuComponent.setForeground(Color.red);
			_menuComponent.setText("  " + getName() + " [Filter is Active]  ");
		} else {
			_menuComponent.setBackground(X11Colors.getX11Color("Alice Blue"));
			_menuComponent.setForeground(X11Colors.getX11Color("Dark Blue"));
			_menuComponent.setText("  " + getName() + " [Filter is Inactive]  ");
		}
	}

}
