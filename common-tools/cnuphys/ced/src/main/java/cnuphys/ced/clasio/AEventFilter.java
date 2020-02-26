package cnuphys.ced.clasio;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.frame.Ced;

public abstract class AEventFilter implements IEventFilter {
	
	//a name for the filter
	private String _name = "???";
	
	//the active flag
	private boolean _isActive;

	//used primarily for event filter menu
	protected JMenuItem _menuComponent;
	
	public AEventFilter() {
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				toggleActiveState();
			}
			
		};
		
		_menuComponent = new JMenuItem();
		_menuComponent.addActionListener(al);
		
//		_menuLabel.setFont(Fonts.defaultBoldFont);
		_menuComponent.setOpaque(true);
	}

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
	 * Get the menu component
	 * @return the menu component
	 */
	@Override
	public JComponent getMenuComponent() {
		return _menuComponent;
	}
	
	//fix the label
	private void fixMenuComponent() {
		if (isActive()) {
			_menuComponent.setBackground(Color.black);
			_menuComponent.setForeground(Color.red);
			_menuComponent.setText("  " + getName() + " [Filter is Active]  ");
		}
		else {
			_menuComponent.setBackground(X11Colors.getX11Color("Alice Blue"));
			_menuComponent.setForeground(X11Colors.getX11Color("Dark Blue"));
			_menuComponent.setText("  " + getName() + " [Filter is Inactive]  ");
		}
	}

}
