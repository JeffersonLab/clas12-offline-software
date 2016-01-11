package cnuphys.ced.cedview.bst;

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
import cnuphys.ced.event.data.BMT;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DataDrawSupport;

public class CrossDrawerZ extends BSTzViewDrawer {

	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);
	private static final Color ERROR = new Color(255, 0, 0, 128);

	// feedback string color
	private static final String FBCOL = "$wheat$";

	// cached rectangles for feedback
	private Rectangle _fbRects[];

	/**
	 * A BST Cross drawer
	 * @param view the owner vie
	 */
	public CrossDrawerZ(BSTzView view) {
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
		_fbRects = null;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawBSTCrosses(g, container);
		drawBMTCrosses(g, container);
		g2.setStroke(oldStroke);
	}

	public void drawBSTCrosses(Graphics g, IContainer container) {
		// bst crosses?
		if (BST.crossCount() == 0) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		Point2D.Double wp2 = new Point2D.Double();
		Point pp2 = new Point();
		Point2D.Double wp3 = new Point2D.Double();
		Point2D.Double wp4 = new Point2D.Double();

		double labx[] = BMT.crossX();

		if (labx != null) {
			double laby[] = BMT.crossY();
			double labz[] = BMT.crossZ();
			double errz[] = BMT.crossZerr();
			double unitx[] = BMT.crossUx();
			double unity[] = BMT.crossUy();
			double unitz[] = BMT.crossUz();
			
			int len = (labx == null) ? 0 : labx.length;

			if (len == 0) {
				_fbRects = null;
			} else {
				_fbRects = new Rectangle[len];
			}

			// error bars
			if (errz != null) {
				for (int i = 0; i < len; i++) {
					// System.err.println("Draw Z error bars");
					_view.labToWorld(labx[i], laby[i], labz[i], wp);
					wp3.setLocation(wp.x - errz[i], wp.y);
					wp4.setLocation(wp.x + errz[i], wp.y);
					container.worldToLocal(pp, wp3);
					container.worldToLocal(pp2, wp4);
					g.setColor(ERROR);
					g.fillRect(pp.x, pp.y - DataDrawSupport.CROSSHALF, pp2.x - pp.x,
							2 * DataDrawSupport.CROSSHALF);
				}
			}
			
			for (int i = 0; i < len; i++) {
				_view.labToWorld(labx[i], laby[i], labz[i], wp);

				// arrows

				int pixlen = ARROWLEN;
				double r = pixlen
						/ WorldGraphicsUtilities.getMeanPixelDensity(container);

				double xa = labx[i] + r * unitx[i];
				double ya = laby[i] + r * unity[i];
				double za = labz[i] + r * unitz[i];
				_view.labToWorld(xa, ya, za, wp2);

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
			}
		}
	}
	
	public void drawBMTCrosses(Graphics g, IContainer container) {
		// bst crosses?
		
		
		if (BMT.crossCount() == 0) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		Point2D.Double wp2 = new Point2D.Double();
		Point pp2 = new Point();
		Point2D.Double wp3 = new Point2D.Double();
		Point2D.Double wp4 = new Point2D.Double();
		
		double labx[] = BMT.crossX();

		if (labx != null) {
			double laby[] = BMT.crossY();
			double labz[] = BMT.crossZ();
			double errz[] = BMT.crossZerr();
			double unitx[] = BMT.crossUx();
			double unity[] = BMT.crossUy();
			double unitz[] = BMT.crossUz();

			int len = (labx == null) ? 0 : labx.length;

			if (len == 0) {
				_fbRects = null;
			} else {
				_fbRects = new Rectangle[len];
			}
			
			// error bars
			if (errz != null) {
				for (int i = 0; i < len; i++) {
					// System.err.println("Draw Z error bars");
					_view.labToWorld(labx[i], laby[i], labz[i], wp);
					wp3.setLocation(wp.x - errz[i], wp.y);
					wp4.setLocation(wp.x + errz[i], wp.y);
					container.worldToLocal(pp, wp3);
					container.worldToLocal(pp2, wp4);
					g.setColor(ERROR);
					g.fillRect(pp.x, pp.y - DataDrawSupport.CROSSHALF, pp2.x - pp.x,
							2 * DataDrawSupport.CROSSHALF);
				}
			}


			for (int i = 0; i < len; i++) {
				_view.labToWorld(labx[i], laby[i], labz[i], wp);

				// arrows

				int pixlen = ARROWLEN;
				double r = pixlen
						/ WorldGraphicsUtilities.getMeanPixelDensity(container);

				double xa = labx[i] + r * unitx[i];
				double ya = laby[i] + r * unity[i];
				double za = labz[i] + r * unitz[i];
				_view.labToWorld(xa, ya, za, wp2);

				container.worldToLocal(pp, wp);
				container.worldToLocal(pp2, wp2);

				g.setColor(Color.orange);
				g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
				g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
				g.setColor(Color.darkGray);
				g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

				DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BMT_CROSS);

				// fbrects for quick feedback
				_fbRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
						2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
			}
		}  //labx != null
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

		if (_fbRects == null) {
			return;
		}


		if (_fbRects == null) {
			return;
		}

		double labx[] = BST.crossX();
		double laby[] = BST.crossY();
		double labz[] = BST.crossZ();
		double ux[] = BST.crossUx();
		double uy[] = BST.crossUy();
		double uz[] = BST.crossUz();
		int id[] = BST.crossID();
		double xerr[] = BST.crossXerr();
		double yerr[] = BST.crossYerr();
		double zerr[] = BST.crossZerr();
		int sect[] = BST.crossSector();
		int reg[] = BST.crossRegion();

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
