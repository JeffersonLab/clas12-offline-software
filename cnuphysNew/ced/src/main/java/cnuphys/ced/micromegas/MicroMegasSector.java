package cnuphys.ced.micromegas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.DonutItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.BMT;
import cnuphys.splot.plot.DoubleFormat;

public class MicroMegasSector extends DonutItem {
	
	private static final String[] sectorNames = {"A[2]", "B[1]", "C[3]"};
	private static final double labelAngs[] = {90, 225, 325};
	private static final double labelRad[] = {230, 220, 225};
	private static final int xoff[] = {0, -30, 4};

	private static double[][] startAngle = new double[3][6];
	private static double[][] stopAngle = new double[3][6];
	private static double[][] delAngle = new double[3][6];

	private static double[] innerRadius = new double[6];
	private static double[] outerRadius = new double[6];
	

	public static final String microMegasStr = UnicodeSupport.SMALL_MU + "megas";
	
	private static Geometry geo;

	static {
		Constants.Load();
		geo = new Geometry();
		for (int sector = 1; sector <= 3; sector++) {
			for (int layer = 5; layer <= 6; layer++) {
				double beginAng = Math
						.toDegrees(geo.CRC_GetBeginStrip(sector, layer));
				double endAng = Math
						.toDegrees(geo.CRC_GetEndStrip(sector, layer));
				startAngle[sector - 1][layer - 1] = beginAng;
				stopAngle[sector - 1][layer - 1] = endAng;

				delAngle[sector - 1][layer - 1] = endAng - beginAng;
				if (delAngle[sector - 1][layer - 1] < 0) {
					delAngle[sector - 1][layer - 1] += 360;
				}
			}
		}

		innerRadius[4] = 205.8; // layer 5 mm
		outerRadius[4] = innerRadius[4] + 4; // layer 5 mm
		innerRadius[5] = 220.8; // layer 6 mm
		outerRadius[5] = innerRadius[5] + 4; // layer 6 mm
	}
	
	// sector and layer are one based
	private int _sector;
	private int _layer;

	// public DonutItem(LogicalLayer layer, Point2D.Double wpc,
	// double radiusInner, double radiusOuter, double startAngle,
	// double arcAngle) {

	public MicroMegasSector(LogicalLayer logLayer, int sector, int layer) {
		super(logLayer, new Point2D.Double(0, 0), innerRadius[layer - 1],
				outerRadius[layer - 1], startAngle[sector - 1][layer - 1],
				delAngle[sector - 1][layer - 1]);

		_sector = sector;
		_layer = layer;
	}
	
	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		
		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		super.drawItem(g, container);
		
		if (_layer == 6) {
			g.setFont(Fonts.smallFont);
			g.setColor(Color.black);
			int sm1 = _sector -1;
			String s = sectorNames[_sector - 1];

			Point2D.Double wp = new Point2D.Double();
			wp.x = labelRad[sm1]*Math.cos(Math.toRadians(labelAngs[sm1]));
			wp.y = labelRad[sm1]*Math.sin(Math.toRadians(labelAngs[sm1]));
			Point pp = new Point();
			container.worldToLocal(pp, wp);
			g.drawString(s, pp.x+xoff[_sector-1], pp.y);
		}
		
		g2.setClip(oldClip);
	}
	
	//horrible hack
	public static int geoSectorFromDataSector(int dataSector) {
		if (dataSector == 2) {
			return 1; //A
		}
		else if (dataSector == 1) {
			return 2; //B
		}
		else if (dataSector == 3) {
			return 3; //C
		}
		return -1;
	}

	/**
	 * 
	 * @param g
	 * @param container
	 * @param strip 1-based strip
	 * @param fillColor
	 * @param lineColor
	 */
	public FeedbackRect drawHit(Graphics g, IContainer container,
			int hit, Color fillColor, Color lineColor) {
		
		if ((_layer % 2) == 0) {
			return null;
		}
		
		int strips[] = BMT.strip();
		double edep[] = BMT.Edep();

		int strip = strips[hit];
		
		//double ang = Math.PI-geo.CRZ_GetAngleStrip(_sector, _layer, strip);
		double ang = geo.CRZ_GetAngleStrip(_sector, _layer, strip);
		
		if (ang > Math.PI) {
			ang -= (2 * Math.PI);
		}
//		System.err.println("SECT: " + sectorNames[_sector-1] + "  LAYER: " + _layer +
//				" STRIP: " + strip + " Zstrip: " + zstrip + 
//				"  ANGLE: " + Math.toDegrees(ang));

		double rad = (innerRadius[_layer - 1] + outerRadius[_layer - 1]) / 2;
		Point2D.Double wp = new Point2D.Double(rad * Math.cos(ang),
				rad * Math.sin(ang));
		Point pp = new Point();
		container.worldToLocal(pp, wp);
		g.setColor(fillColor);
		g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
		g.setColor(lineColor);
		g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
				
		String edepStr="";
		if (edep != null) {
			edepStr = "  edep " + DoubleFormat.doubleFormat(edep[hit],2);
		}
		
		return new FeedbackRect(FeedbackRect.Dtype.BMT, pp.x - 3, pp.y - 3, 6, 6, hit, 0, 
				microMegasStr + " hit " + hit  + " strip " + strip + edepStr);
		
	}

	/**
	 * Add any appropriate feedback strings for the headsup display or feedback
	 * panel.
	 * 
	 * @param container the Base container.
	 * @param screenPoint the mouse location.
	 * @param worldPoint the corresponding world point.
	 * @param feedbackStrings the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double wp, List<String> feedbackStrings) {
		if (contains(container, screenPoint)) {
			double ang  = Math.PI-Math.atan2(wp.y, wp.x);
			int zstrip = geo.getZStrip(_layer, ang);

			feedbackStrings.add("$lawn green$" + microMegasStr + " sect: "
					+ sectorNames[_sector - 1] + " lay: " + _layer + " strip: " + zstrip);			
		}
	}
}
