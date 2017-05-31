package cnuphys.ced.common;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;

public abstract class CedViewDrawer implements IDrawable {

	// convenient access to the event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	// the CedView being rendered.
	protected CedView _view;

	public CedViewDrawer(CedView view) {
		_view = view;
	}

	@Override
	public void setDirty(boolean dirty) {
	}

	@Override
	public void prepareForRemoval() {
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public abstract void draw(Graphics g, IContainer container);

	/**
	 * Use what was drawn to generate feedback strings
	 * 
	 * @param container
	 *            the drawing container
	 * @param screenPoint
	 *            the mouse location
	 * @param worldPoint
	 *            the corresponding world location
	 * @param feedbackStrings
	 *            add strings to this collection
	 * @param option
	 *            for whatever needed purpose
	 */
	public abstract void vdrawFeedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings, int option);
	
}
