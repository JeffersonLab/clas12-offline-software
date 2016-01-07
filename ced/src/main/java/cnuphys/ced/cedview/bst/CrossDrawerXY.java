package cnuphys.ced.cedview.bst;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
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

public class CrossDrawerXY extends BSTxyViewDrawer {


	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);

	// feedback string color
	private static final String FBCOL = "$wheat$";

	// cached rectangles for feedback
	private Rectangle _fbRects[];

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	public CrossDrawerXY(BSTxyView view) {
		super(view);
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
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);
		_fbRects = null;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawBSTCrosses(g, container);
		drawBMTCrosses(g, container);

		g2.setStroke(oldStroke);
		
		g2.setClip(oldClip);
	}

	public void drawBSTCrosses(Graphics g, IContainer container) {

		// bst crosses?
		if (DataSupport.bstGetCrossCount() == 0) {
			return;
		}

		// System.err.println("Drawing reconstructed data");

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		Point2D.Double wp2 = new Point2D.Double();
		Point pp2 = new Point();

		double labx[] = ColumnData.getDoubleArray("BSTRec::Crosses.x");
		
		if (labx != null) {
			double laby[] = ColumnData.getDoubleArray("BSTRec::Crosses.y");
			double unitx[] = ColumnData.getDoubleArray("BSTRec::Crosses.ux");
			double unity[] = ColumnData.getDoubleArray("BSTRec::Crosses.uy");

			int len = (labx == null) ? 0 : labx.length;

			if (len == 0) {
				_fbRects = null;
			} else {
				_fbRects = new Rectangle[len];
			}

			for (int i = 0; i < len; i++) {
				wp.setLocation(labx[i], laby[i]);

				// arrows

				int pixlen = ARROWLEN;
				double r = pixlen
						/ WorldGraphicsUtilities.getMeanPixelDensity(container);

				wp2.x = wp.x + r * unitx[i];
				wp2.y = wp.y + r * unity[i];

				container.worldToLocal(pp, wp);
				container.worldToLocal(pp2, wp2);

				g.setColor(Color.orange);
				g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
				g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
				g.setColor(Color.darkGray);
				g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

				// the circles and crosses
				DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BST_CROSS);

				// fbrects for quick feedback
				_fbRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
						2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
			} //for i to len
		} //crosses not null
	}

	public void drawBMTCrosses(Graphics g, IContainer container) {

		// bst crosses?
		if (DataSupport.bmtGetCrossCount() == 0) {
			return;
		}

		// System.err.println("Drawing reconstructed data");

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		Point2D.Double wp2 = new Point2D.Double();
		Point pp2 = new Point();
		
		double labx[] = ColumnData.getDoubleArray("BMTRec::Crosses.x");

		if (labx != null) {
			double laby[] = ColumnData.getDoubleArray("BMTRec::Crosses.y");
			double unitx[] = ColumnData.getDoubleArray("BMTRec::Crosses.ux");
			double unity[] = ColumnData.getDoubleArray("BMTRec::Crosses.uy");

			int len = (labx == null) ? 0 : labx.length;

			if (len == 0) {
				_fbRects = null;
			} else {
				_fbRects = new Rectangle[len];
			}

			for (int i = 0; i < len; i++) {
				wp.setLocation(labx[i], laby[i]);

				// arrows

				int pixlen = ARROWLEN;
				double r = pixlen
						/ WorldGraphicsUtilities.getMeanPixelDensity(container);

				wp2.x = wp.x + r * unitx[i];
				wp2.y = wp.y + r * unity[i];

				container.worldToLocal(pp, wp);
				container.worldToLocal(pp2, wp2);

				g.setColor(Color.orange);
				g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
				g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
				g.setColor(Color.darkGray);
				g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

				// the circles and crosses
				DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BMT_CROSS);

				// fbrects for quick feedback
				_fbRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
						2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
			} //for i to len
		} //crosses not null
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

		if (_fbRects == null) {
			return;
		}

		// svt crosses?

		if (_fbRects == null) {
			return;
		}

		double labx[] = ColumnData.getDoubleArray("BSTRec::Crosses.x");
		double laby[] = ColumnData.getDoubleArray("BSTRec::Crosses.y");
		double labz[] = ColumnData.getDoubleArray("BSTRec::Crosses.z");
		double ux[] = ColumnData.getDoubleArray("BSTRec::Crosses.ux");
		double uy[] = ColumnData.getDoubleArray("BSTRec::Crosses.uy");
		double uz[] = ColumnData.getDoubleArray("BSTRec::Crosses.uz");
		int id[] = ColumnData.getIntArray("BSTRec::Crosses.ID");
		double xerr[] = ColumnData.getDoubleArray("BSTRec::Crosses.err_x");
		double yerr[] = ColumnData.getDoubleArray("BSTRec::Crosses.err_y");
		double zerr[] = ColumnData.getDoubleArray("BSTRec::Crosses.err_z");
		int sect[] = ColumnData.getIntArray("BSTRec::Crosses.sector");
		int reg[] = ColumnData.getIntArray("BSTRec::Crosses.region");

		for (int i = 0; i < _fbRects.length; i++) {
			if ((_fbRects[i] != null) && _fbRects[i].contains(screenPoint)) {

				feedbackStrings.add(FBCOL + "cross ID: " + id[i] + "  sect: "
						+ sect[i] + "  reg: " + reg[i]);

				feedbackStrings
						.add(vecStr("cross loc (lab)", labx[i], laby[i], labz[i]));
				feedbackStrings.add(vecStr("cross error", xerr[i], yerr[i], zerr[i]));
				feedbackStrings.add(vecStr("cross direction", ux[i], uy[i], uz[i]));

				break;
			}
		}
	}

	// for writing out a vector
	private String vecStr(String prompt, double vx, double vy, double vz) {
		return FBCOL + prompt + ": (" + DoubleFormat.doubleFormat(vx, 2) + ", "
				+ DoubleFormat.doubleFormat(vy, 2) + ", "
				+ DoubleFormat.doubleFormat(vz, 2) + ")";
	}

}
