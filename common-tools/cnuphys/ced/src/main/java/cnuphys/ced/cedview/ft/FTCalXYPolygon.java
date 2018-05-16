package cnuphys.ced.cedview.ft;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.geometry.FTCALGeometry;

public class FTCalXYPolygon extends Polygon {

	/**
	 * The paddleId
	 */
	public int paddleId;

	private ScintillatorPaddle paddle;

	/**
	 * Create a XY Polygon for the CND
	 * 
	 * @param Id
	 *            the paddle ID
	 */
	public FTCalXYPolygon(int paddleId) {
		this.paddleId = paddleId;
		paddle = FTCALGeometry.getPaddle(paddleId);

		// System.err.println("PADDLE ID: " + paddleId + "   good paddle: " +
		// (paddle != null));
	}

	/**
	 * Draw the polygon
	 * 
	 * @param g
	 *            the graphics object
	 * @param container
	 *            the drawing container
	 */
	public void draw(Graphics g, IContainer container) {
		reset();
		Point pp = new Point();
		for (int i = 0; i < 4; i++) {
			container.worldToLocal(pp, paddle.getVolumePoint(i).x(), paddle
					.getVolumePoint(i).y());
			addPoint(pp.x, pp.y);
		}

		g.setColor(CedXYView.LIGHT);
		g.fillPolygon(this);
		g.setColor(Color.black);
		g.drawPolygon(this);

	}

	/**
	 * Get the feedback strings
	 * @param container
	 * @param screenPoint
	 * @param worldPoint
	 * @param feedbackStrings
	 * @return
	 */
	public boolean getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		if (!contains(screenPoint)) {
			return false;
		}

		fbString("red", "Id " + paddleId, feedbackStrings);
		
		//get the xy indices
		Point p = FTCALGeometry.getXYIndices(paddleId);
		fbString("red", "XY Indices [" + p.x + ", " + p.y + "]", feedbackStrings);

		return true;
	}

	// convenience method to create a feedback string
	private void fbString(String color, String str, List<String> fbstrs) {
		fbstrs.add("$" + color + "$" + str);
	}

}
