package cnuphys.ced.cedview.allpcal;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.ECDataContainer;
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
		
		if (_view.isAccumulatedMode()) {
			return;
		}

		ECDataContainer ecData = _eventManager.getECData();

		showGemcXYZHits(g, container, ecData);
	}

	// the actual hit drawing
	private void showGemcXYZHits(Graphics g, IContainer container,
			ECDataContainer data) {

		double x[] = data.pcal_true_avgX;
		double y[] = data.pcal_true_avgY;
		double z[] = data.pcal_true_avgZ;

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

			List<String> fbs = data.gemcHitFeedback(hitIndex,
					ADataContainer.FB_CLAS_XYZ + ADataContainer.FB_CLAS_RTP
							+ ADataContainer.FB_LOCAL_XYZ
							+ ADataContainer.FB_TOTEDEP,
					PCALGeometry.getTransformations(), data.pcal_true_pid,
					data.pcal_true_avgX, data.pcal_true_avgY,
					data.pcal_true_avgZ, data.pcal_true_totEdep);

			// get the right item
			_view.getHexSectorItem(sector).ijkToScreen(container, localP, pp);

			FeedbackRect rr = new FeedbackRect(pp.x - 4, pp.y - 4, 8, 8,
					hitIndex, data, 0, fbs);
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

		for (FeedbackRect rr : _fbRects) {
			boolean contains = rr.contains(screenPoint, feedbackStrings);
			if (contains && (rr.hitIndex >= 0)) {

				ADataContainer data = rr.dataContainer;
				int hitIndex = rr.hitIndex;

				// additional feedback
				if (rr.dataContainer instanceof ECDataContainer) {
					ECDataContainer ecdata = (ECDataContainer) (rr.dataContainer);
					int strip = data.get(ecdata.pcal_dgtz_strip, hitIndex);
					int view = rr.dataContainer.get(ecdata.pcal_dgtz_view,
							hitIndex);

					if ((strip > 0) && (view > 0)) {
						String s = ADataContainer.trueColor + "Gemc Hit "
								+ " type "
								+ DataDrawSupport.EC_VIEW_NAMES[view]
								+ " strip " + strip;
						feedbackStrings.add(s);
					}
				}
				return;
			}
		}

	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return ADataContainer.trueColor + prompt + " ("
				+ DoubleFormat.doubleFormat(vx, 3) + ", "
				+ DoubleFormat.doubleFormat(vy, 3) + ", "
				+ DoubleFormat.doubleFormat(vz, 3) + ")";
	}
}
