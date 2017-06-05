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
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.BMT;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.SVT;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.SVTGeometry;
import cnuphys.ced.geometry.SVTxyPanel;
import cnuphys.ced.geometry.bmt.BMTSectorItem;

public class CentralXYHitDrawer implements IDrawable {

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	private boolean _visible = true;

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	// owner view
	private CentralXYView _view;

	public CentralXYHitDrawer(CentralXYView view) {
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
		return "CentralXYHitDrawer";
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

	// only called in single event mode
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		drawSVTAccumulatedHits(g, container);
	//	drawMicroMegasAccumulateHitsHits(g, container);
		drawCTOFAccumulatedHits(g, container);
	}

	
	// draw accumulated SVT hits (panels)
	private void drawSVTAccumulatedHits(Graphics g, IContainer container) {
		// panels

		int maxHit = AccumulationManager.getInstance().getMaxSVTCount();
		if (maxHit < 1) {
			return;
		}

		// first index is layer 0..7, second is sector 0..23
		int bstData[][] = AccumulationManager.getInstance()
				.getAccumulatedSVTData();
		
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			for (int sect0 = 0; sect0 < SVTGeometry.sectorsPerSuperlayer[supl0]; sect0++) {
				SVTxyPanel panel = CentralXYView.getPanel(lay0 + 1, sect0 + 1);

				if (panel != null) {
					int hitCount = bstData[lay0][sect0];
					
//					System.err.println("SVT layer: " + (lay0+1) +  " sect: " + (sect0+0) + "   hit count " + hitCount);

					double fract;
					if (_view.isSimpleAccumulatedMode()) {
						fract = ((double) hitCount) / maxHit;
					}
					else {
						fract = Math.log(hitCount + 1.) / Math.log(maxHit + 1.);
					}

					Color color = AccumulationManager.getInstance()
							.getColor(fract);
					_view.drawSVTPanel((Graphics2D) g, container, panel, color);

				}
				else {
					System.err.println("Got a null panel in drawSVTAccumulatedHits.");
				}
			}
		}
	}
	
	// draw CTOF accumulated hits
	private void drawCTOFAccumulatedHits(Graphics g, IContainer container) {
		int maxHit = AccumulationManager.getInstance().getMaxCTOFCount();
		if (maxHit < 1) {
			return;
		}

		int ctofData[] = AccumulationManager.getInstance()
				.getAccumulatedCTOFData();
		
		for (int index = 0; index < 48; index++) {
			CTOFXYPolygon poly = _view.getCTOFPolygon(index+1);
			if (poly != null) {
				int hitCount = ctofData[index];
				
				double fract;
				if (_view.isSimpleAccumulatedMode()) {
					fract = ((double) hitCount) / maxHit;
				}
				else {
					fract = Math.log(hitCount + 1.) / Math.log(maxHit + 1.);
				}

				Color color = AccumulationManager.getInstance()
						.getColor(fract);
				g.setColor(color);
				g.fillPolygon(poly);
				g.setColor(Color.black);
				g.drawPolygon(poly);
			}
		}
	}
	


	// only called in single event mode
	private void drawHitsSingleMode(Graphics g, IContainer container) {
		drawSVTHitsSingleMode(g, container);
		drawMicroMegasHitsSingleMode(g, container);
		drawCTOFSingleHitsMode(g, container);
	}
	
	// draw CTOF hits
	private void drawCTOFSingleHitsMode(Graphics g,
			IContainer container) {
		
		TdcAdcHitList hits = CTOF.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (TdcAdcHit hit : hits) {
				if (hit != null) {
					CTOFXYPolygon poly = _view.getCTOFPolygon(hit.component);
					if (poly != null) {
						Color color = hits.adcColor(hit);
						g.setColor(color);
						g.fillPolygon(poly);
						g.setColor(Color.black);
						g.drawPolygon(poly);
					}
				}
			}
		}
		
	}

	// draw micromegas hits
	private void drawMicroMegasHitsSingleMode(Graphics g,
			IContainer container) {
//
//		int hitCount = BMT.hitCount();
//		if (hitCount > 0) {
//			int sect[] = BMT.sector();
//			int layer[] = BMT.layer();
//
//			for (int hit = 0; hit < hitCount; hit++) {
//				int geoSector = MicroMegasSector
//						.geoSectorFromDataSector(sect[hit]);
//				MicroMegasSector mms = _view.getMicroMegasSector(geoSector,
//						layer[hit]);
//				if (mms != null) {
//					FeedbackRect fbr = mms.drawHit(g, container, hit,
//							X11Colors.getX11Color("lawn green"), Color.black);
//
//					if (fbr != null) {
//						_fbRects.add(fbr);
//					}
//				}
//			} // for
//		} // hitcount > 0
	}

	
	private Color baseColor = new Color(255, 0, 0, 60);
	// draw gemc simulated hits single event mode
	private void drawSVTHitsSingleMode(Graphics g, IContainer container) {
		
		AdcHitList hits = SVT.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			
//			Shape oldClip = g.getClip();
			Graphics2D g2 = (Graphics2D) g;

			for (AdcHit hit : hits) {
				if (hit != null) {
					
					SVTxyPanel panel = CentralXYView.getPanel(hit.layer,
							hit.sector);
					
					if (panel != null) {
						_view.drawSVTPanel(g2, container, panel, baseColor);
						_view.drawSVTPanel(g2, container, panel, hits.adcColor(hit));
	//					_view.drawSVTPanel(g2, container, panel, Color.red);
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
