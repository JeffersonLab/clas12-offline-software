package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
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
import cnuphys.ced.event.data.BMT;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.BaseHit2;
import cnuphys.ced.event.data.BaseHit2List;
import cnuphys.ced.event.data.CND;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.bmt.BMTSectorItem;
import cnuphys.splot.plot.X11Colors;

public class CentralXYHitDrawer implements IDrawable {
	
	private static Color _baseColor = new Color(255, 0, 0, 60);


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

	/**
	 * Mouse over feedback
	 * @param container
	 * @param screenPoint
	 * @param worldPoint
	 * @param feedbackStrings
	 */
	public void feedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		for (FeedbackRect rr : _fbRects) {
			rr.contains(screenPoint, feedbackStrings);
		}
	}

	// only called in single event mode
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		drawBSTAccumulatedHits(g, container);
	//	drawMicroMegasAccumulateHitsHits(g, container);
		drawCTOFAccumulatedHits(g, container);
		drawCNDAccumulatedHits(g, container);
	}

	// draw accumulated BST hits (panels)
	private void drawCNDAccumulatedHits(Graphics g, IContainer container) {
		
	}

	
	// draw accumulated BST hits (panels)
	private void drawBSTAccumulatedHits(Graphics g, IContainer container) {
		// panels

		int medianHit = AccumulationManager.getInstance().getMedianBSTCount();

		// first index is layer 0..7, second is sector 0..23
		int bstData[][] = AccumulationManager.getInstance()
				.getAccumulatedBSTData();
		
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			for (int sect0 = 0; sect0 < BSTGeometry.sectorsPerSuperlayer[supl0]; sect0++) {
				BSTxyPanel panel = CentralXYView.getPanel(lay0 + 1, sect0 + 1);

				if (panel != null) {
					int hitCount = bstData[lay0][sect0];
					
//					System.err.println("BST layer: " + (lay0+1) +  " sect: " + (sect0+0) + "   hit count " + hitCount);

					double fract = _view.getMedianSetting()*(((double) hitCount) / (1 + medianHit));
					Color color = AccumulationManager.getInstance()
							.getColor(fract);
					_view.drawBSTPanel((Graphics2D) g, container, panel, color);

				}
				else {
//					System.err.println("Got a null panel in drawBSTAccumulatedHits.");
				}
			}
		}
	}
	
	// draw CTOF accumulated hits
	private void drawCTOFAccumulatedHits(Graphics g, IContainer container) {
		int medianHit = AccumulationManager.getInstance().getMedianCTOFCount();

		int ctofData[] = AccumulationManager.getInstance()
				.getAccumulatedCTOFData();
		
		for (int index = 0; index < 48; index++) {
			CTOFXYPolygon poly = _view.getCTOFPolygon(index+1);
			if (poly != null) {
				int hitCount = ctofData[index];
				
				double fract = _view.getMedianSetting()*(((double) hitCount) / (1 + medianHit));

				Color color = AccumulationManager.getInstance()
						.getColor(fract);
				
				poly.draw(g, container, index+1, color);
			}
		}
	}
	


	// only called in single event mode
	private void drawHitsSingleMode(Graphics g, IContainer container) {
		drawBSTHitsSingleMode(g, container);
		drawBMTHitsSingleMode(g, container);
		drawCTOFSingleHitsMode(g, container);
		drawCNDSingleHitsMode(g, container);
	}
	
	// draw CTOF hits
	private void drawCNDSingleHitsMode(Graphics g,
			IContainer container) {
		
		TdcAdcHitList hits = CND.getInstance().getHits();

		if ((hits != null) && !hits.isEmpty()) {
			for (TdcAdcHit hit : hits) {
				
				//adcs have order = 0, 1
				if (hit != null){
					
				//	System.err.println("ORDER: " + hit.order);
					
					int comp = 1 + (hit.order % 2);
					
					CNDXYPolygon poly = _view.getCNDPolygon(hit.sector, hit.layer, comp);
					if (poly != null) {
						
						
						if (hit.order < 2) {  //adc
							Color color = hits.adcColor(hit);
							poly.draw(g, container, color, Color.black);
						}
						else {
							poly.draw(g, container, null, Color.red);
						} //tdc
						
					}
				}
			}
		}
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
						poly.draw(g, container, hit.component, color);
						
//						g.setColor(color);
//						g.fillPolygon(poly);
//						g.setColor(Color.black);
//						g.drawPolygon(poly);
					}
				}
			}
		}
		
	}

	// draw BMT hits
	private void drawBMTHitsSingleMode(Graphics g,
			IContainer container) {
		
		Point pp = new Point();
		Point2D.Double wp = new Point2D.Double();
		
		AdcHitList hits = BMT.getInstance().getADCHits();
		if ((hits != null) && !hits.isEmpty()) {
			
//			Shape oldClip = g.getClip();
			Graphics2D g2 = (Graphics2D) g;
			
			for (AdcHit hit : hits) {
				if (hit != null) {
					BMTSectorItem bmtItem = _view.getBMTSectorItem(hit.sector, hit.layer);

					Polygon poly = bmtItem.getStripPolygon(container, hit.component);
					if (poly != null) {
						g.setColor(X11Colors.getX11Color("tan"));
						g.fillPolygon(poly);
						g.setColor(Color.red);
						g.drawPolygon(poly);
					}
				}
			}
		} // adc hits not empty

		if (_view.showADCHits() && (hits != null) && !hits.isEmpty()) {
			for (AdcHit hit : hits) {
				if (hit != null) {
					BMTSectorItem bmtItem = _view.getBMTSectorItem(hit.sector, hit.layer);

					if (bmtItem.isZLayer()) {

						Color color = hits.adcColor(hit);

						double phi = BMTGeometry.getGeometry().CRZStrip_GetPhi(hit.sector, hit.layer, hit.component);

						double rad = bmtItem.getInnerRadius() + BMTSectorItem.FAKEWIDTH / 2.;
						wp.x = rad * Math.cos(phi);
						wp.y = rad * Math.sin(phi);
						container.worldToLocal(pp, wp);
						hit.setLocation(pp);
						DataDrawSupport.drawAdcHit(g, pp, color);

						// SymbolDraw.drawX(g2, pp.x, pp.y, 4, Color.black);
					}
				}
			}
		} // adc hits not empty

		if (_view.showReconHits()) {
			BaseHit2List recHits = BMT.getInstance().getRecHits();
			if (recHits != null) {
				// System.err.println("NUM RECON BMT HITS: " +
				// recHits.getCount());

				if (recHits.count() > 0) {
					for (BaseHit2 bhit2 : recHits) {
						BMTSectorItem bmtItem = _view.getBMTSectorItem(bhit2.sector, bhit2.layer);
						if (bmtItem.isZLayer()) {

							double phi = BMTGeometry.getGeometry().CRZStrip_GetPhi(bhit2.sector, bhit2.layer,
									bhit2.component);

							double rad = bmtItem.getInnerRadius() + BMTSectorItem.FAKEWIDTH / 2.;
							wp.x = rad * Math.cos(phi);
							wp.y = rad * Math.sin(phi);
							container.worldToLocal(pp, wp);

							bhit2.setLocation(pp);
							DataDrawSupport.drawReconHit(g, pp);
						}
					}
				}
			}
		}
	}

	
	// draw BST hits single event mode
	private void drawBSTHitsSingleMode(Graphics g, IContainer container) {
		
		AdcHitList hits = BST.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			
//			Shape oldClip = g.getClip();
			Graphics2D g2 = (Graphics2D) g;

			for (AdcHit hit : hits) {
				if (hit != null) {
					
					BSTxyPanel panel = CentralXYView.getPanel(hit.layer,
							hit.sector);
					
					if (panel != null) {
						_view.drawBSTPanel(g2, container, panel, _baseColor);
						_view.drawBSTPanel(g2, container, panel, hits.adcColor(hit));
	//					_view.drawBSTPanel(g2, container, panel, Color.red);
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
