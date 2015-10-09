package cnuphys.ced.cedview.bst;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.geometry.CNDGeometry;

public class CNDXYPolygon extends Polygon {

	/**
	 * The layer, 1..3
	 */
	public int layer;

	/**
	 * The paddleId 1..48
	 */
	public int paddleId;

	private ScintillatorPaddle paddle;

	/**
	 * Create a XY Polygon for the CND
	 * 
	 * @param layer
	 *            the layer 1..3
	 * @param paddleId
	 *            the paddle ID 1..48
	 */
	public CNDXYPolygon(int layer, int paddleId) {
		this.layer = layer;
		this.paddleId = paddleId;
		paddle = CNDGeometry.getPaddle(layer, paddleId);
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
			// convert cm to mm
			container.worldToLocal(pp, 10 * paddle.getVolumePoint(i).x(),
					10 * paddle.getVolumePoint(i).y());
			addPoint(pp.x, pp.y);
		}

		g.setColor(CedXYView.LIGHT);
		g.fillPolygon(this);
		g.setColor(Color.black);
		g.drawPolygon(this);

	}

	public boolean getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		if (!contains(screenPoint)) {
			return false;
		}

		fbString("red", "cnd layer " + layer, feedbackStrings);
		fbString("red", "cnd paddle " + paddleId, feedbackStrings);

		return true;
	}

	// convenience method to create a feedback string
	private void fbString(String color, String str, List<String> fbstrs) {
		fbstrs.add("$" + color + "$" + str);
	}

}
