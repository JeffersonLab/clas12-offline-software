package cnuphys.ced.cedview.bst;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.geometry.BSTxyPanel;

public class BSTzHitDrawer implements IDrawable {

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	private boolean _visible = true;

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	// owner view
	private BSTzView _view;

	public BSTzHitDrawer(BSTzView view) {
		_view = view;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public void setVisible(boolean visible) {
		_visible = visible;

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
		return "BSTzHitDrawer";
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (_eventManager.isAccumulating()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		_fbRects.clear();

		if (_view.isSingleEventMode()) {
			if (_view.showMcTruth()) {
				drawMCTruth(g, container);
			}

			drawHitsSingleMode(g, container);
		}
		else {
			drawAccumulatedHits(g, container);
		}

		g2.setClip(oldClip);

	}

	public void feedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		for (FeedbackRect rr : _fbRects) {
			rr.contains(screenPoint, feedbackStrings);
		}
	}

	// draw accumulated hits (panels)
	private void drawAccumulatedHits(Graphics g, IContainer container) {
	}

	// only called in single event mode
	private void drawHitsSingleMode(Graphics g, IContainer container) {
		drawBSTHitsSingleMode(g, container);
		drawMicroMegasHitsSingleMode(g, container);
	}

	// draw micromegas hits
	private void drawMicroMegasHitsSingleMode(Graphics g,
			IContainer container) {
	}

	// draw gemc simulated hits single event mode
	private void drawBSTHitsSingleMode(Graphics g, IContainer container) {

		BSTDataContainer bstData = _eventManager.getBSTData();

		int hitCount = bstData.getHitCount(0);
		if (hitCount > 0) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// panels
			for (int i = 0; i < bstData.getHitCount(0); i++) {
				BSTxyPanel panel = BSTxyView.getPanel(bstData.bst_dgtz_layer[i],
						bstData.bst_dgtz_sector[i]);
				if (panel != null) {
					for (int zopt = 0; zopt < 3; zopt++) {
						if (panel.hit[zopt]) {
							_view.drawSVTStrip(g2, container, Color.red,
									bstData.bst_dgtz_sector[i],
									bstData.bst_dgtz_layer[i],
									bstData.bst_dgtz_strip[i]);
						}
					}
				}
			} // for on hits

		} // hotcount > 0

	}

	private void drawMCTruth(Graphics g, IContainer container) {
	}

	@Override
	public void setDirty(boolean dirty) {
	}

	@Override
	public void prepareForRemoval() {
	}

}
