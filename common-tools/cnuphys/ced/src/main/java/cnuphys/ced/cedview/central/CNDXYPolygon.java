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
import cnuphys.ced.event.data.CND;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.CNDGeometry;

@SuppressWarnings("serial")
public class CNDXYPolygon extends Polygon {

	/**
	 * The layer, 1..3
	 */
	public int layer;

	/**
	 * The paddleId 1..48
	 */
	public int paddleId;
	
	private static Font _font = Fonts.hugeFont;
	
	//"REAL" numbering
	int sector; //1..24
	int component; //1..2


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
		
		int real[] = new int[3];
		int geo[] ={1, layer, paddleId};
		CNDGeometry.geoTripletToRealTriplet(geo, real);
		
		sector = real[0];
		component = real[2];
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
		draw(g, container, CedXYView.LIGHT, Color.black);
	}
	
	/**
	 * Draw the polygon
	 * 
	 * @param g
	 *            the graphics object
	 * @param container
	 *            the drawing container
	 */
	public void draw(Graphics g, IContainer container, Color fillColor, Color lineColor) {
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

		if (fillColor != null) {
			g.setColor(fillColor);
			g.fillPolygon(this);
		}
		g.setColor(lineColor);
		g.drawPolygon(this);
		
		if ((component == 1) && (layer == 2)) {
			Point2D.Double centroid = WorldGraphicsUtilities.getCentroid(wp);
			container.worldToLocal(pp, centroid);
			g.setColor(Color.gray);
			g.setFont(_font);
			g.drawString("" + sector, pp.x-6, pp.y+6);

		}

	}

	public boolean getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		if (!contains(screenPoint)) {
			return false;
		}

		
		fbString("red", "cnd sector " + sector, feedbackStrings);
		fbString("red", "cnd layer " + layer, feedbackStrings);
		fbString("red", "cnd component " + component, feedbackStrings);

		
		//hit?
		
		TdcAdcHit hit = null;
		TdcAdcHitList hits = CND.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			// all hits have component 1
			hit = hits.get(sector, layer, 1);

			if (hit != null) {
				fbString("cyan", "cnd hit order " + hit.order, feedbackStrings);
				if ((hit.order == 0) && (component == 1)) {
					int adc = hit.adcL;
					fbString("cyan", "adcL " + hit.adcL, feedbackStrings);
				}
				else if ((hit.order == 1) && (component == 2)) {
					int adc = hit.adcL;
					fbString("cyan", "adcR " + hit.adcR, feedbackStrings);
			}
				else if ((hit.order == 2) && (component == 1)) {
					int tdc = hit.tdcL;
					fbString("cyan", "tdcL " + hit.tdcL, feedbackStrings);
				}
				else if ((hit.order == 3) && (component == 2)) {
					int tdc = hit.tdcR;
					fbString("cyan", "tdcR " + hit.tdcR, feedbackStrings);
				}

			}
		}

		return true;
	}

	// convenience method to create a feedback string
	private void fbString(String color, String str, List<String> fbstrs) {
		fbstrs.add("$" + color + "$" + str);
	}

}
