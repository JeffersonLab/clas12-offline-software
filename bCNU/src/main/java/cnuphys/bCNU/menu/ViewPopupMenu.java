package cnuphys.bCNU.menu;

import javax.swing.JPopupMenu;

import cnuphys.bCNU.view.BaseView;

public class ViewPopupMenu extends JPopupMenu {

	// base view owner
	private BaseView _view;

	// quickzoom menu
	private QuickZoomMenu _quickZoomMenu;

	public ViewPopupMenu(BaseView view) {
		super("Options");
		setLightWeightPopupEnabled(false);
		_view = view;

		// quick zoom menu
		_quickZoomMenu = new QuickZoomMenu(view);
		add(_quickZoomMenu);
	}

	/**
	 * Add a quick zoom to the view's popup
	 * 
	 * @param title the title of the quickzoom
	 * @param xmin min world x
	 * @param ymin min world y
	 * @param xmax min
	 * @param ymax
	 */
	public void addQuickZoom(String title, final double xmin, final double ymin,
			final double xmax, final double ymax) {
		_quickZoomMenu.add(
				new QuickZoomMenuItem(title, _view, xmin, ymin, xmax, ymax));
	}

}
