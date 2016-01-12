package cnuphys.ced.cedview.sectorview;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.FTOF;

public class ReconDrawer extends SectorViewDrawer {

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	public ReconDrawer(SectorView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		_fbRects.clear();
		
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (!_view.isSingleEventMode()) {
			return;
		}

		// FTOF
		if (_view.showFTOFReconHits()) {
			drawFTOFReconHits(g, container);
		}
	}

	// draw FTOF reconstructed hits

	// draw reconstructed hits
	private void drawFTOFReconHits(Graphics g, IContainer container) {

		// arggh this sector array is zero based
		int sector[] = FTOF.reconSector();
		int panel[] = FTOF.reconPanel();
		int paddle[] = FTOF.reconPaddle();
		float recX[] = FTOF.reconX();
		float recY[] = FTOF.reconY();
		float recZ[] = FTOF.reconZ();

		// _view.getWorldFromDetectorXYZ(100 * v3d[0], 100 *v3d[1],
		// 100 * v3d[2], wp);

		if ((recX != null) && (recY != null) && (recZ != null)) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			int hitCount = recX.length;
			// System.err.println("Drawing " + hitCount + " ftof recons hits");
			for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
				int sect = sector[hitIndex] + 1; // 1-based
				if (_view.isSectorOnView(sect)) {

					_view.getWorldFromLabXYZ(recX[hitIndex], recY[hitIndex], recZ[hitIndex],
							wp);
					
					
					// _view.sectorToWorld(wp, sectXYZ, sect);
					container.worldToLocal(pp, wp);

					String s1 = "$Orange Red$" + vecStr("FTOF hit (lab)",
							recX[hitIndex], recY[hitIndex], recZ[hitIndex]);
					String s2 = "$Orange Red$FTOF panel: "
							+ FTOF.panelNames[panel[hitIndex] - 1]
							+ " paddle: " + (paddle[hitIndex] + 1);

					container.worldToLocal(pp, wp);
					FeedbackRect fbr = new FeedbackRect(FeedbackRect.Dtype.FTOF, pp.x - 4, pp.y - 4, 8,
							8, hitIndex, panel[hitIndex], s1, s2);
					_fbRects.add(fbr);

					DataDrawSupport.drawReconHit(g, pp);
				}
			}
		}
	}


	/**
	 * See if we are on a feedback rect
	 * 
	 * @param container the drawing container
	 * @param screenPoint the mouse location
	 * @param option 0 for hit based, 1 for time based
	 * @return the FeedbackRect, or <code>null</code>
	 */
	public FeedbackRect getFeedbackRect(IContainer container, Point screenPoint,
			int option) {
		if (_fbRects.isEmpty()) {
			return null;
		}

		for (FeedbackRect rr : _fbRects) {
			if ((rr.option == option) && rr.contains(screenPoint)) {
				return rr;
			}
		}
		return null;
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

		if (_fbRects.isEmpty()) {
			return;
		}

		for (FeedbackRect rr : _fbRects) {
			if ((rr.option == option)
					&& rr.contains(screenPoint, feedbackStrings)) {
				return;
			}
		}

	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return vecStr(prompt, vx, vy, vz, 2);
	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz,
			int ndig) {
		return prompt + " (" + DoubleFormat.doubleFormat(vx, ndig) + ", "
				+ DoubleFormat.doubleFormat(vy, ndig) + ", "
				+ DoubleFormat.doubleFormat(vz, ndig) + ")";
	}

}
