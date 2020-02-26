package cnuphys.ced.common;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.ECAL;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.HTCC;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.geometry.GeometryManager;

public abstract class AMcHitDrawer extends CedViewDrawer {

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	public AMcHitDrawer(CedView view) {
		super(view);
	}
	
	@Override
	public void draw(Graphics g, IContainer container) {

		_fbRects.clear();

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (!_view.showMcTruth()) {
			return;
		}

		if (!_view.isSingleEventMode()) {
			return;
		}

		drawGemCXYZHits_DC(g, container);
		drawGemCXYZHits_FTOF(g, container);
		drawGemCXYZHits_HTCC(g, container);
		
	}
	
	protected void drawGemCXYZHits_HTCC(Graphics g, IContainer container) {
		showGemcXYZHits(g, container, FeedbackRect.Dtype.HTCC, 
				HTCC.avgX(), 
				HTCC.avgY(), 
				HTCC.avgZ(), 
				0);
	}
	

	protected void drawGemCXYZHits_DC(Graphics g, IContainer container) {
//		showGemcXYZHits(g, container, FeedbackRect.Dtype.DC, 
//				DC.avgX(), 
//				DC.avgY(), 
//				DC.avgZ(), 
//				0);
	}
	
	protected void drawGemCXYZHits_FTOF(Graphics g, IContainer container) {
		showGemcXYZHits(g, container, FeedbackRect.Dtype.FTOF, 
				FTOF.getInstance().avgX(FTOF.PANEL_1A),
				FTOF.getInstance().avgY(FTOF.PANEL_1A), 
				FTOF.getInstance().avgZ(FTOF.PANEL_1A),
				FTOF.PANEL_1A);

		showGemcXYZHits(g, container, FeedbackRect.Dtype.FTOF, 
				ColumnData.getDoubleArray("FTOF1B::true.avgX"),
				ColumnData.getDoubleArray("FTOF1B::true.avgY"), 
				ColumnData.getDoubleArray("FTOF1B::true.avgZ"), 
				FTOF.PANEL_1B);

		showGemcXYZHits(g, container, FeedbackRect.Dtype.FTOF, 
				ColumnData.getDoubleArray("FTOF2B::true.avgX"),
				ColumnData.getDoubleArray("FTOF2B::true.avgY"), 
				ColumnData.getDoubleArray("FTOF2B::true.avgZ"), 
				FTOF.PANEL_2);
	}

	protected void showGemcXYZHits(Graphics g, IContainer container,
			FeedbackRect.Dtype dtype,
			double x[], double y[], double z[],
			int option) {

		
		if ((x == null) || (y == null) || (z == null) || (x.length < 1)) {
			return;
		}

		double labXYZ[] = new double[3];
		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		// should not be necessary but be safe
		int len = x.length;
		len = Math.min(len, y.length);
		len = Math.min(len, z.length);

		for (int hitIndex = 0; hitIndex < len; hitIndex++) {
			labXYZ[0] = x[hitIndex] / 10; // mm to cm
			labXYZ[1] = y[hitIndex] / 10;
			labXYZ[2] = z[hitIndex] / 10;

			int sector = GeometryManager.getSector(x[hitIndex], y[hitIndex]);
			boolean showPoint = correctSector(sector);

			if (showPoint) {
				_view.projectClasToWorld(labXYZ[0], labXYZ[1], labXYZ[2],  _view.getProjectionPlane(), wp);
				container.worldToLocal(pp, wp);


				// display 1-based hit index

				String s = vecStr("Gemc Hit [" + (hitIndex + 1) + "] " + "  sect: " + sector + "  ",
						labXYZ[0], labXYZ[1], labXYZ[2]) + " cm";
				FeedbackRect rr = new FeedbackRect(dtype, pp.x - 4, pp.y - 4, 8, 8,
						hitIndex, option, s);
				_fbRects.addElement(rr);

				DataDrawSupport.drawGemcHit(g, pp);
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
	 */
	@Override
	public void vdrawFeedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings,
			int option) {

		if (_fbRects.isEmpty()) {
			return;
		}
		
		int ecstrip[] = ECAL.strip();
		int ecstack[] = ECAL.stack();
		int ecview[] = ECAL.view();
		
		int pcalstrip[] = PCAL.strip();
		int pcalstack[] = PCAL.stack(); //all 1s
		int pcalview[] = PCAL.view();
		

		for (FeedbackRect rr : _fbRects) {
			boolean contains = rr.contains(screenPoint, feedbackStrings);
			if (contains && (rr.hitIndex >= 0)) {

				int hitIndex = rr.hitIndex;
				
				int strip = -1;
				int stack = -1;
				int view = -1;

				if (rr.type == FeedbackRect.Dtype.EC) {
					strip = ecstrip[hitIndex];
					stack = ecstack[hitIndex];
					view = ecview[hitIndex];
				}
				else if (rr.type == FeedbackRect.Dtype.PCAL) {
					strip = pcalstrip[hitIndex];
					stack = pcalstack[hitIndex];
					view = pcalview[hitIndex];
				}
				
				if ((strip > 0) && (stack > 0) && (view > 0)) {
					String s = DataSupport.trueColor + "Gemc Hit "
							+ " plane "
							+ DataDrawSupport.EC_PLANE_NAMES[stack]
							+ " type " + DataDrawSupport.EC_VIEW_NAMES[view]
							+ " strip " + strip;
					feedbackStrings.add(s);
				}
				
				return;
			} //contains
		}

	}


	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return DataSupport.trueColor + prompt + " ("
				+ DoubleFormat.doubleFormat(vx, 3) + ", "
				+ DoubleFormat.doubleFormat(vy, 3) + ", "
				+ DoubleFormat.doubleFormat(vz, 3) + ")";
	}

	/**
	 * Check if this is the correct sector
	 * @param sector the sector we want to draw in
	 * @return <code>true</code> if we are in the correct sector, so go ahead and draw.
	 */
	protected abstract boolean correctSector(int sector);
}
