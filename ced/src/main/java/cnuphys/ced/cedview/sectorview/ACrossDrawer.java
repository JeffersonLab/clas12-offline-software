package cnuphys.ced.cedview.sectorview;

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
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DataDrawSupport;

public class ACrossDrawer extends CedViewDrawer {

	public static final int HB = 0;
	public static final int TB = 1;

	protected static final int ARROWLEN = 30; // pixels
	protected static final Stroke THICKLINE = new BasicStroke(1.5f);

	// feedback string color
	protected static String fbcolors[] = { "$wheat$", "$misty rose$" };

	protected int _mode = HB;

	protected double tiltedx[];
	protected double tiltedy[];
	protected double tiltedz[];
	protected int sector[];
	protected double unitx[];
	protected double unity[];
	protected double unitz[];

	// cached rectangles for feedback
	protected FeedbackRects[] _fbRects = new FeedbackRects[2];

	public ACrossDrawer(CedView view) {
		super(view);
		for (int i = 0; i < _fbRects.length; i++) {
			_fbRects[i] = new FeedbackRects();
		}
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

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		// dc crosses?
		int crossCount = 0;
		if (_mode == HB) {
			crossCount = DC.hitBasedCrossCount();
		}
		else if (_mode == TB) {
			crossCount = DC.timeBasedCrossCount();
		}

		_fbRects[_mode].rects = null;

		if (crossCount > 0) {
			double result[] = new double[3];
			Point pp = new Point();
			Point2D.Double wp = new Point2D.Double();
			if (_mode == HB) {
				sector = DC.hitBasedCrossSector();
				tiltedx = DC.hitBasedCrossX();
				tiltedy = DC.hitBasedCrossY();
				tiltedz = DC.hitBasedCrossZ();
				unitx = DC.hitBasedCrossUx();
				unity = DC.hitBasedCrossUy();
				unitz = DC.hitBasedCrossUz();
			}
			else if (_mode == TB) {
				sector = DC.timeBasedCrossSector();
				tiltedx = DC.timeBasedCrossX();
				tiltedy = DC.timeBasedCrossY();
				tiltedz = DC.timeBasedCrossZ();
				unitx = DC.timeBasedCrossUx();
				unity = DC.timeBasedCrossUy();
				unitz = DC.timeBasedCrossUz();
			}

			_fbRects[_mode].rects = new Rectangle[crossCount];

			for (int i = 0; i < crossCount; i++) {
				result[0] = tiltedx[i];
				result[1] = tiltedy[i];
				result[2] = tiltedz[i];
				_view.tiltedToSector(result, result);
				CedView.sectorToWorld(_view.getProjectionPlane(), wp, result, sector[i]);

				// right sector?
				int mySector = _view.getSector(container, null, wp);
				if (mySector == sector[i]) {

					container.worldToLocal(pp, wp);

					// arrows
					Point2D.Double wp2 = new Point2D.Double();
					Point pp2 = new Point();

					int pixlen = ARROWLEN;
					double r = pixlen / WorldGraphicsUtilities
							.getMeanPixelDensity(container);

					result[0] = tiltedx[i] + r * unitx[i];
					result[1] = tiltedy[i] + r * unity[i];
					result[2] = tiltedz[i] + r * unitz[i];
					_view.tiltedToSector(result, result);

					CedView.sectorToWorld(_view.getProjectionPlane(), wp2, result, sector[i]);
					container.worldToLocal(pp2, wp2);

					g.setColor(Color.orange);
					g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
					g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
					g.setColor(Color.darkGray);
					g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

					// the circles and crosses
					DataDrawSupport.drawCross(g2, pp.x, pp.y, _mode);

					// fbrects for quick feedback
					_fbRects[_mode].rects[i] = new Rectangle(
							pp.x - DataDrawSupport.CROSSHALF,
							pp.y - DataDrawSupport.CROSSHALF,
							2 * DataDrawSupport.CROSSHALF,
							2 * DataDrawSupport.CROSSHALF);
				}

			} // end for
		} // hbcrosscount > 0

		g2.setStroke(oldStroke);
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

		if (_fbRects[_mode].rects == null) {
			return;
		}

		// hit based dc crosses?
		int hbcrosscount = DC.hitBasedCrossCount();

		if (hbcrosscount < 1) {
			return;
		}

		int sectarray[];
		int regarray[];
		int idarray[];
		double cross_x[];
		double cross_y[];
		double cross_z[];
		double uxarray[];
		double uyarray[];
		double uzarray[];
		double errxarray[];
		double erryarray[];
		double errzarray[];

		if (_mode == HB) {
			sectarray = DC.hitBasedCrossSector();
			regarray = DC.hitBasedCrossRegion();
			idarray = DC.hitBasedCrossID();
			cross_x = DC.hitBasedCrossX();
			cross_y = DC.hitBasedCrossY();
			cross_z = DC.hitBasedCrossZ();
			uxarray = DC.hitBasedCrossUx();
			uyarray = DC.hitBasedCrossUy();
			uzarray = DC.hitBasedCrossUz();
			errxarray = DC.hitBasedCrossErrX();
			erryarray = DC.hitBasedCrossErrY();
			errzarray = DC.hitBasedCrossErrZ();
		}
		else { // TB
			sectarray = DC.timeBasedCrossSector();
			regarray = DC.timeBasedCrossRegion();
			idarray = DC.timeBasedCrossID();
			cross_x = DC.timeBasedCrossX();
			cross_y = DC.timeBasedCrossY();
			cross_z = DC.timeBasedCrossZ();
			uxarray = DC.timeBasedCrossUx();
			uyarray = DC.timeBasedCrossUy();
			uzarray = DC.timeBasedCrossUz();
			errxarray = DC.timeBasedCrossErrX();
			erryarray = DC.timeBasedCrossErrY();
			errzarray = DC.timeBasedCrossErrZ();
		}

		for (int i = 0; i < _fbRects[_mode].rects.length; i++) {
			if ((_fbRects[_mode].rects[i] != null)
					&& _fbRects[_mode].rects[i].contains(screenPoint)) {

				double tiltedx = cross_x[i];
				double tiltedy = cross_y[i];
				double tiltedz = cross_z[i];

				int id = idarray[i];
				int sect = sectarray[i];
				int reg = regarray[i];
				// int track = trackarray[i];

				double xerr = errxarray[i];
				double yerr = erryarray[i];
				double zerr = errzarray[i];

				double ux = uxarray[i];
				double uy = uyarray[i];
				double uz = uzarray[i];

				// feedbackStrings.add(fbcolors[_mode] + prefix[_mode] +
				// "cross ID: " + id
				// + " sect: " + sect + " reg: " + reg
				// + " track: " + track);
				feedbackStrings.add(fbcolors[_mode]
						+ DataDrawSupport.prefix[_mode] + "cross ID: " + id
						+ "  sect: " + sect + "  reg: " + reg);

				feedbackStrings.add(
						vecStr("cross loc tilted", tiltedx, tiltedy, tiltedz));
				feedbackStrings.add(vecStr("cross error", xerr, yerr, zerr));
				feedbackStrings.add(vecStr("cross direc tilted", ux, uy, uz));

				double result[] = new double[3];
				result[0] = tiltedx;
				result[1] = tiltedy;
				result[2] = tiltedz;
				_view.tiltedToSector(result, result);
				feedbackStrings.add(vecStr("cross loc sector", result[0],
						result[1], result[2]));

				result[0] = ux;
				result[1] = uy;
				result[2] = uz;
				_view.tiltedToSector(result, result);
				feedbackStrings.add(vecStr("cross direc sector", result[0],
						result[1], result[2]));

				break;
			}
		}
	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return fbcolors[_mode] + DataDrawSupport.prefix[_mode] + prompt + " ("
				+ DoubleFormat.doubleFormat(vx, 2) + ", "
				+ DoubleFormat.doubleFormat(vy, 2) + ", "
				+ DoubleFormat.doubleFormat(vz, 2) + ")";
	}

	class FeedbackRects {
		public Rectangle rects[];
	}

}
