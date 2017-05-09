package cnuphys.ced.cedview.central;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.geometry.CTOFGeometry;

public class CTOFXYPolygon extends Polygon {

	/**
	 * The paddleId 1..48
	 */
	public int paddleId;

	//the quad
	private Point2D.Double[] wp;

	/**
	 * Create a XY Polygon for the CND
	 * 
	 * @param paddleId
	 *            the paddle ID 1..48
	 */
	public CTOFXYPolygon(int paddleId) {
		this.paddleId = paddleId;
		wp = CTOFGeometry.getQuad(paddleId);
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
			container.worldToLocal(pp, wp[i]);
			addPoint(pp.x, pp.y);
		}

		g.setColor(CedXYView.LIGHT);
		g.fillPolygon(this);
		g.setColor(Color.black);
		g.drawPolygon(this);

	}

}
