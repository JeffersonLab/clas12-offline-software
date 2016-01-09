package cnuphys.ced.cedview.bst;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.micromegas.MicroMegasSector;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.splot.style.SymbolDraw;

public class BSTxyHitDrawer implements IDrawable {

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	private boolean _visible = true;

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	// owner view
	private BSTxyView _view;

	public BSTxyHitDrawer(BSTxyView view) {
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
		return "BSTxyHitDrawer";
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

	//draw accumulated hits (panels)
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		// panels

		int maxHit = AccumulationManager.getInstance().getMaxDgtzBstCount();
		if (maxHit < 1) {
			return;
		}

		// first index is layer 0..7, second is sector 0..23
		int bstData[][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzBstData();
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0/2;
			for (int sect0 = 0; sect0 < BSTGeometry.sectorsPerSuperlayer[supl0]; sect0++) {
				BSTxyPanel panel = BSTxyView.getPanel(lay0 + 1, sect0 + 1);

				if (panel != null) {
					int hitCount = bstData[lay0][sect0];
					
					double fract;
					if (_view.isSimpleAccumulatedMode()) {
						fract = ((double) hitCount) / maxHit;
					}
					else {
						fract = Math.log(hitCount+1.)/Math.log(maxHit+1.);
					}

					Color color = AccumulationManager.getInstance().getColor(fract);
					_view.drawSVTPanel((Graphics2D) g, container, panel, color);

				}
			}
		}
	}

	// only called in single event mode
	private void drawHitsSingleMode(Graphics g, IContainer container) {
		drawBSTHitsSingleMode(g, container);
		drawMicroMegasHitsSingleMode(g, container);
	}

	// draw micromegas hits
	private void drawMicroMegasHitsSingleMode(Graphics g,
			IContainer container) {

		int sect[] = ColumnData.getIntArray("BMT::dgtz.sector");
		int hitCount = (sect == null) ? 0 : sect.length;
		if (hitCount > 0) {

			int layer[] = ColumnData.getIntArray("BMT::dgtz.layer");

			for (int hit = 0; hit < hitCount; hit++) {
				int geoSector = MicroMegasSector
						.geoSectorFromDataSector(sect[hit]);
				MicroMegasSector mms = _view.getMicroMegasSector(geoSector,
						layer[hit]);
				if (mms != null) {
					FeedbackRect fbr = mms.drawHit(g, container, hit,
							X11Colors.getX11Color("lawn green"), Color.black);

					if (fbr != null) {
						_fbRects.add(fbr);
					}
				}
			}
		}
	}

	// draw gemc simulated hits single event mode
	private void drawBSTHitsSingleMode(Graphics g, IContainer container) {

		int hitCount = BST.hitCount();
		if (hitCount > 0) {
			
			int bstsector[] = BST.sector();
			int bstlayer[] = BST.layer();
			int bststrip[] = BST.strip();

			Shape oldClip = g.getClip();
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// panels
			for (int i = 0; i < hitCount; i++) {
				BSTxyPanel panel = BSTxyView.getPanel(bstlayer[i],
						bstsector[i]);
				if (panel != null) {
					_view.drawSVTPanel(g2, container, panel, Color.red);
				}
			} // for on hits

			// desginate the hits by strip midpoints?
			if (_view.showStripMidpoints()) {
				// System.err.println("DISPLAY MIDPOINTS");
				Point pp = new Point();
				for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
					// covert all to zero based indices
					int sector = bstsector[hitIndex] - 1;
					int complayer = bstlayer[hitIndex];
					int superlayer = (complayer - 1) / 2;
					int layer = (complayer - 1) % 2;
					int strip = bststrip[hitIndex] - 1;

					if ((strip > 255) || (strip < 0)) {
						Log.getInstance()
								.warning("In BST dgtz data, bad strip Id:"
										+ bststrip[hitIndex]);
					}
					else {
						// System.err.println("Drawing strip midpoint ");
						Point2D.Double wp = BSTGeometry.getStripMidpoint(sector,
								superlayer, layer, strip);

						container.worldToLocal(pp, 10 * wp.x, 10 * wp.y);

						SymbolDraw.drawUpTriangle(g2, pp.x, pp.y, 3,
								X11Colors.getX11Color("Dark Green"),
								X11Colors.getX11Color("Aquamarine"));
					}

					// System.out.println("sect " + sector + " supl " +
					// superlayer + " lay " + layer + " strip " + strip);
				}
			}

			// draw GEMC nearest x and y

			if (_view.showMcTruth()) {
		        int pid[] = BST.pid();
				double avgX[] = BST.avgX();
				double avgY[] = BST.avgY();
				if (avgX != null) {

					Rectangle sr = container.getInsetRectangle();
					g2.clipRect(sr.x, sr.y, sr.width, sr.height);

					Point p1 = new Point();
					Point2D.Double wp1 = new Point2D.Double();
					Color default_fc = Color.red;

					Stroke oldStroke = g2.getStroke();
					g2.setStroke(CedXYView.stroke);

					for (int i = 0; i < avgX.length; i++) {
						Color fc = default_fc;
						if (pid != null) {
							LundId lid = LundSupport.getInstance()
									.get(pid[i]);
							if (lid != null) {
								fc = lid.getStyle().getFillColor();
							}
						}
						g2.setColor(fc);

						wp1.setLocation(avgX[i],
								avgY[i]);
						container.worldToLocal(p1, wp1);

						DataDrawSupport.drawGemcHit(g, p1);
					}
					g2.setStroke(oldStroke);
				} //avgx != null
			} //show mc truth

			g.setClip(oldClip);
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
