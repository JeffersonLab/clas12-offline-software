package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.DCGeometry;

public class DCHexSectorItem extends HexSectorItem {

	// superlayer polygon
	private Polygon polys[] = new Polygon[6];
	private static final Color suplColor = new Color(255, 0, 0, 8);

	/**
	 * Get a hex sector item
	 * 
	 * @param logLayer
	 *            the logical layer
	 * @param sector
	 *            the 1-based sector
	 */
	public DCHexSectorItem(LogicalLayer logLayer, DCXYView view, int sector) {
		super(logLayer, view, sector);
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

		g.setColor(Color.white);
		for (int supl = 1; supl <= 6; supl++) {
			polys[supl - 1] = superLayerPoly(container, supl);
			g.fillPolygon(polys[supl - 1]);
		}

		for (int supl = 1; supl <= 6; supl++) {
			g.setColor(suplColor);
			g.fillPolygon(polys[supl - 1]);
		}

	}

	// supl is 1-based
	private Polygon superLayerPoly(IContainer container, int supl) {
		Polygon poly = new Polygon();
		Point pp = new Point();
		Point2D.Double workPoint = new Point2D.Double();

		for (int layer = 1; layer <= 6; layer++) {
			wireToLocal(container, supl, layer, 1, 0, pp, workPoint);
			poly.addPoint(pp.x, pp.y);
			wireToLocal(container, supl, layer, 112, 0, pp, workPoint);
			poly.addPoint(pp.x, pp.y);
		}

		for (int layer = 6; layer >= 1; layer--) {
			wireToLocal(container, supl, layer, 112, 1, pp, workPoint);
			poly.addPoint(pp.x, pp.y);
			wireToLocal(container, supl, layer, 1, 1, pp, workPoint);
			poly.addPoint(pp.x, pp.y);
		}

		// close poly
		// poly.addPoint(poly.xpoints[0], poly.ypoints[0]);
		return poly;
	}

	/**
	 * Wire endpoints
	 * 
	 * @param superlayer
	 *            the superlayer 1..6
	 * @param layer
	 *            the layer 1..6
	 * @param wire
	 *            the wire 1..112
	 * @param end
	 *            0 or 1 for opposite ends of the wire
	 */
	private void wireToLocal(IContainer container, int superlayer, int layer,
			int wire, int end, Point pp, Point2D.Double workPoint) {

		Point3D wireEnd = null;
		if (end == 0) {
			wireEnd = DCGeometry.getOrigin(superlayer, layer, wire);
		} else {
			wireEnd = DCGeometry.getEnd(superlayer, layer, wire);
		}

		workPoint.x = wireEnd.x();
		workPoint.y = wireEnd.y();
		sector2DToLocal(container, pp, workPoint);

	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {

			// have no z info, just lab x, y, phy
			double labRho = Math.hypot(wp.x, wp.y);
			double labPhi = Math.atan2(wp.y, wp.x);

			String labXY = String.format("$yellow$lab xy (%-6.2f, %-6.2f) ",
					wp.x, wp.y);

			String labRhoPhi = String.format("$yellow$lab " + CedView.rhoPhi
					+ " (%-6.2f, %-6.2f)", labRho, (Math.toDegrees(labPhi)));

			Point2D.Double sect2D = new Point2D.Double();
			worldToSector2D(sect2D, wp);
			double sectRho = Math.hypot(sect2D.x, sect2D.y);
			double sectPhi = Math.atan2(sect2D.y, sect2D.x);

			String sectXY = String.format(
					"$orange$sector xy (%-6.2f, %-6.2f) ", sect2D.x, sect2D.y);

			String sectRhoPhi = String.format("$orange$sector "
					+ CedView.rhoPhi + " (%-6.2f, %-6.2f)", sectRho,
					(Math.toDegrees(sectPhi)));

			feedbackStrings.add(labXY);
			feedbackStrings.add(labRhoPhi);
			feedbackStrings.add(sectXY);
			feedbackStrings.add(sectRhoPhi);
		} // end contains

	}

}
