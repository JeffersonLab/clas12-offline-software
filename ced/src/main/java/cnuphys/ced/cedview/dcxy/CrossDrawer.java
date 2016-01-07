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
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.item.HexSectorItem;

public class CrossDrawer extends DCXYViewDrawer {

	public static final int HB = 0;
	public static final int TB = 1;

	private static final int ARROWLEN = 40; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);

	// feedback string color
	private static String fbcolors[] = { "$wheat$", "$misty rose$" };

	private int _mode = HB;

	// cached rectangles for feedback
	private FeedbackRects[] _fbRects = new FeedbackRects[2];

	private double tiltedx[];
	private double tiltedy[];
	private double tiltedz[];
	private int sector[];
	private double unitx[];
	private double unity[];
	private double unitz[];

	public CrossDrawer(DCXYView view) {
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
		if (ClasIoEventManager.getInstance().isAccumulating()
				|| (!_view.isSingleEventMode())) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		int crossCount = 0;
		if (_mode == HB) {
			crossCount = DataSupport.getHitBasedCrossCount();
		}
		else if (_mode == TB) {
			crossCount = DataSupport.getTimeBasedCrossCount();
		}

		_fbRects[_mode].rects = null;

		if (crossCount > 0) {
			double result[] = new double[3];
			Point pp = new Point();

			if (_mode == HB) {
				sector = ColumnData
						.getIntArray("HitBasedTrkg::HBCrosses.sector");
				tiltedx = ColumnData
						.getDoubleArray("HitBasedTrkg::HBCrosses.x");
				tiltedy = ColumnData
						.getDoubleArray("HitBasedTrkg::HBCrosses.y");
				tiltedz = ColumnData
						.getDoubleArray("HitBasedTrkg::HBCrosses.z");
				unitx = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.ux");
				unity = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uy");
				unitz = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uz");
			}
			else { //TB
				sector = ColumnData
						.getIntArray("TimeBasedTrkg::TBCrosses.sector");
				tiltedx = ColumnData
						.getDoubleArray("TimeBasedTrkg::TBCrosses.x");
				tiltedy = ColumnData
						.getDoubleArray("TimeBasedTrkg::TBCrosses.y");
				tiltedz = ColumnData
						.getDoubleArray("TimeBasedTrkg::TBCrosses.z");
				unitx = ColumnData
						.getDoubleArray("TimeBasedTrkg::TBCrosses.ux");
				unity = ColumnData
						.getDoubleArray("TimeBasedTrkg::TBCrosses.uy");
				unitz = ColumnData
						.getDoubleArray("TimeBasedTrkg::TBCrosses.uz");
			}

			_fbRects[_mode].rects = new Rectangle[crossCount];

			for (int i = 0; i < crossCount; i++) {
				HexSectorItem hsItem = _view.getHexSectorItem(sector[i]);

				if (hsItem == null) {
					System.err.println(
							"null sector item in DCXY Cross Drawer sector: "
									+ sector[i]);
					break;
				}

				result[0] = tiltedx[i];
				result[1] = tiltedy[i];
				result[2] = tiltedz[i];
				_view.tiltedToSector(result, result);

				// only care about xy
				Point2D.Double sp = new Point2D.Double(result[0], result[1]);
				hsItem.sector2DToLocal(container, pp, sp);

				// arrows
				Point pp2 = new Point();

				int pixlen = ARROWLEN;
				double r = pixlen
						/ WorldGraphicsUtilities.getMeanPixelDensity(container);

				// System.err.println("ARROWLEN r = " + r + " absmaxx: " +
				// DCGeometry.getAbsMaxWireX());
				// System.err.println("PIX LEN: " + pixlen + " density " +
				// WorldGraphicsUtilities.getMeanPixelDensity(container) +
				// " pix/len");

				result[0] = tiltedx[i] + r * unitx[i];
				result[1] = tiltedy[i] + r * unity[i];
				result[2] = tiltedz[i] + r * unitz[i];
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

				// fbrects for quick feedback
				_fbRects[_mode].rects[i] = new Rectangle(
						pp.x - DataDrawSupport.CROSSHALF,
						pp.y - DataDrawSupport.CROSSHALF,
						2 * DataDrawSupport.CROSSHALF,
						2 * DataDrawSupport.CROSSHALF);

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
	public void feedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		if (_fbRects[_mode].rects == null) {
			return;
		}

		// hit based dc crosses?
		int hbcrosscount = DataSupport.getHitBasedCrossCount();

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
			sectarray = ColumnData
					.getIntArray("HitBasedTrkg::HBCrosses.sector");
			regarray = ColumnData.getIntArray("HitBasedTrkg::HBCrosses.region");
			idarray = ColumnData.getIntArray("HitBasedTrkg::HBCrosses.ID");
			cross_x = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.x");
			cross_y = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.y");
			cross_z = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.z");
			uxarray = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.ux");
			uyarray = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uy");
			uzarray = ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uz");
			errxarray = ColumnData
					.getDoubleArray("HitBasedTrkg::HBCrosses.err_x");
			erryarray = ColumnData
					.getDoubleArray("HitBasedTrkg::HBCrosses.err_y");
			errzarray = ColumnData
					.getDoubleArray("HitBasedTrkg::HBCrosses.err_z");
		}
		else { // TB
			sectarray = ColumnData
					.getIntArray("TimeBasedTrkg::TBCrosses.sector");
			regarray = ColumnData
					.getIntArray("TimeBasedTrkg::TBCrosses.region");
			idarray = ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.ID");
			cross_x = ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.x");
			cross_y = ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.y");
			cross_z = ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.z");
			uxarray = ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.ux");
			uyarray = ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.uy");
			uzarray = ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.uz");
			errxarray = ColumnData
					.getDoubleArray("TimeBasedTrkg::TBCrosses.err_x");
			erryarray = ColumnData
					.getDoubleArray("TimeBasedTrkg::TBCrosses.err_y");
			errzarray = ColumnData
					.getDoubleArray("TimeBasedTrkg::TBCrosses.err_z");
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

				// feedbackStrings.add(fbcolors[_mode] + prefix[_mode]
				// + "cross ID: " + id + " sect: " + sect + " reg: "
				// + reg + " track: " + track);

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