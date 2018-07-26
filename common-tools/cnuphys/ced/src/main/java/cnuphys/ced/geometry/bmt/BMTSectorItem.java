package cnuphys.ced.geometry.bmt;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
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

public class BMTSectorItem extends DonutItem {
	
	public enum LAYERTYPE {C, Z};
	
	public static final Color cColor = new Color(220, 255, 220);
	public static final Color zColor = new Color(240, 240, 240);
	
	//the layervtypes, note from inside out they go C, Z, Z, C, Z, C
	private static LAYERTYPE[] layerTypes = 
		{LAYERTYPE.C, LAYERTYPE.Z, LAYERTYPE.Z, 
				LAYERTYPE.C, LAYERTYPE.Z, LAYERTYPE.C};
	
	private static final String[] _sectorNames = {"1", "3", "2"};
	private static final double labelAngs[] = {225, 315, 90};
//	private static final double labelRad = 238;
	private static final double labelRad = 231;
	
	private static double[][] startAngle = new double[3][6];
	private static double[][] endAngle = new double[3][6];
	private static double[][] delAngle = new double[3][6];

	public static double[] innerRadius = new double[6];
	public static double[] outerRadius = new double[6];
		
	private LAYERTYPE _layerType;
	
	public static final double FAKEWIDTH = Constants.LYRTHICKN; 

	public static final String microMegasStr = UnicodeSupport.SMALL_MU + "megas";
	
	static {
		Geometry geo = BMTGeometry.getGeometry();
		
		for (int sector = 1; sector <= 3; sector++) {
			for (int layer = 1; layer <= 6; layer++) {
				

				double beginAng = Math.toDegrees(geo.GetStartAngle(sector, layer));
				double endAng = Math.toDegrees(geo.GetEndAngle(sector, layer));
								
				startAngle[sector - 1][layer - 1] = beginAng;
				endAngle[sector - 1][layer - 1] = endAng;

				delAngle[sector - 1][layer - 1] = endAng - beginAng;
				if (delAngle[sector - 1][layer - 1] < 0) {
					delAngle[sector - 1][layer - 1] += 360;
				}
				
			}
		}
		
		innerRadius[0] = Constants.getCRCRADIUS()[0];
		innerRadius[1] = Constants.getCRZRADIUS()[0];
		innerRadius[2] = Constants.getCRZRADIUS()[1];
		innerRadius[3] = Constants.getCRCRADIUS()[1];
		innerRadius[4] = Constants.getCRZRADIUS()[2];
		innerRadius[5] = Constants.getCRCRADIUS()[2];
		
		for (int layer0 = 0; layer0 < 6; layer0++) {
			outerRadius[layer0] = innerRadius[layer0] + FAKEWIDTH; //mm
		}

////		innerRadius[4] = 205.8; // layer 5 mm
//		outerRadius[4] = innerRadius[4] + 4; // layer 5 mm
////		innerRadius[5] = 220.8; // layer 6 mm
//		outerRadius[5] = innerRadius[5] + 4; // layer 6 mm
	}
	
	/**
	 * Get the inner radius in mm
	 * @return the inner radius in mm
	 */
	public double getInnerRadius() {
		return innerRadius[_layer-1];
	}
	
	/**
	 * Get the start angle in degrees
	 * @return the start angle in degrees
	 */
	public double getStartAngle() {
		return startAngle[_sector-1][_layer-1];
	}
	
	/**
	 * Get the end angle in degrees
	 * @return the end angle in degrees
	 */
	public double getEndAngle() {
		return endAngle[_sector-1][_layer-1];
	}

	
	/**
	 * Get the outer radius in mm
	 * @return the outer radius in mm
	 */
	public double getOuterRadius() {
		return outerRadius[_layer-1];
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
			String s = _sectorNames[_sector - 1];

			Point2D.Double wp = new Point2D.Double();
			wp.x = labelRad*Math.cos(Math.toRadians(labelAngs[sm1]));
			wp.y = labelRad*Math.sin(Math.toRadians(labelAngs[sm1]));
			Point pp = new Point();
			container.worldToLocal(pp, wp);
			
			g.setColor(Color.blue);
			FontMetrics fm = container.getComponent().getFontMetrics(Fonts.defaultFont);
			int sw = fm.stringWidth(s);
			g.drawString(s, pp.x-sw/2, pp.y+fm.getAscent()/2);
		}
		
