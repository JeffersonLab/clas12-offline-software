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
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.event.data.FTOFDataContainer;
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

		if (_view.isAccumulatedMode()) {
			return;
		}
		
		// get the data
		DCDataContainer dcData = ClasIoEventManager.getInstance().getDCData();
		FTOFDataContainer ftofData = _eventManager.getFTOFData();
		ECDataContainer ecData = _eventManager.getECData();


		showGemcXYZHits(g, container, dcData, dcData.dc_true_avgX,
				dcData.dc_true_avgY, dcData.dc_true_avgZ, dcData.dc_true_pid, 0);

		// showGemcXYZHits(g, container, ecData, ecData.ec_true_avgX,
		// ecData.ec_true_avgY, ecData.ec_true_avgZ, ecData.ec_true_pid,
		// ECDataContainer.EC_OPTION);
		//
		// showGemcXYZHits(g, container, ecData, ecData.pcal_true_avgX,
		// ecData.pcal_true_avgY, ecData.ec_true_avgZ,
		// ecData.pcal_true_pid, ECDataContainer.PCAL_OPTION);
		//
		// showGemcXYZHits(g, container, ftofData, ftofData.ftof1a_true_avgX,
		// ftofData.ftof1a_true_avgY, ftofData.ftof1a_true_avgZ,
		// ftofData.ftof1a_true_pid, FTOFDataContainer.PANEL_1A);
		//
		// showGemcXYZHits(g, container, ftofData, ftofData.ftof1b_true_avgX,
		// ftofData.ftof1b_true_avgY, ftofData.ftof1b_true_avgZ,
		// ftofData.ftof1b_true_pid, FTOFDataContainer.PANEL_1B);
		//
		// showGemcXYZHits(g, container, ftofData, ftofData.ftof2b_true_avgX,
		// ftofData.ftof2b_true_avgY, ftofData.ftof2b_true_avgZ,
		// ftofData.ftof2b_true_pid, FTOFDataContainer.PANEL_2B);

	}

	// the actual hit drawing
	private void showGemcXYZHits(Graphics g, IContainer container,
			ADataContainer data, double x[], double y[], double z[], int pid[],
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
					} else {
						pidstr = " [??] (" + pid[hitIndex] + ") ";
					}
				}

				// display 1-based hit index

				pidstr += "  sect: " + sector + "  ";
				String s = vecStr(
						"Gemc Hit [" + (hitIndex + 1) + "] " + pidstr,
						labXYZ[0], labXYZ[1], labXYZ[2])
						+ " cm";
				FeedbackRect rr = new FeedbackRect(pp.x - 4, pp.y - 4, 8, 8,
						hitIndex, data, option, s);
				_fbRects.addElement(rr);

				DataDrawSupport.drawGemcHit(g, pp);
			}

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
	public void vdrawFeedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings, int option) {

		if (_fbRects.isEmpty()) {
			return;
		}

		for (FeedbackRect rr : _fbRects) {
			boolean contains = rr.contains(screenPoint, feedbackStrings);
			if (contains && (rr.hitIndex >= 0)) {

				int hitIndex = rr.hitIndex;

				// additional feedback EC (and PCAL)
				if (rr.dataContainer instanceof ECDataContainer) {
					ECDataContainer data = (ECDataContainer) (rr.dataContainer);

					int strip = -1;
					int stack = -1;
					int view = -1;
					if (rr.option == ECDataContainer.EC_OPTION) {
						strip = rr.dataContainer.get(data.ec_dgtz_strip,
								hitIndex);
						stack = rr.dataContainer.get(data.ec_dgtz_stack,
								hitIndex);
						view = rr.dataContainer
								.get(data.ec_dgtz_view, hitIndex);
					} else { // pcal
						strip = rr.dataContainer.get(data.pcal_dgtz_strip,
								hitIndex);
						stack = rr.dataContainer.get(data.pcal_dgtz_stack,
								hitIndex);
						view = rr.dataContainer.get(data.pcal_dgtz_view,
								hitIndex);
					}

					if ((strip > 0) && (stack > 0) && (view > 0)) {
						String s = ADataContainer.trueColor + "Gemc Hit "
								+ " plane "
								+ DataDrawSupport.EC_PLANE_NAMES[stack]
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
