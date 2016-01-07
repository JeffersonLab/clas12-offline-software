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
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.EC;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public class McHitDrawer extends SectorViewDrawer {

	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

	public McHitDrawer(SectorView view) {
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


		showGemcXYZHits(g, container, FeedbackRect.Dtype.DC, 
				DC.avgX(),
				DC.avgY(), 
				DC.avgZ(), 
				DC.pid(),
				0);

		showGemcXYZHits(g, container, FeedbackRect.Dtype.FTOF, 
				ColumnData.getDoubleArray("FTOF1A::true.avgX"),
				ColumnData.getDoubleArray("FTOF1A::true.avgY"), 
				ColumnData.getDoubleArray("FTOF1A::true.avgZ"), 
				ColumnData.getIntArray("FTOF1A::true.pid"),
				DataSupport.PANEL_1A);

		showGemcXYZHits(g, container, FeedbackRect.Dtype.FTOF, 
				ColumnData.getDoubleArray("FTOF1B::true.avgX"),
				ColumnData.getDoubleArray("FTOF1B::true.avgY"), 
				ColumnData.getDoubleArray("FTOF1B::true.avgZ"), 
				ColumnData.getIntArray("FTOF1B::true.pid"),
				DataSupport.PANEL_1B);

		showGemcXYZHits(g, container, FeedbackRect.Dtype.FTOF, 
				ColumnData.getDoubleArray("FTOF2B::true.avgX"),
				ColumnData.getDoubleArray("FTOF2B::true.avgY"), 
				ColumnData.getDoubleArray("FTOF2B::true.avgZ"), 
				ColumnData.getIntArray("FTOF2B::true.pid"),
				DataSupport.PANEL_2);

	}

	// the actual hit drawing
	private void showGemcXYZHits(Graphics g, IContainer container,
			FeedbackRect.Dtype dtype,
			double x[], double y[], double z[], int pid[],
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
			boolean showPoint = false;

			switch (_view.getDisplaySectors()) {
			case SECTORS14:
				showPoint = ((sector == 1) || (sector == 4));
				break;
			case SECTORS25:
				showPoint = ((sector == 2) || (sector == 5));
				break;
			case SECTORS36:
				showPoint = ((sector == 3) || (sector == 6));
				break;
			}

			if (showPoint) {
				_view.getWorldFromLabXYZ(labXYZ[0], labXYZ[1], labXYZ[2], wp);
				container.worldToLocal(pp, wp);

				String pidstr = "";
				if (pid != null) {
					LundId lid = LundSupport.getInstance().get(pid[hitIndex]);
					if (lid != null) {
						pidstr = " [" + lid.getName() + "] ";
					}
					else {
						pidstr = " [??] (" + pid[hitIndex] + ") ";
					}
				}

				// display 1-based hit index

				pidstr += "  sect: " + sector + "  ";
				String s = vecStr("Gemc Hit [" + (hitIndex + 1) + "] " + pidstr,
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
		
		int ecstrip[] = EC.strip();
		int ecstack[] = EC.stack();
		int ecview[] = EC.view();
		
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

}
