package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.Fonts;
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
	
	private static Font _font = Fonts.mediumFont;


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
		
		Point2D.Double wp[] = new Point2D.Double[4];
		
		for (int i = 0; i < 4; i++) {
			// convert cm to mm
			wp[i] = new Point2D.Double(10 * paddle.getVolumePoint(i).x(),
					10 * paddle.getVolumePoint(i).y());
			container.worldToLocal(pp, wp[i]);
			
			addPoint(pp.x, pp.y);
		}

		g.setColor(CedXYView.LIGHT);
		g.fillPolygon(this);
		g.setColor(Color.black);
		g.drawPolygon(this);
		
		if (layer == 1) {
			Point2D.Double centroid = WorldGraphicsUtilities.getCentroid(wp);
			container.worldToLocal(pp, centroid);
			g.setColor(Color.black);
			g.setFont(_font);
			g.drawString("" + paddleId, pp.x-6, pp.y+6);

		}

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
