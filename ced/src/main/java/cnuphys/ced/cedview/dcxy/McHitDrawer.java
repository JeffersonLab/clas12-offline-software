package cnuphys.ced.cedview.dcxy;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DataSupport;

public class McHitDrawer extends DCXYViewDrawer {

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	public McHitDrawer(DCXYView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (!_view.showMcTruth()) {
			return;
		}
		
		if (!_view.isSingleEventMode()) {
			return;
		}

		_fbRects.clear();

		showGemcXYHits(g, container);
	}

	private void showGemcXYHits(Graphics g, IContainer container) {
//		
//		double x[] = DC.avgX();
//		double y[] = DC.avgY();
//		double z[] = DC.avgZ();
//		int pid[] = DC.pid();
//
//		if ((x == null) || (y == null) || (z == null) || (x.length < 1)) {
//			return;
//		}
//
//		Point2D.Double lab = new Point2D.Double();
//		Point pp = new Point();
//
//		// should not be necessary but be safe
//		int len = x.length;
//		len = Math.min(len, y.length);
//		len = Math.min(len, z.length);
//		for (int hitIndex = 0; hitIndex < len; hitIndex++) {
//			lab.setLocation(x[hitIndex] / 10, y[hitIndex] / 10);
//			DCXYView.labToLocal(container, pp, lab);
//			String pidstr = "";
//			if (pid != null) {
//				LundId lid = LundSupport.getInstance().get(pid[hitIndex]);
//				if (lid != null) {
//					pidstr = " [" + lid.getName() + "] ";
//				} else {
//					pidstr = " [??] (" + pid[hitIndex] + ") ";
//				}
//			}
//
//			// display 1-based hit index
//			String s = vecStr("Gemc Hit [" + (hitIndex + 1) + "] " + pidstr,
//					x[hitIndex] / 10, y[hitIndex] / 10, z[hitIndex] / 10)
//					+ " cm";
//			FeedbackRect rr = new FeedbackRect(FeedbackRect.Dtype.DC, pp.x - 4, pp.y - 4, 8, 8,
//					hitIndex, 0, s);
//			_fbRects.addElement(rr);
//
//			DataDrawSupport.drawGemcHit(g, pp);
//		}
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
			if (rr.contains(screenPoint, feedbackStrings)) {
				return;
			}
		}

	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return DataSupport.trueColor + prompt + " ("
				+ DoubleFormat.doubleFormat(vx, 3) + ", "
				+ DoubleFormat.doubleFormat(vy, 3) + ", "
				+ DoubleFormat.doubleFormat(vz, 3) + ")";
	}
	
}
