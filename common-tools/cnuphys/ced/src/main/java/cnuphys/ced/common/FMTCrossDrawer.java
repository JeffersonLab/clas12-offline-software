package cnuphys.ced.common;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.List;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.Cross2;
import cnuphys.ced.event.data.CrossList2;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.FMTCrosses;
import cnuphys.ced.geometry.GeometryManager;

public class FMTCrossDrawer extends CedViewDrawer  {


	private static final int ARROWLEN = 30; // pixels
	private static final Stroke THICKLINE = new BasicStroke(1.5f);

	// feedback string color
	private static final String FBCOL = "$wheat$";

	// cached rectangles for feedback
	private Rectangle _fmtFBRects[];
	

	public FMTCrossDrawer(CedView view) {
		super(view);
	}

	@Override
	public void draw(Graphics g, IContainer container) {
		
		if (!_view.showFMTCrosses()) {
			return;
		}
		
		
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
		_fmtFBRects = null;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THICKLINE);

		drawFMTCrosses(g, container);

		g2.setStroke(oldStroke);
		g2.setClip(oldClip);
	}

	/**
	 * Draw FMT crosses
	 * @param g the graphics context
	 * @param container the drawing container
	 */
	public void drawFMTCrosses(Graphics g, IContainer container) {
		
		//treat DCXY view separately
		if (_view instanceof DCXYView) {
			drawFMTCrossesXY(g, container);
			return;
		}
		
		CrossList2 crosses = FMTCrosses.getInstance().getCrosses();
		
		int len = (crosses == null) ? 0 : crosses.size();
		
		if (len == 0) {
			_fmtFBRects = null;
		} else {
			_fmtFBRects = new Rectangle[len];
		}

		double result[] = new double[3];
		
		if (len > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			for (int i = 0; i < len; i++) {
				Cross2 cross = crosses.elementAt(i);

				//lab coordinates
				result[0] = cross.x;
				result[1] = cross.y;
				result[2] = cross.z;
				
				int crossSector = GeometryManager.labXYZToSectorNumber(result);
				
				_view.projectClasToWorld(result[0], result[1], result[2],  _view.getProjectionPlane(), wp);

				int mySector = _view.getSector(container, null, wp);
				if (mySector == crossSector) {

					container.worldToLocal(pp, wp);
					cross.setLocation(pp);

					// arrows

					int pixlen = ARROWLEN;
					double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);
					
					//lab coordinates of end of arrow
					result[0] = cross.x + r * cross.ux;
					result[1] = cross.y + r * cross.uy;
					result[2] = cross.z + r * cross.uz;
					_view.projectClasToWorld(result[0], result[1], result[2],  _view.getProjectionPlane(), wp2);
					container.worldToLocal(pp2, wp2);

					g.setColor(Color.orange);
					g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
					g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
					g.setColor(Color.darkGray);
					g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.FMT_CROSS);

					// fbrects for quick feedback
					_fmtFBRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
							2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
				}
			} // loop over crosses
		} // len > 0

	}
	
	/**
	 * Draw FMT crosses
	 * @param g the graphics context
	 * @param container the drawing container
	 */
	public void drawFMTCrossesXY(Graphics g, IContainer container) {
		
		CrossList2 crosses = FMTCrosses.getInstance().getCrosses();
		
		int len = (crosses == null) ? 0 : crosses.size();
		
		if (len == 0) {
			_fmtFBRects = null;
		} else {
			_fmtFBRects = new Rectangle[len];
		}

		if (len > 0) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			Point2D.Double wp2 = new Point2D.Double();
			Point pp2 = new Point();

			for (int i = 0; i < len; i++) {
				Cross2 cross = crosses.elementAt(i);

				//lab coordinates
				wp.setLocation(cross.x, cross.y);
				


					container.worldToLocal(pp, wp);
					cross.setLocation(pp);

					// arrows

					int pixlen = ARROWLEN;
					double r = pixlen / WorldGraphicsUtilities.getMeanPixelDensity(container);
					
					//lab coordinates of end of arrow
					wp2.setLocation(cross.x + r * cross.ux, cross.y + r * cross.uy);
					container.worldToLocal(pp2, wp2);

					g.setColor(Color.orange);
					g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
					g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
					g.setColor(Color.darkGray);
					g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

					// the circles and crosses
					DataDrawSupport.drawCross(g, pp.x, pp.y, DataDrawSupport.FMT_CROSS);

					// fbrects for quick feedback
					_fmtFBRects[i] = new Rectangle(pp.x - DataDrawSupport.CROSSHALF, pp.y - DataDrawSupport.CROSSHALF,
							2 * DataDrawSupport.CROSSHALF, 2 * DataDrawSupport.CROSSHALF);
			} // loop over crosses
		} // len > 0

	}

	
	/**
	 * Use what was drawn to generate feedback strings
	 * 
	 * @param container the drawing container
	 * @param screenPoint the mouse location
	 * @param worldPoint the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 */
	private void feedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		// svt crosses?
		CrossList2 crosses = FMTCrosses.getInstance().getCrosses();
		int len = (crosses == null) ? 0 : crosses.size();


		if ((len > 0)  && (_fmtFBRects != null) && (_fmtFBRects.length == len)) {
			for (int i = 0; i < len; i++) {
				if ((_fmtFBRects[i] != null) && _fmtFBRects[i].contains(screenPoint)) {

					Cross2 cross = crosses.elementAt(i);
					feedbackStrings.add(FBCOL + "cross ID: " + cross.id + "  sect: " + cross.sector + "  reg: " + cross.region);

					feedbackStrings.add(vecStr("cross loc (lab)", cross.x, cross.y, cross.z));
					feedbackStrings.add(vecStr("cross error", cross.err_x, cross.err_y, cross.err_z));
					feedbackStrings.add(vecStr("cross direction", cross.ux, cross.uy, cross.uz));

					break;
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

	@Override
	public void vdrawFeedback(IContainer container,
			Point screenPoint,
			Double worldPoint,
			List<String> feedbackStrings,
			int option) {
		feedback(container, screenPoint, worldPoint, feedbackStrings);
		
	}


}