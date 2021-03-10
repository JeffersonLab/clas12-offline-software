package cnuphys.ced.cedview.dcxy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.AIHBCrosses;
import cnuphys.ced.event.data.AITBCrosses;
import cnuphys.ced.event.data.Cross;
import cnuphys.ced.event.data.CrossList;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.HBCrosses;
import cnuphys.ced.event.data.TBCrosses;
import cnuphys.ced.item.HexSectorItem;

public class CrossDrawer extends DCXYViewDrawer {

	public static final int HB = cnuphys.ced.common.CrossDrawer.HB;
	public static final int TB = cnuphys.ced.common.CrossDrawer.TB;
	public static final int AIHB = cnuphys.ced.common.CrossDrawer.AIHB;
	public static final int AITB = cnuphys.ced.common.CrossDrawer.AITB;


	private static final int ARROWLEN = cnuphys.ced.common.CrossDrawer.ARROWLEN;
	private static final Stroke THICKLINE = cnuphys.ced.common.CrossDrawer.THICKLINE;

	// feedback string color
	private static String fbcolors[] = cnuphys.ced.common.CrossDrawer.fbcolors;

	private int _mode = HB;

	/**
	 * A cross drawer for the DCXY view
	 * @param view the view
	 */
	public CrossDrawer(DCXYView view) {
		super(view);
	}

	/**
	 * Set the mode. 0 for hit based, 1 for time based
	 * 
	 * @param mode the new mode
	 */
	public void setMode(int mode) {
		_mode = mode;
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (!_view.isSingleEventMode()) {
			return;
		}

		// any crosses?
		CrossList crosses = null;
		if (_mode == HB) {
			crosses = HBCrosses.getInstance().getCrosses();
		} 
		else if (_mode == TB) {
			crosses = TBCrosses.getInstance().getCrosses();
		}
		else if (_mode == AIHB) {
			crosses = AIHBCrosses.getInstance().getCrosses();
		}
		else if (_mode == AITB) {
			crosses = AITBCrosses.getInstance().getCrosses();
		}
		
		if ((crosses == null) || crosses.isEmpty()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		double result[] = new double[3];
		Point pp = new Point();

		for (Cross cross : crosses) {
			HexSectorItem hsItem = _view.getHexSectorItem(cross.sector);

			if (hsItem == null) {
				System.err.println("null sector item in DCXY Cross Drawer sector: " + cross.sector);
				break;
			}

			result[0] = cross.x;
			result[1] = cross.y;
			result[2] = cross.z;
			_view.tiltedToSector(result, result);

			// only care about xy
			Point2D.Double sp = new Point2D.Double(result[0], result[1]);
			hsItem.sector2DToLocal(container, pp, sp);
			cross.setLocation(pp);

			// arrows
			Point pp2 = new Point();

			int pixlen = ARROWLEN;
			double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

			// System.err.println("ARROWLEN r = " + r + " absmaxx: " +
			// DCGeometry.getAbsMaxWireX());
			// System.err.println("PIX LEN: " + pixlen + " density " +
			// WorldGraphicsUtilities.getMeanPixelDensity(container) +
			// " pix/len");

			result[0] = cross.x + r * cross.ux;
			result[1] = cross.y + r * cross.uy;
			result[2] = cross.z + r * cross.uz;
			_view.tiltedToSector(result, result);
			sp.setLocation(result[0], result[1]);
			hsItem.sector2DToLocal(container, pp2, sp);

			g.setColor(Color.orange);
			g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
			g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
			g.setColor(Color.darkGray);
			g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

			// the circles and crosses
			DataDrawSupport.drawCross(g, pp.x, pp.y, _mode);
		}

		g2.setStroke(oldStroke);
	}

	/**
	 * Use what was drawn to generate feedback strings
	 * 
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location
	 * @param worldPoint      the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 */
	@Override
	public void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		// any crosses?
		CrossList crosses = null;
		if (_mode == HB) {
			crosses = HBCrosses.getInstance().getCrosses();
		} 
		else if (_mode == TB) {
			crosses = TBCrosses.getInstance().getCrosses();
		}
		else if (_mode == AIHB) {
			crosses = AIHBCrosses.getInstance().getCrosses();
		}
		else if (_mode == AITB) {
			crosses = AITBCrosses.getInstance().getCrosses();
		}
		
		if ((crosses == null) || crosses.isEmpty()) {
			return;
		}

		for (Cross cross : crosses) {
			if (cross.contains(screenPoint)) {
				feedbackStrings.add(fbcolors[_mode] + DataDrawSupport.prefix[_mode] + "cross ID: " + cross.id
						+ "  sect: " + cross.sector + "  reg: " + cross.region);

				feedbackStrings.add(vecStr("cross loc tilted", cross.x, cross.y, cross.z));
				feedbackStrings.add(vecStr("cross error", cross.err_x, cross.err_y, cross.err_z));
				feedbackStrings.add(vecStr("cross direc tilted", cross.ux, cross.uy, cross.uz));

				double result[] = new double[3];
				result[0] = cross.x;
				result[1] = cross.y;
				result[2] = cross.z;
				_view.tiltedToSector(result, result);
				feedbackStrings.add(vecStr("cross loc vector", result[0], result[1], result[2]));

				result[0] = cross.ux;
				result[1] = cross.uy;
				result[2] = cross.uz;
				_view.tiltedToSector(result, result);
				feedbackStrings.add(vecStr("cross direc vector", result[0], result[1], result[2]));
				break;
			}
		}
	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return fbcolors[_mode] + DataDrawSupport.prefix[_mode] + prompt + " (" + DoubleFormat.doubleFormat(vx, 2) + ", "
				+ DoubleFormat.doubleFormat(vy, 2) + ", " + DoubleFormat.doubleFormat(vz, 2) + ")";
	}

	class FeedbackRects {
		public Rectangle rects[];
	}
}