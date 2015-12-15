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
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Histo2DData;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.BMTDataContainer;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.micromegas.MicroMegasSector;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

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

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		_fbRects.clear();

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			drawAccumulatedHits(g, container);
		}

		else {
			if (_view.showMcTruth()) {
				drawMCTruth(g, container);
			}

			drawHits(g, container);
		}
		
		g2.setClip(oldClip);

	}

	public void feedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		for (FeedbackRect rr : _fbRects) {
			rr.contains(screenPoint, feedbackStrings);
		}
	}

	private void drawAccumulatedHits(Graphics g, IContainer container) {
		drawGEMCHitsAccumulatedMode(g, container);
	}

	private void drawHits(Graphics g, IContainer container) {
		drawBSTHits(g, container);
		drawMicroMegasHits(g, container);
	}

	// draw gemc simulated hits
	private void drawBSTHits(Graphics g, IContainer container) {
		if (_view.getMode() == CedView.Mode.SINGLE_EVENT) {
			drawBSTHitsSingleMode(g, container);
		}
		else {
		}
	}

	// draw micromegas hits
	private void drawMicroMegasHits(Graphics g, IContainer container) {

		if (_view.getMode() == CedView.Mode.SINGLE_EVENT) {
			BMTDataContainer bmtData = _eventManager.getBMTData();

			// System.err.println("BMTDATAContainer: " + bmtData);
			int hitCount = bmtData.getHitCount(0);
			if (hitCount > 0) {
				// System.err.println("BMT HIT COUNT: " + hitCount);

				int sect[] = bmtData.bmt_dgtz_sector;
				int layer[] = bmtData.bmt_dgtz_layer;

				for (int hit = 0; hit < hitCount; hit++) {
					int geoSector = MicroMegasSector
							.geoSectorFromDataSector(sect[hit]);
					MicroMegasSector mms = _view.getMicroMegasSector(geoSector,
							layer[hit]);
					if (mms != null) {
						FeedbackRect fbr = mms.drawHit(g, container, bmtData, hit,
								X11Colors.getX11Color("lawn green"),
								Color.black);
						
						if (fbr != null) {
							_fbRects.add(fbr);
						}
					}
				}
			}
		}
	}
	

	// draw gemc simulated hits single event mode
	private void drawBSTHitsSingleMode(Graphics g, IContainer container) {

		BSTDataContainer bstData = _eventManager.getBSTData();

		int hitCount = bstData.getHitCount(0);
		if (hitCount > 0) {

			Shape oldClip = g.getClip();
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// panels
			for (int i = 0; i < bstData.getHitCount(0); i++) {
				BSTxyPanel panel = _view.getPanel(bstData.bst_dgtz_layer[i],
						bstData.bst_dgtz_sector[i]);
				if (panel != null) {
					_view.drawSVTPanel(g2, container, panel, Color.red);
				}
			} // for on hits

			// draw the actual hits

			if (_view.displayDgtzCrosses()) {
			}
			else {
				// System.err.println("DISPLAY MIDPOINTS");
				Point pp = new Point();
				for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
					// covert all to zero based indices
					int sector = bstData.bst_dgtz_sector[hitIndex] - 1;
					int complayer = bstData.bst_dgtz_layer[hitIndex];
					int superlayer = (complayer - 1) / 2;
					int layer = (complayer - 1) % 2;
					int strip = bstData.bst_dgtz_strip[hitIndex] - 1;

					if ((strip > 255) || (strip < 0)) {
						Log.getInstance()
								.warning("In BST dgtz data, bad strip Id:"
										+ bstData.bst_dgtz_strip[hitIndex]);
					}
					else {
						// System.err.println("Drawing strip midpoint ");
						Point2D.Double wp = BSTGeometry.getStripMidpoint(sector,
								superlayer, layer, strip);

						container.worldToLocal(pp, 10 * wp.x, 10 * wp.y);

						drawCross(g, pp.x, pp.y + 1,
								X11Colors.getX11Color("Aquamarine"));
						drawCross(g, pp.x, pp.y,
								X11Colors.getX11Color("Dark Green"));
					}

					// System.out.println("sect " + sector + " supl " +
					// superlayer + " lay " + layer + " strip " + strip);
				}
			}

			// draw GEMC nearest x and y

			if ((bstData.bst_true_avgX != null) && _view.showMcTruth()) {

				Rectangle sr = container.getInsetRectangle();
				g2.clipRect(sr.x, sr.y, sr.width, sr.height);

				Point p1 = new Point();
				Point2D.Double wp1 = new Point2D.Double();
				Color default_fc = Color.red;

				Stroke oldStroke = g2.getStroke();
				g2.setStroke(CedXYView.stroke);

				for (int i = 0; i < bstData.bst_true_avgX.length; i++) {
					Color fc = default_fc;
					if (bstData.bst_true_pid != null) {
						LundId lid = LundSupport.getInstance()
								.get(bstData.bst_true_pid[i]);
						if (lid != null) {
							fc = lid.getStyle().getFillColor();
						}
					}
					g2.setColor(fc);

					wp1.setLocation(bstData.bst_true_avgX[i],
							bstData.bst_true_avgY[i]);
					container.worldToLocal(p1, wp1);

					// draw an x
					g2.drawLine(p1.x - 3, p1.y - 3, p1.x + 3, p1.y + 3);
					g2.drawLine(p1.x + 3, p1.y - 3, p1.x - 3, p1.y + 3);
				}
				g2.setStroke(oldStroke);
			}

			g.setClip(oldClip);
		} // hotcount > 0

	}

	// draw gemc simulated hits accumulated mode
	private void drawGEMCHitsAccumulatedMode(Graphics g, IContainer container) {
		Histo2DData bstXYData = AccumulationManager.getInstance()
				.getBSTXYGemcAccumulatedData();
		if (bstXYData != null) {
			// System.err.println("Good count: " + bstXYData.getGoodCount());
			// System.err.println("Bad count: " +
			// bstXYData.getOutOfRangeCount());
			// System.err.println("Max count: " + bstXYData.getMaxZ());

			long counts[][] = bstXYData.getCounts();
			if (counts != null) {
				Rectangle2D.Double wr = new Rectangle2D.Double();
				Rectangle r = new Rectangle();

				double maxBinCount = bstXYData.getMaxZ();

				for (int i = 0; i < bstXYData.getNumberBinsX(); i++) {
					double x1 = bstXYData.getBinMinX(i);
					double x2 = bstXYData.getBinMaxX(i);

					for (int j = 0; j < bstXYData.getNumberBinsY(); j++) {
						if (counts[i][j] > 0) {
							double y1 = bstXYData.getBinMinY(j);
							double y2 = bstXYData.getBinMaxY(j);

							wr.setFrame(x1, y1, x2 - x1, y2 - y1);

							container.worldToLocal(r, wr);

							double fract = ((counts[i][j])) / maxBinCount;
							Color color = AccumulationManager
									.getColorScaleModel().getColor(fract);

							g.setColor(color);
							g.fillRect(r.x, r.y, r.width, r.height);
						}

					}
				}
			} // counts != null

		}
	}

	// draw a cross
	private void drawCross(Graphics g, int x, int y, Color color) {
		int len = 5;

		g.setColor(CedXYView.TRANS2);
		g.fillOval(x - len, y - len, 2 * len, 2 * len);
		g.setColor(color);
		g.drawLine(x - len, y, x + len, y);
		g.drawLine(x, y - len, x, y + len);
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
