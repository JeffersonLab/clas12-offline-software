package cnuphys.snr.test;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TrackTest {

	private Stroke trackStroke = new BasicStroke(2);

	private Point2D.Double startPoint;

	private Point2D.Double endPoint;

	private Point p0 = new Point();

	private Point p1 = new Point();

	public TrackTest(Point2D.Double startPoint, Point2D.Double endPoint) {
		super();
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	/**
	 * Draw the track.
	 * 
	 * @param g
	 * @param world
	 * @param local
	 */
	public void draw(Graphics g, Rectangle2D.Double world, Rectangle local) {
		Graphics2D g2 = (Graphics2D) g;

		RenderingHints rhints = g2.getRenderingHints();
		boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_OFF);

		// Enable antialiasing for shapes

		if (!antialiasOn) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		TestSupport.toLocal(world, local, p0, startPoint);
		TestSupport.toLocal(world, local, p1, endPoint);
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(trackStroke);
		g2.setColor(TestParameters.getTrackColor());
		g2.drawLine(p0.x, Math.max(20, p0.y), p1.x, Math.max(20, p1.y));
		g2.setStroke(oldStroke);
	}

	public Point2D.Double getStartPoint() {
		return startPoint;
	}

	public Point2D.Double getEndPoint() {
		return endPoint;
	}

}
