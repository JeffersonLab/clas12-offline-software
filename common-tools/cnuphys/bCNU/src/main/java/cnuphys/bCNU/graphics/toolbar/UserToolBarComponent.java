package cnuphys.bCNU.graphics.toolbar;

import java.awt.Graphics;

import javax.swing.JComponent;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;

@SuppressWarnings("serial")
public class UserToolBarComponent extends JComponent {

	private IDrawable _userDraw;

	private IContainer _container;

	public UserToolBarComponent(IContainer container) {
		_container = container;
	}

	@Override
	public void paintComponent(Graphics g) {

		if (_userDraw != null) {
			_userDraw.draw(g, _container);
		}
	}

	/**
	 * Set the user draw for this user toolbar component.
	 * 
	 * @param userDraw
	 *            the userDraw to set
	 */
	public void setUserDraw(IDrawable userDraw) {
		_userDraw = userDraw;
	}

}
