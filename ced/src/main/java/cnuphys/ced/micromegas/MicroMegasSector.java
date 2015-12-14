package cnuphys.ced.micromegas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.DonutItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.BMTDataContainer;
import cnuphys.splot.plot.DoubleFormat;

public class MicroMegasSector extends DonutItem {
	
	private static final String[] sectorNames = {"A [2]", "B [1]", "C [3]"};
	private static final double labelAngs[] = {90, 225, 325};
	private static final double labelRad = 235;
	private static final int xoff[] = {0, 4, -30};

	private static double[][] startAngle = new double[3][6];
	private static double[][] stopAngle = new double[3][6];
	private static double[][] delAngle = new double[3][6];

	private static double[] innerRadius = new double[6];
	private static double[] outerRadius = new double[6];
	
	// cached rectangles for feedback
	private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();


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
	
	public void clearFBRects() {
		_fbRects.clear();		
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
		super.drawItem(g, container);
		
		if (_layer == 6) {
			g.setFont(Fonts.mediumFont);
			g.setColor(Color.black);
			String s = sectorNames[_sector - 1];

			Point2D.Double wp = new Point2D.Double();
			wp.x = labelRad*Math.cos(Math.toRadians(labelAngs[_sector-1]));
			wp.y = labelRad*Math.sin(Math.toRadians(labelAngs[_sector-1]));
			Point pp = new Point();
			container.worldToLocal(pp, wp);
			g.drawString(s, pp.x+xoff[_sector-1], pp.y);
		}
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
	public void drawHit(Graphics g, IContainer container, BMTDataContainer bmtData,
			int hit, Color fillColor, Color lineColor) {
		
		if ((_layer % 2) == 0) {
			return;
		}
		
		int strips[] = bmtData.bmt_dgtz_strip;

		int strip = strips[hit];
		
		//double ang = Math.PI-geo.CRZ_GetAngleStrip(_sector, _layer, strip);
		double ang = geo.CRZ_GetAngleStrip(_sector, _layer, strip);

		int zstrip = geo.getZStrip(_layer, geo.CRZ_GetAngleStrip(_sector, _layer, strip));
		
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
		
//		public FeedbackRect(int x, int y, int w, int h, int index,
//				ADataContainer data, int opt, String... fbString) {
		
		String edepStr="";
		if (bmtData.bmt_dgtz_Edep != null) {
			edepStr = "  edep " + DoubleFormat.doubleFormat(bmtData.bmt_dgtz_Edep[hit],2);
		}
		
		_fbRects.add(new FeedbackRect(pp.x - 3, pp.y - 3, 6, 6, hit, bmtData, 0, 
				"hit " + hit  + " strip " + strip + edepStr));
		
//		System.err.println("FBRECTS LEN: " + _fbRects.size());
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

			feedbackStrings.add("$lawn green$" + UnicodeSupport.SMALL_MU + "megas sect: "
					+ sectorNames[_sector - 1] + " lay: " + _layer + " strip: " + zstrip);
			
			
			if (!_fbRects.isEmpty()) {
				for (FeedbackRect fbr : _fbRects) {
					if (fbr.contains(screenPoint, feedbackStrings)) {
						break;
					}
				}
			}
		}
	}
}
