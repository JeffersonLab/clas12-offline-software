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
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.BMTDataContainer;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.event.data.DataDrawSupport;

public class CrossDrawerZ extends BSTzViewDrawer {

	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);
	private static final Color ERROR = new Color(255, 0, 0, 128);

	// feedback string color
	private static final String FBCOL = "$wheat$";

	// cached rectangles for feedback
	private Rectangle _fbRects[];

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

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
		BSTDataContainer bstData = _eventManager.getBSTData();
		if (bstData.getCrossCount() == 0) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		Point2D.Double wp2 = new Point2D.Double();
		Point pp2 = new Point();
		Point2D.Double wp3 = new Point2D.Double();
		Point2D.Double wp4 = new Point2D.Double();

		if (bstData.bstrec_crosses_x != null) {
			double labx[] = bstData.bstrec_crosses_x;
			double laby[] = bstData.bstrec_crosses_y;
			double labz[] = bstData.bstrec_crosses_z;
			double errz[] = bstData.bstrec_crosses_err_z;

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

				double unitx[] = bstData.bstrec_crosses_ux;
				double unity[] = bstData.bstrec_crosses_uy;
				double unitz[] = bstData.bstrec_crosses_uz;
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
		BMTDataContainer bmtData = _eventManager.getBMTData();
		if (bmtData.getCrossCount() == 0) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();
		Point2D.Double wp2 = new Point2D.Double();
		Point pp2 = new Point();
		Point2D.Double wp3 = new Point2D.Double();
		Point2D.Double wp4 = new Point2D.Double();

		if (bmtData.bmtrec_crosses_x != null) {
			double labx[] = bmtData.bmtrec_crosses_x;
			double laby[] = bmtData.bmtrec_crosses_y;
			double labz[] = bmtData.bmtrec_crosses_z;
			double errz[] = bmtData.bmtrec_crosses_err_z;

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

				double unitx[] = bmtData.bmtrec_crosses_ux;
				double unity[] = bmtData.bmtrec_crosses_uy;
				double unitz[] = bmtData.bmtrec_crosses_uz;
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

		if (_fbRects == null) {
			return;
		}

		// svt crosses?
		BSTDataContainer bstData = _eventManager.getBSTData();

		if (_fbRects == null) {
			return;
		}

		for (int i = 0; i < _fbRects.length; i++) {
			if ((_fbRects[i] != null) && _fbRects[i].contains(screenPoint)) {

				double labx = bstData.bstrec_crosses_x[i];
				double laby = bstData.bstrec_crosses_y[i];
				double labz = bstData.bstrec_crosses_z[i];

				int id = bstData.bstrec_crosses_ID[i];
				int sect = bstData.bstrec_crosses_sector[i];
				int reg = bstData.bstrec_crosses_region[i];

				double xerr = bstData.bstrec_crosses_err_x[i];
				double yerr = bstData.bstrec_crosses_err_y[i];
				double zerr = bstData.bstrec_crosses_err_z[i];

				double ux = bstData.bstrec_crosses_ux[i];
				double uy = bstData.bstrec_crosses_uy[i];
				double uz = bstData.bstrec_crosses_uz[i];

				feedbackStrings.add(FBCOL + "cross ID: " + id + "  sect: "
						+ sect + "  reg: " + reg);

				feedbackStrings
						.add(vecStr("cross loc (lab)", labx, laby, labz));
				feedbackStrings.add(vecStr("cross error", xerr, yerr, zerr));
				feedbackStrings.add(vecStr("cross direction", ux, uy, uz));

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
