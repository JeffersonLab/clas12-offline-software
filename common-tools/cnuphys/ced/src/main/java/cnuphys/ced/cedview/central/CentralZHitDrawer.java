package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.SVT;
import cnuphys.ced.geometry.SVTGeometry;
import cnuphys.ced.geometry.SVTxyPanel;

public class CentralZHitDrawer implements IDrawable {

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	private boolean _visible = true;

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	// owner view
	private CentralZView _view;

	public CentralZHitDrawer(CentralZView view) {
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
		return "CentralZHitDrawer";
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
		} else {
			drawAccumulatedHits(g, container);
		}

		g2.setClip(oldClip);

	}

	public void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		for (FeedbackRect rr : _fbRects) {
			rr.contains(screenPoint, feedbackStrings);
		}
	}

	// draw accumulated hits (panels)
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		drawSVTHitsAccumulatedMode(g, container);
		drawMicroMegasHitsAccumulatedMode(g, container);
	}

	private void drawSVTHitsAccumulatedMode(Graphics g, IContainer container) {

		int maxHit = AccumulationManager.getInstance().getMaxFullSVTCount();
		if (maxHit < 1) {
			return;
		}

		// first index is layer 0..7, second is sector 0..23
		int bstFullData[][][] = AccumulationManager.getInstance().getAccumulatedSVTFullData();
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			for (int sect0 = 0; sect0 < SVTGeometry.sectorsPerSuperlayer[supl0]; sect0++) {
				for (int strip0 = 0; strip0 < 255; strip0++) {
					int hitCount = bstFullData[lay0][sect0][strip0];

					if (hitCount > 1) {

						double fract;
						if (_view.isSimpleAccumulatedMode()) {
							fract = ((double) hitCount) / maxHit;
						} else {
							fract = Math.log(hitCount + 1.) / Math.log(maxHit + 1.);
						}

						Color color = AccumulationManager.getInstance().getColor(fract);
						_view.drawSVTStrip((Graphics2D) g, container, color, sect0 + 1, lay0 + 1, strip0 + 1);
					}

				}
			}
		}
	}

	private void drawMicroMegasHitsAccumulatedMode(Graphics g, IContainer container) {
	}

	// only called in single event mode
	private void drawHitsSingleMode(Graphics g, IContainer container) {
		drawSVTHitsSingleMode(g, container);
		drawMicroMegasHitsSingleMode(g, container);
	}

	// draw micromegas hits
	private void drawMicroMegasHitsSingleMode(Graphics g, IContainer container) {
	}

	// draw gemc simulated hits single event mode
	private void drawSVTHitsSingleMode(Graphics g, IContainer container) {
		
		AdcHitList hits = SVT.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			
//			Shape oldClip = g.getClip();
			Graphics2D g2 = (Graphics2D) g;

			for (AdcHit hit : hits) {
				if (hit != null) {
					//HACK GEO SECTOR DOESN"T MATCH REAL
					//TODO Undo hack when geometry fixed
					
					int superlayer = (hit.layer - 1) / 2;
	                int numSect = SVTGeometry.sectorsPerSuperlayer[superlayer];
					int hackSect = (hit.sector + (numSect/2)) % numSect;
					if (hackSect == 0) hackSect = numSect;

					
					SVTxyPanel panel = CentralXYView.getPanel(hit.layer, hackSect);
					if (panel != null) {
						_view.drawSVTStrip(g2, container, Color.red, hit.sector, hit.layer, hit.component);					}
					else {
						System.err.println("null SVTZ panel");
					}

				}
			}
		}

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
