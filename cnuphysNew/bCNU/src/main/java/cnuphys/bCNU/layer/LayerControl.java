package cnuphys.bCNU.layer;

import cnuphys.bCNU.drawable.DrawableChangeType;
import cnuphys.bCNU.drawable.DrawableList;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.drawable.IDrawableListener;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.view.BaseView;

public class LayerControl implements IDrawableListener {

	/**
	 * The container whose layers are being managed.
	 */
	private IContainer _container;

	/**
	 * Constructor for a layer controller.
	 * 
	 * @param container
	 *            the parent container.
	 */
	public LayerControl(IContainer container) {
		_container = container;
	}

	// logger
	private static Log log = Log.getInstance();

	/**
	 * Come here when the LAYERS have been changed--typically added.
	 */
	@Override
	public void drawableChanged(DrawableList list, IDrawable drawable,
			DrawableChangeType type) {

		LogicalLayer layer = (drawable == null) ? null
				: (LogicalLayer) drawable;
		String name = (layer == null) ? "null" : layer.getName();

		// some containers may not have views--i.e. on dialogs
		BaseView view = _container.getView();

		String viewName = view != null ? _container.getView().getName()
				: "(non-view)";

		switch (type) {
		case ADDED:
			log.info("View: " + viewName + "  Layer added: " + name);
			break;

		case DESELECTED:
			log.info("View: " + viewName + "  Layer deselected: " + name);
			break;

		case DOUBLECLICKED:
			log.info("View: " + viewName + "  Layer doubleclicked: " + name);
			break;

		case HIDDEN:
			log.info("View: " + viewName + "  Layer hidden: " + name);
			break;

		case MODIFIED:
			log.info("View: " + viewName + "  Layer modified: " + name);
			break;

		case MOVED:
			log.info("View: " + viewName + "  Layer moved: " + name);
			break;

		case REMOVED:
			log.info("View: " + viewName + "  Layer removed: " + name);
			break;

		case RESIZED:
			log.info("View: " + viewName + "  Layer resized: " + name);
			break;

		case ROTATED:
			log.info("View: " + viewName + "  Layer rotated: " + name);
			break;

		case SELECTED:
			log.info("View: " + viewName + "  Layer selected: " + name);
			break;

		case SHOWN:
			log.info("View: " + viewName + "  Layer shown: " + name);
			break;

		case LISTCLEARED:
			log.info("View: " + viewName + "  Layer list cleared: " + name);
			break;

		case LISTHIDDEN:
			log.info("View: " + viewName + "  Layer list hidden: " + name);
			break;

		case LISTSHOWN:
			log.info("View: " + viewName + "  Layer list shown: " + name);
			break;
		}
	}

}