		g2.setClip(oldClip);
	}
	
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
		boolean oldBSTGeometry = Ced.getCed().useOldBSTGeometry();
		if (oldBSTGeometry && (_layer < 5)) {
			return false;
		}
		return super.shouldDraw(g, container);
	}
	
	@Override
	public boolean contains(IContainer container, Point screenPoint) {
		boolean oldBSTGeometry = Ced.getCed().useOldBSTGeometry();
		if (oldBSTGeometry && (_layer < 5)) {
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
	 * Convenience method to check if this is a Z layer
	 * @return <code>true</code> if this is a Z layer
	 */
	public boolean isZLayer() {
		return _layerType == LAYERTYPE.Z;
	}
	
	/**
	 * Convenience method to check if this is a C layer
	 * @return <code>true</code> if this is a C layer
	 */
	public boolean isCLayer() {
		return _layerType == LAYERTYPE.C;
	}


	/**
	 * Add any appropriate feedback strings
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
	//		double ang  = Math.PI-Math.atan2(wp.y, wp.x);
			double ang  = Math.atan2(wp.y, wp.x);

			int zstrip = -1;

			if (isZLayer()) {
				zstrip = BMTGeometry.getGeometry().getZStrip(_layer, ang);
			}

			feedbackStrings.add("$lawn green$" + microMegasStr + " type " + _layerType);
			feedbackStrings.add("$lawn green$" + microMegasStr + " sector " + _sector + " = "
					+ _sectorNames[_sector - 1] + " layer " + _layer + " strip " + 
					((zstrip < 0) ? " N/A " : zstrip) );
			
			feedbackStrings.add("$lawn green$inner radius " + getInnerRadius() + "mm");
			feedbackStrings.add("$lawn green$startAngle " + DoubleFormat.doubleFormat(getStartAngle(), 1) + UnicodeSupport.DEGREE + 
			" endAngle " + DoubleFormat.doubleFormat(getEndAngle(), 1) + UnicodeSupport.DEGREE);
			feedbackStrings.add("$lawn green$number of strips " + getNumStrips());
		}
	}
	
	/**
	 * Get the number of strips
	 * @return the number of strips
	 */
	public int getNumStrips () {
		int region = (_layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6

		if (isZLayer()) {
			return Constants.getCRZNSTRIPS()[region];
		}
		else { //C
			return Constants.getCRCNSTRIPS()[region];
		}
	}
	
	public Polygon getStripPolygon(IContainer container, int strip) {
//		Polygon poly = null;
//		if (isZLayer()) {
//			Point pp = new Point();
//			double phi = BMTGeometry.getGeometry().CRZStrip_GetPhi(_sector, 
//					_layer, strip);
//			
//			double phi2;
//			if (strip < (getNumStrips()/2)) {
//				phi2 = BMTGeometry.getGeometry().CRZStrip_GetPhi(_sector, 
//						_layer, strip+1);
//			}
//			else {
//				phi2 = BMTGeometry.getGeometry().CRZStrip_GetPhi(_sector, 
//						_layer, strip-1);
//			}
//			double delPhi = (Math.abs(phi2-phi))/2;
//			
//			double phiMin = phi - delPhi;
//			double phiMax = phi + delPhi;
//			double cmin = Math.cos(phiMin);
//			double cmax = Math.cos(phiMax);
//			double smin = Math.sin(phiMin);
//			double smax = Math.sin(phiMax);
//			
//			poly = new Polygon();
//			
//			container.worldToLocal(pp, getInnerRadius()*cmin, getInnerRadius()*smin);
//			poly.addPoint(pp.x, pp.y);
//			container.worldToLocal(pp, getOuterRadius()*cmin, getOuterRadius()*smin);
//			poly.addPoint(pp.x, pp.y);
//			container.worldToLocal(pp, getOuterRadius()*cmax, getOuterRadius()*smax);
//			poly.addPoint(pp.x, pp.y);
//			container.worldToLocal(pp, getInnerRadius()*cmax, getInnerRadius()*smax);
//			poly.addPoint(pp.x, pp.y);
//			
//		}
//		
//		return poly;
		
		return this._lastDrawnPolygon;
	}
}
