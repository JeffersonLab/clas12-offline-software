package cnuphys.ced.cedview.projecteddc;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.sectorview.CedViewDrawer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.EC;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public class McHitDrawer extends CedViewDrawer {

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	public McHitDrawer(ProjectedDCView view) {
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

		showGemcXYZHits(g, container, FeedbackRect.Dtype.DC, DC.avgX(), DC.avgY(), DC.avgZ(), DC.pid(), 0);
	}

	// the actual hit drawing
	private void showGemcXYZHits(Graphics g, IContainer container, FeedbackRect.Dtype dtype, double x[], double y[],
			double z[], int pid[], int option) {

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
			boolean showPoint = sector == ((ProjectedDCView)_view).getSector();

			if (showPoint) {
				((ProjectedDCView)_view).getWorldFromClas(labXYZ[0], labXYZ[1], labXYZ[2], wp);
				container.worldToLocal(pp, wp);

				String pidstr = "";
				if (pid != null) {
					LundId lid = LundSupport.getInstance().get(pid[hitIndex]);
					if (lid != null) {
						pidstr = " [" + lid.getName() + "] ";
					} else {
						pidstr = " [??] (" + pid[hitIndex] + ") ";
					}
				}

				// display 1-based hit index

				pidstr += "  sect: " + sector + "  ";
				String s = vecStr("Gemc Hit [" + (hitIndex + 1) + "] " + pidstr, labXYZ[0], labXYZ[1], labXYZ[2])
						+ " cm";
				FeedbackRect rr = new FeedbackRect(dtype, pp.x - 4, pp.y - 4, 8, 8, hitIndex, option, s);
				_fbRects.addElement(rr);

				DataDrawSupport.drawGemcHit(g, pp);
			}

		}
	}

	@Override
	public void vdrawFeedback(IContainer container, Point screenPoint, Double worldPoint, List<String> feedbackStrings,
			int option) {
	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return DataSupport.trueColor + prompt + " (" + DoubleFormat.doubleFormat(vx, 3) + ", "
				+ DoubleFormat.doubleFormat(vy, 3) + ", " + DoubleFormat.doubleFormat(vz, 3) + ")";
	}

}
