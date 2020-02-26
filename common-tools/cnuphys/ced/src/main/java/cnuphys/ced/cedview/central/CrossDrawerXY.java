package cnuphys.ced.cedview.central;

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
import cnuphys.ced.event.data.BMTCrosses;
import cnuphys.ced.event.data.Cross2;
import cnuphys.ced.event.data.CrossList2;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.BSTCrosses;

public class CrossDrawerXY extends CentralXYViewDrawer {


	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);

	// feedback string color
	private static final String FBCOL = "$wheat$";

	// cached rectangles for feedback
	private Rectangle _svtFBRects[];
	
	// cached rectangles for feedback
	private Rectangle _bmtFBRects[];


	public CrossDrawerXY(CentralXYView view) {
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
		_svtFBRects = null;
		_bmtFBRects = null;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawBSTCrosses(g, container);
		drawBMTCrosses(g, container);

		g2.setStroke(oldStroke);
		
		g2.setClip(oldClip);
	}

	/**
	 * Draw BST crosses
	 * @param g the graphics context
	 * @param container the drawing container
	 */
	public void drawBSTCrosses(Graphics g, IContainer container) {
		
		CrossList2 crosses = BSTCrosses.getInstance().getCrosses();
		
		int len = (crosses == null) ? 0 : crosses.size();
		
		if (len == 0) {
			_svtFBRects = null;
		} else {
			_svtFBRects = new Rectangle[len];
		}

		
		if (len > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			for (int i = 0; i < len; i++) {
				Cross2 cross = crosses.elementAt(i);

				if (!cross.isXYLocationBad()) {
					
					//version 1.0 must convert cm to mm
					wp.setLocation(10.0*cross.x, 10.0*cross.y);
					// arrows

					if (!cross.isDirectionBad()) {
						int pixlen = ARROWLEN;
						double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

						wp2.x = wp.x + r * cross.ux;
						wp2.y = wp.y + r * cross.uy;

						container.worldToLocal(pp, wp);
						container.worldToLocal(pp2, wp2);

						g.setColor(Color.orange);
						g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
						g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
						g.setColor(Color.darkGray);
						g.drawLine(pp.x, pp.y, pp2.x, pp2.y);
					} //direction bad

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BST_CROSS);

					// fbrects for quick feedback
					_svtFBRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
							2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
				}  //location !bad
				else {
					_svtFBRects[i] = new Rectangle(0, 0, 0, 0);
				}

			}
		} // len > 0

	}

	/**
	 * Draw the BMT Crosses
	 * @param g
	 * @param container
	 */
	public void drawBMTCrosses(Graphics g, IContainer container) {
		
		CrossList2 crosses = BMTCrosses.getInstance().getCrosses();
		
		int len = (crosses == null) ? 0 : crosses.size();
		
		if (len == 0) {
			_bmtFBRects = null;
		} else {
			_bmtFBRects = new Rectangle[len];
		}

		
		if (len > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			for (int i = 0; i < len; i++) {
				Cross2 cross = crosses.elementAt(i);
				//version 1.0 must convert cm to mm
				wp.setLocation(10.0*cross.x, 10.0*cross.y);
				// arrows

				int pixlen = ARROWLEN;
				double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);

				if (!cross.isXYLocationBad()) {
					container.worldToLocal(pp, wp);
					if (!cross.isDirectionBad()) {
						wp2.x = wp.x + r * cross.ux;
						wp2.y = wp.y + r * cross.uy;

						container.worldToLocal(pp2, wp2);

						g.setColor(Color.orange);
						g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
						g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
						g.setColor(Color.darkGray);
						g.drawLine(pp.x, pp.y, pp2.x, pp2.y);
					} //bad direction

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.BMT_CROSS);

					// fbrects for quick feedback
					_bmtFBRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
							2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
				}  //bad location
				else {
					_bmtFBRects[i] = new Rectangle(0,0,0,0);
				}
			}
		} //len > 0
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
			Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		// svt crosses?
		if (_view.showCrosses()) {

			CrossList2 crosses = BSTCrosses.getInstance().getCrosses();
			int len = (crosses == null) ? 0 : crosses.size();

			if ((len > 0) && (_svtFBRects != null) && (_svtFBRects.length == len)) {
				for (int i = 0; i < len; i++) {
					if ((_svtFBRects[i] != null) && _svtFBRects[i].contains(screenPoint)) {

						Cross2 cross = crosses.elementAt(i);
						feedbackStrings.add(
								FBCOL + "cross ID: " + cross.id + "  sect: " + cross.sector + "  reg: " + cross.region);

						if (!cross.isXYLocationBad()) {
							//to mm
							feedbackStrings.add(vecStr("cross loc (lab)", 10*cross.x, 10*cross.y, 10*cross.z));
						}
						else {
							feedbackStrings.add("cross location contains NaN");
						}

						if (!cross.isErrorBad()) {
							//to mm
							feedbackStrings.add(vecStr("cross error", 10*cross.err_x, 10*cross.err_y, 10*cross.err_z));
						}
						else {
							feedbackStrings.add("cross error contains NaN");
						}

						if (!cross.isDirectionBad()) {
							feedbackStrings.add(vecStr("cross direction", cross.ux, cross.uy, cross.uz));
						}
						else {
							feedbackStrings.add("cross direction contains NaN");
						}

						break;
					}
				}
			}
		}

		// bmt crosses?
		if (_view.showCrosses()) {
			CrossList2 crosses = BMTCrosses.getInstance().getCrosses();
			int len = (crosses == null) ? 0 : crosses.size();

			if ((len > 0) && (_bmtFBRects != null) && (_bmtFBRects.length == len)) {
				for (int i = 0; i < len; i++) {
					if ((_bmtFBRects[i] != null) && _bmtFBRects[i].contains(screenPoint)) {

						Cross2 cross = crosses.elementAt(i);
						feedbackStrings.add(
								FBCOL + "cross ID: " + cross.id + "  sect: " + cross.sector + "  reg: " + cross.region);

						if (!cross.isXYLocationBad()) {
							//to mm
							feedbackStrings.add(vecStr("cross loc (lab)", 10*cross.x, 10*cross.y, 10*cross.z));
						}
						else {
							feedbackStrings.add("cross location contains NaN");
						}

						if (!cross.isErrorBad()) {
							feedbackStrings.add(vecStr("cross error", 10*cross.err_x, 10*cross.err_y, 10*cross.err_z));
						}
						else {
							feedbackStrings.add("cross error contains NaN");
						}

						if (!cross.isDirectionBad()) {
							feedbackStrings.add(vecStr("cross direction", cross.ux, cross.uy, cross.uz));
						}
						else {
							feedbackStrings.add("cross direction contains NaN");
						}

						break;
					}
				}
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
