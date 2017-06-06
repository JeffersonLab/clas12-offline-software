package cnuphys.ced.geometry.bmt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.IStyled;
import cnuphys.bCNU.item.DonutItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.lund.X11Colors;

public class BMTSectorItem extends DonutItem {
	
	public enum LAYERTYPE {C, Z};
	
	public static final Color cColor = X11Colors.getX11Color("sea green", 96);
	public static final Color zColor = X11Colors.getX11Color("orange", 96);
	
	private static LAYERTYPE[] layerTypes = 
		{LAYERTYPE.C, LAYERTYPE.Z, LAYERTYPE.Z, 
				LAYERTYPE.C, LAYERTYPE.Z, LAYERTYPE.C};
	
	private static final String[] sectorNames = {"A[2]", "B[1]", "C[3]"};
//	private static final String[] sectorNames = {"C[3]", "A[2]", "B[1]"};
//	private static final double labelAngs[] = {90, 225, 325};
//	private static final double labelRad[] = {230, 255, 250};
	private static final double labelAngs[] = {225, 325, 90};
	private static final double labelRad[] = {255, 250, 230};
//	private static final int xoff[] = {0, -30, 4};
	private static final int xoff[] = {-30, 4, 0};

	private static double[][] startAngle = new double[3][6];
	private static double[][] stopAngle = new double[3][6];
	private static double[][] delAngle = new double[3][6];

	private static double[] innerRadius = new double[6];
	private static double[] outerRadius = new double[6];
	
	private LAYERTYPE _layerType;
	

	public static final String microMegasStr = UnicodeSupport.SMALL_MU + "megas";
	
	static {
		Geometry geo = BMTGeometry.getGeometry();
		
		for (int sector = 1; sector <= 3; sector++) {
			for (int layer = 1; layer <= 6; layer++) {
				
//				double beginAng = Math
//						.toDegrees(geo.CRC_GetBeginStrip(sector, layer));
//				double endAng = Math
//						.toDegrees(geo.CRC_GetEndStrip(sector, layer));

				double beginAng = Math.toDegrees(geo.GetStartAngle(sector, layer));
				double endAng = Math.toDegrees(geo.GetEndAngle(sector, layer));
				
				
//				if (sector == 1) {
//					beginAng = 0;
//					endAng = 30;
//				}
//				else if (sector == 2) {
//					beginAng = 90;
//					endAng = 120;
//				}
//				else if (sector == 3) {
//					beginAng = 230;
//					endAng = 260;
//				}
				
				startAngle[sector - 1][layer - 1] = beginAng;
				stopAngle[sector - 1][layer - 1] = endAng;

				delAngle[sector - 1][layer - 1] = endAng - beginAng;
				if (delAngle[sector - 1][layer - 1] < 0) {
					delAngle[sector - 1][layer - 1] += 360;
				}
				
				if (layer == 1)
				System.err.println("BMT sector " + sector + " layer " + layer + 
						"   startAng: " + DoubleFormat.doubleFormat(beginAng, 1) + "   endAng: " + DoubleFormat.doubleFormat(endAng, 1));
			}
		}
		
		innerRadius[0] = Constants.getCRCRADIUS()[0];
		innerRadius[1] = Constants.getCRZRADIUS()[0];
		innerRadius[2] = Constants.getCRZRADIUS()[1];
		innerRadius[3] = Constants.getCRCRADIUS()[1];
		innerRadius[4] = Constants.getCRZRADIUS()[2];
		innerRadius[5] = Constants.getCRCRADIUS()[2];
		
		for (int layer0 = 0; layer0 < 6; layer0++) {
			outerRadius[layer0] = innerRadius[layer0] + 4;
		}

////		innerRadius[4] = 205.8; // layer 5 mm
//		outerRadius[4] = innerRadius[4] + 4; // layer 5 mm
////		innerRadius[5] = 220.8; // layer 6 mm
//		outerRadius[5] = innerRadius[5] + 4; // layer 6 mm
	}
	
	// sector and layer are one based
	private int _sector;
	private int _layer;

	// public DonutItem(LogicalLayer layer, Point2D.Double wpc,
	// double radiusInner, double radiusOuter, double startAngle,
	// double arcAngle) {

	public BMTSectorItem(LogicalLayer logLayer, int sector, int layer) {
		super(logLayer, new Point2D.Double(0, 0), innerRadius[layer - 1],
				outerRadius[layer - 1], startAngle[sector - 1][layer - 1],
				delAngle[sector - 1][layer - 1]);

		_sector = sector;
		_layer = layer;
		_layerType = layerTypes[layer-1];
		
		IStyled style = getStyle();
		if (_layerType == LAYERTYPE.C) {
			style.setFillColor(cColor);
		}
		else {
			style.setFillColor(zColor);
		}
		
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
	
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
		boolean oldSVTGeometry = Ced.getCed().useOldSVTGeometry();
		if (oldSVTGeometry && (_layer < 5)) {
			return false;
		}
		return super.shouldDraw(g, container);
	}
	
	@Override
	public boolean contains(IContainer container, Point screenPoint) {
		boolean oldSVTGeometry = Ced.getCed().useOldSVTGeometry();
		if (oldSVTGeometry && (_layer < 5)) {
			return false;
		}
		return super.contains(container, screenPoint);
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
//		
//		if ((_layer % 2) == 0) {
//			return null;
//		}
//		
//		int strips[] = BMT.strip();
//		double edep[] = BMT.Edep();
//
//		int strip = strips[hit];
//		
//		//double ang = Math.PI-geo.CRZ_GetAngleStrip(_sector, _layer, strip);
//		double ang = geo.CRZ_GetAngleStrip(_sector, _layer, strip);
//		
//		if (ang > Math.PI) {
//			ang -= (2 * Math.PI);
//		}
////		System.err.println("SECT: " + sectorNames[_sector-1] + "  LAYER: " + _layer +
////				" STRIP: " + strip + " Zstrip: " + zstrip + 
////				"  ANGLE: " + Math.toDegrees(ang));
//
//		double rad = (innerRadius[_layer - 1] + outerRadius[_layer - 1]) / 2;
//		Point2D.Double wp = new Point2D.Double(rad * Math.cos(ang),
//				rad * Math.sin(ang));
//		Point pp = new Point();
//		container.worldToLocal(pp, wp);
//		g.setColor(fillColor);
//		g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
//		g.setColor(lineColor);
//		g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
//				
//		String edepStr="";
//		if (edep != null) {
//			edepStr = "  edep " + DoubleFormat.doubleFormat(edep[hit],2);
//		}
//		
//		return new FeedbackRect(FeedbackRect.Dtype.BMT, pp.x - 3, pp.y - 3, 6, 6, hit, 0, 
//				microMegasStr + " hit " + hit  + " strip " + strip + edepStr);
		return null;
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
			int zstrip = BMTGeometry.getGeometry().getZStrip(_layer, ang);

			feedbackStrings.add("$lawn green$" + microMegasStr + " type " + _layerType);
			feedbackStrings.add("$lawn green$" + microMegasStr + " sector " + _sector + " = "
					+ sectorNames[_sector - 1] + " layer " + _layer + " strip " + zstrip);			
		}
	}
}
