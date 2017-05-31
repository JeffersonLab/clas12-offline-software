package cnuphys.ced.cedview.allpcal;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.PCALGeometry;

public class McHitDrawer extends PCALViewDrawer {

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	public McHitDrawer(PCALView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {


		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		_fbRects.clear();

		if (!_view.showMcTruth()) {
			return;
		}
		
		if (!_view.isSingleEventMode()) {
			return;
		}

		showGemcXYZHits(g, container);
	}

	// the actual hit drawing
	private void showGemcXYZHits(Graphics g, IContainer container) {

		double x[] = PCAL.avgX();
		double y[] = PCAL.avgY();
		double z[] = PCAL.avgZ();

		if ((x == null) || (y == null) || (z == null) || (x.length < 1)) {
			return;
		}

		double labXYZ[] = new double[3];
		Point pp = new Point();

		// should not be necessary but be safe
		int len = x.length;
		len = Math.min(len, y.length);
		len = Math.min(len, z.length);

		for (int hitIndex = 0; hitIndex < len; hitIndex++) {

			labXYZ[0] = x[hitIndex] / 10; // mm to cm
			labXYZ[1] = y[hitIndex] / 10;
			labXYZ[2] = z[hitIndex] / 10;
			Point3D clasP = new Point3D(labXYZ[0], labXYZ[1], labXYZ[2]);
			Point3D localP = new Point3D();
			PCALGeometry.getTransformations().clasToLocal(localP, clasP);
			int sector = GeometryManager.getSector(labXYZ[0], labXYZ[1]);
			localP.setZ(0);
			
			List<String> fbs = PCAL.gemcHitFeedback(hitIndex, 
					DataSupport.FB_CLAS_XYZ + DataSupport.FB_CLAS_RTP
					+ DataSupport.FB_LOCAL_XYZ
					+ DataSupport.FB_TOTEDEP, 
					PCALGeometry.getTransformations());

			// get the right item
			_view.getHexSectorItem(sector).ijkToScreen(container, localP, pp);

			FeedbackRect rr = new FeedbackRect(FeedbackRect.Dtype.PCAL, pp.x - 4, pp.y - 4, 8, 8,
					hitIndex, 0, fbs);
			_fbRects.addElement(rr);

			DataDrawSupport.drawGemcHit(g, pp);
		}
	}

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
	 */
	@Override
	public void feedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		if (_fbRects.isEmpty()) {
			return;
		}

		int strips[] = PCAL.strip();
		int views[] = PCAL.view();
		
		for (FeedbackRect rr : _fbRects) {
			boolean contains = rr.contains(screenPoint, feedbackStrings);
			if (contains && (rr.hitIndex >= 0)) {

				int hitIndex = rr.hitIndex;

				// additional feedback
				if (rr.type == FeedbackRect.Dtype.PCAL) {
					
					int strip = strips[hitIndex];
					int view = views[hitIndex];
					
					if ((strip > 0) && (view > 0)) {
						String s = DataSupport.trueColor + "Gemc Hit "
								+ " type "
								+ DataDrawSupport.EC_VIEW_NAMES[view]
								+ " strip " + strip;
						feedbackStrings.add(s);
					}
				}
				
				return;
			} //contains
		}

	}

}
