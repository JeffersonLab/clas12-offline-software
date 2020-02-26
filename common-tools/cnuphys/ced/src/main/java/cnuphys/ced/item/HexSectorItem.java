package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.GeometryManager;

public abstract class HexSectorItem extends PolygonItem {

	private static final double TAN30 = 1.0 / Math.sqrt(3.0);

	protected static final Color TRANS = new Color(0, 0, 0, 48);

	public static final Stroke THINSTROKE = GraphicsUtilities.getStroke(0.0f,
			LineStyle.SOLID);
	public static final Stroke DASHTHINSTROKE = GraphicsUtilities.getStroke(
			0.0f, LineStyle.DASH);

	// 1-based sector
	protected int _sector;

	// midplane phi in radians and rotation params
	protected double _midPlanePhi;

	// the owner view
	private HexView _view;

	/**
	 * Get a hex sector item
	 * 
	 * @param layer
	 *            the logical layer
	 * @param sector
	 *            the 1-based sector
	 */
	public HexSectorItem(LogicalLayer layer, HexView view, int sector) {
		super(layer, getPoints(view.getContainer().getWorldSystem().getMinX(),
				sector));
		_sector = sector;
		// the view this item lives on.

		_view = view;
		_midPlanePhi = (Math.PI * (_sector - 1)) / 3;
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
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		super.drawItem(g, container);
		drawCenterLine(g, container);
	}

	// draw dashed center line through the sector
	protected void drawCenterLine(Graphics g, IContainer container) {
		Graphics2D g2 = (Graphics2D) g;

		Shape oldClip = g2.getClip();
		g2.setClip(_lastDrawnPolygon);
		Point2D.Double sp1 = new Point2D.Double(0, 0);
		Point2D.Double sp2 = new Point2D.Double(500, 0);
		drawDashedThinLine(g2, container, sp1, sp2, TRANS);

		g2.setClip(oldClip);
	}

	/**
	 * Draw a dashed thin line based on sector xy points
	 * 
	 * @param g2
	 *            graphics context
	 * @param container
	 *            the owner
	 * @param sp1
	 *            first sector point
	 * @param sp2
	 *            second sector point
	 * @param color
	 *            the line color
	 */
	protected void drawDashedThinLine(Graphics2D g2, IContainer container,
			Point2D.Double sp1, Point2D.Double sp2, Color color) {

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(DASHTHINSTROKE);
		g2.setColor(color);

		Point p1 = new Point();
		Point p2 = new Point();
		sector2DToLocal(container, p1, sp1);
		sector2DToLocal(container, p2, sp2);
		g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(oldStroke);
	}

	/**
	 * Draw a thin line based on sector xy points
	 * 
	 * @param g2
	 *            graphics context
	 * @param container
	 *            the owner
	 * @param sp1
	 *            first sector point
	 * @param sp2
	 *            second sector point
	 * @param color
	 *            the line color
	 */
	protected void drawThinLine(Graphics2D g2, IContainer container,
			Point2D.Double sp1, Point2D.Double sp2, Color color) {

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(THINSTROKE);
		g2.setColor(color);

		Point p1 = new Point();
		Point p2 = new Point();
		sector2DToLocal(container, p1, sp1);
		sector2DToLocal(container, p2, sp2);

		g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(oldStroke);
	}

	/**
	 * Get the sector
	 * 
	 * @return the 1-based sector [1..6]
	 */
	public int getSector() {
		return _sector;
	}

	// sector is 1-based
	// the use of xmax from the views world system makes the
	// world system the same as the lab 2D (xy) system!
	private static Point2D.Double[] getPoints(double xmax, int sector) {
		Point2D.Double[] points = GeometryManager.allocate(4);

		points[0].setLocation(xmax, -xmax * TAN30);
		points[1].setLocation(xmax, xmax * TAN30);
		points[2].setLocation(0, 0);

		if (sector != 1) {
			double midPhi = (Math.PI * (sector - 1)) / 3;
			rotatePoint(points[0], midPhi);
			rotatePoint(points[1], midPhi);
		}

		points[3].setLocation(points[0]);
		return points;
	}

	/**
	 * Rotate a point around the z axis
	 * 
	 * @param wp
	 *            the point being rotated
	 * @param phi
	 *            rotation angle in radians
	 */
	private static void rotatePoint(Point2D.Double wp, double phi) {
		double cosPhi = Math.cos(phi);
		double sinPhi = Math.sin(phi);
		double x = cosPhi * wp.x + -sinPhi * wp.y;
		double y = sinPhi * wp.x + cosPhi * wp.y;
		wp.setLocation(x, y);
	}

	/**
	 * Converts a sector xyz to lab (CLAS) xyz
	 * 
	 * @param sp
	 *            the sector point
	 * @param lab
	 *            the lab (CLAS) point
	 */
	protected void sectorXYZToLabXYZ(double sectorXYZ[], double labXYZ[]) {

		Point2D.Double sp = new Point2D.Double(sectorXYZ[0], sectorXYZ[1]);
		Point2D.Double lab = new Point2D.Double();
		sector2DToLab2D(sp, lab);
		labXYZ[0] = lab.x;
		labXYZ[1] = lab.y;
		labXYZ[2] = sectorXYZ[2];
	}

	/**
	 * Converts a lab (CLAS) xy to sector
	 * 
	 * @param sp
	 *            the sector point
	 * @param lab
	 *            lab xy point
	 */
	protected void lab2DToSector2D(Point2D.Double sp, Point2D.Double lab) {
		sp.setLocation(lab);
		if (_sector != 1) {
			rotatePoint(sp, -_midPlanePhi);
		}
	}

	/**
	 * Converts a sector to lab (CLAS) xy
	 * 
	 * @param sp
	 *            the sector point
	 * @param lab
	 *            the lab (CLAS) point
	 */
	protected void sector2DToLab2D(Point2D.Double sp, Point2D.Double lab) {
		lab.setLocation(sp);
		if (_sector != 1) {
			rotatePoint(lab, _midPlanePhi);
		}
	}

	/**
	 * World graphical coordinates to sector coordinates.
	 * 
	 * @param sp
	 *            will hold the sector coordinates
	 * @param wp
	 *            the graphical world coordinates
	 */
	protected void worldToSector2D(Point2D.Double sp, Point2D.Double wp) {
		lab2DToSector2D(sp, wp);
	}

	/**
	 * Sector to local screen coordinates
	 * 
	 * @param pp
	 *            will hold the screen coordinates
	 * @param sp
	 *            the sector coordinates
	 */
	public void sector2DToLocal(IContainer container, Point pp,
			Point2D.Double sp) {
		Point2D.Double wp = new Point2D.Double();

		// world are same as lab
		sector2DToLab2D(sp, wp);
		container.worldToLocal(pp, wp);
	}

	/**
	 * local screen coordinates to sector coordinates
	 * 
	 * @param pp
	 *            the screen coordinates
	 * @param sp
	 *            will hold the sector coordinates
	 */
	public void localToSector2D(IContainer container, Point pp,
			Point2D.Double sp) {
		Point2D.Double wp = new Point2D.Double();
		container.localToWorld(pp, wp);
		// world are same as lab
		sector2DToLab2D(sp, wp);
	}

	// convenience call for double formatter
	protected String valStr(double value, int numdec) {
		return DoubleFormat.doubleFormat(value, numdec);
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec
	 *            the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	protected String vecStr(double v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 2) + ", "
				+ DoubleFormat.doubleFormat(v[1], 2) + ", "
				+ DoubleFormat.doubleFormat(v[2], 2) + ")";
	}

}
