package cnuphys.ced.cedview.projecteddc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCHit;
import cnuphys.ced.event.data.DCHitList;
import cnuphys.ced.frame.CedColors;

public class ReconDrawer extends ProjectedViewDrawer {

	
	/**
	 * Reconstructed hits drawer
	 * @param view
	 */
	public ReconDrawer(ProjectedDCView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (!_view.isSingleEventMode()) {
			return;
		}
		
		// DC HB and TB Hits
		drawDCDOCA(g, container);


	}
	// draw reconstructed DC hit based hits
	private void drawDCDOCA(Graphics g, IContainer container) {
		if (_view.showHB()) {
			drawDCHitList(g, container, CedColors.HB_COLOR, DC.getInstance().getHBHits(), false);
		}
		if (_view.showTB()) {
			drawDCHitList(g, container, CedColors.TB_COLOR, DC.getInstance().getTBHits(), true);
		}
	}

	
	//draw a reconstructed hit list
	private void drawDCHitList(Graphics g, IContainer container, Color fillColor, DCHitList hits, boolean isTimeBased) {
		if ((hits == null) || hits.isEmpty()) {
			return;
		}
				
		
		for (DCHit hit : hits) {
			if (_view.getSector() == hit.sector) {
				_view.drawDCHit(g, container, fillColor, Color.black, hit, isTimeBased);
			}
		}


	}
		

	/**
	 * Use what was drawn to generate feedback strings
	 * 
	 * @param container the drawing container
	 * @param screenPoint the mouse location
	 * @param worldPoint the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 * @param option 0 for hit based, 1 for time based
	 */
	@Override
	public void vdrawFeedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings,
			int option) {
		
		
		//DC HB Recon Hits
		if (_view.showDCHBHits()) {
			DCHitList hits = DC.getInstance().getHBHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (DCHit hit : hits) {
					if (_view.getSector() == hit.sector) {
						if (hit.contains(screenPoint)) {
							hit.getFeedbackStrings("HB", feedbackStrings);
							return;
						}
					}
				}
			}
		}

	}

}
