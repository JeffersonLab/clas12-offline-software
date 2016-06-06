package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.allpcal.PCALView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.HitRecord;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.PCALGeometry;

public class PCALHexSectorItem extends HexSectorItem {

	// the view owner
	private PCALView _pcalView;

	public static final Color baseFillColor = new Color(139, 0, 0, 160);

	private static int[] _stripCounts = PCALGeometry.PCAL_NUMSTRIP; //u,v,w

	/**
	 * Get a hex sector item
	 * 
	 * @param layer the logical layer
	 * @param sector the 1-based sector
	 */
	public PCALHexSectorItem(LogicalLayer layer, PCALView view, int sector) {
		super(layer, view, sector);
		_pcalView = view;
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		super.drawItem(g, container);

		for (int stripType = 0; stripType < 3; stripType++) {
			if (_pcalView.showStrips(stripType)) {
				for (int stripIndex = 0; stripIndex < PCALGeometry.PCAL_NUMSTRIP[stripType]; stripIndex++) {
					Polygon poly = stripPolygon(container, stripType,
							stripIndex);

					// _stripPoly[stripType][stripIndex] = poly;
					g.setColor(Color.white);
					g.fillPolygon(poly);
				}
			}
		}

		drawOutlines(g, container, Color.lightGray);

		if (PCAL.hitCount() > 0) {
			drawPCALHits(g, container);
		}

		drawIJKOrigin(g, container);

	}

	// draw strip outlines
	private void drawOutlines(Graphics g, IContainer container, Color color) {
		for (int stripType = 0; stripType < 3; stripType++) {
			if (_pcalView.showStrips(stripType)) {
				for (int stripIndex = 0; stripIndex < PCALGeometry.PCAL_NUMSTRIP[stripType]; stripIndex++) {
					// g.drawPolygon(_stripPoly[stripType][stripIndex]);

					g.setColor(color);
					Polygon poly = stripPolygon(container, stripType,
							stripIndex);

					g.drawPolygon(poly);
				}
			}
		}
	}
	// draw the hits
	private void drawPCALHits(Graphics g, IContainer container) {		
		if (_pcalView.isSingleEventMode()) {
			drawSingleEvent(g, container);
		}
		else {
			drawAccumulatedHits(g, container);
		}
	}

	//draw single event hit
	private void drawSingleEvent(Graphics g, IContainer container) {
		
		int hitCount = PCAL.hitCount();
		if (hitCount > 0) {
			int sector[] = PCAL.sector();
			int view[] = PCAL.view();
			int strip[] = PCAL.strip();
			double totEdep[] = PCAL.totEdep();
			
			for (int i = 0; i < hitCount; i++) {
				if (sector[i] == getSector()) {
					int view0 = view[i] - 1; // uvw
					int strip0 = strip[i] - 1;
					if (_pcalView.showStrips(view0)) {
						Polygon poly = stripPolygon(container, view0, strip0);

						// Polygon poly = _stripPoly[view0][strip0];

						// if mctruth and have energy deposited, use it
						if (_pcalView.showMcTruth() && (totEdep != null)) {

							int alpha = (int) ((255 * totEdep[i])
									/ (ClasIoEventManager.getInstance()
											.getMaxEdepCal(0)));
							alpha = Math.max(60, Math.min(255, alpha));
							g.setColor(new Color(255, 0, 0, alpha));
						}
						else {
							g.setColor(baseFillColor);
						}
						g.fillPolygon(poly);
						g.drawPolygon(poly);
					}
				}
			} //end for loop
		} // hitCount > 0
	}
	
	//draw accumulated hits
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		
		int maxHit = AccumulationManager.getInstance().getMaxDgtzPcalCount();
		if (maxHit < 1) {
			return;
		}
		
		int hits[][][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzPcalData();

		int sect0 = getSector() - 1;

		for (int view0 = 0; view0 < 3; view0++) {
			for (int strip0 = 0; strip0 < _stripCounts[view0]; strip0++) {
				if (_pcalView.showStrips(view0)) {
					Polygon poly = stripPolygon(container, view0, strip0);

					int hit = hits[sect0][view0][strip0];
					if (hit > 0) {
						double fract;
						if (_pcalView.isSimpleAccumulatedMode()) {
							fract = ((double) hit) / maxHit;
						}
						else {
							fract = Math.log(hit + 1.)
									/ Math.log(maxHit + 1.);
						}

						Color color = AccumulationManager.colorScaleModel
								.getAlphaColor(fract, 128);

						g.setColor(color);
						g.fillPolygon(poly);
					}
				}

			}
		}

	}


	// mark the origin og the ijk system
	private void drawIJKOrigin(Graphics g, IContainer container) {
		Point3D orig = new Point3D(0, 0, 0);
		Point pp = new Point();
		ijkToScreen(container, orig, pp);
		g.setColor(new Color(0, 0, 0, 64));
		g.fillOval(pp.x - 5, pp.y - 5, 10, 10);
		g.setColor(Color.cyan);
		g.drawLine(pp.x - 4, pp.y - 4, pp.x + 4, pp.y + 4);
		g.drawLine(pp.x - 4, pp.y + 4, pp.x + 4, pp.y - 4);
	}

	/**
	 * Convert ijk coordinates to world graphics coordinates
	 * 
	 * @param pijk the ijk coordinates
	 * @param pp the screen coordinates
	 */
	public void ijkToScreen(IContainer container, Point3D pijk, Point pp) {
		Point2D.Double wp = new Point2D.Double();
		ijkToWorld(pijk, wp);
		container.worldToLocal(pp, wp);
	}

	/**
	 * Convert ijk coordinates to world graphics coordinates
	 * 
	 * @param pijk the ijk coordinates
	 * @param wp the world graphics coordinates
	 */
	public void ijkToWorld(Point3D pijk, Point2D.Double wp) {
		double sectorXYZ[] = new double[3];
		double labXYZ[] = new double[3];
		_pcalView.ijkToSectorXYZ(pijk, sectorXYZ);
		GeometryManager.sectorXYZToLabXYZ(_sector, labXYZ, sectorXYZ);
		GeometryManager.cal_labXYZToWorld(0, labXYZ, wp);
	}

	/**
	 * Get the polygon for a u, v or w strip
	 * 
	 * @param stripType PCAL_U, PCAL_V, or PCAL_W [0..2]
	 * @param stripIndex the strip index [0..(EC_NUMSTRIP-1)]
	 * @return
	 */
	public Polygon stripPolygon(IContainer container, int stripType,
			int stripIndex) {
		Polygon poly = new Polygon();
		Point pp = new Point();

		for (int i = 0; i < 4; i++) {
			Point3D pijk = PCALGeometry.getStripPoint(stripType, stripIndex, i);
			ijkToScreen(container, pijk, pp);
			poly.addPoint(pp.x, pp.y);
		}

		return poly;

	}

	/**
	 * Converts a graphical world point to sector xyz
	 * 
	 * @param wp the world graphical point
	 * @param sectorXYZ the sector xyz point
	 */
	public void worldToSectorXYZ(Point2D.Double wp, double[] sectorXYZ) {
		Point2D.Double setct2D = new Point2D.Double();
		worldToSector2D(setct2D, wp);
		sectorXYZ[0] = setct2D.x;
		sectorXYZ[1] = setct2D.y;
		sectorXYZ[2] = PCALGeometry.zFromX(setct2D.x);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {

			// get the sector xyz coordinates
			double sectorXYZ[] = new double[3];
			worldToSectorXYZ(wp, sectorXYZ);

			Point3D sp = new Point3D(sectorXYZ[0], sectorXYZ[1], sectorXYZ[2]);
			Point3D lp = new Point3D();
			PCALGeometry.getTransformations().sectorToLocal(lp, sp);

			// sector rho phi
			double sectRho = Math.hypot(sectorXYZ[0], sectorXYZ[1]);
			double sectPhi = Math.atan2(sectorXYZ[1], sectorXYZ[0]);

			// get the lab xyz
			double labXYZ[] = new double[3];
			sectorXYZToLabXYZ(sectorXYZ, labXYZ);

			// TEST
			// Point3D labP = new Point3D();
			// ECGeometry.localToLab(getSector(), plane, lp, labP);
			// feedbackStrings.add("TEST LAB: " + labP);

			// lab rho phy
			double labRho = Math.hypot(labXYZ[0], labXYZ[1]);
			double labPhi = Math.atan2(labXYZ[1], labXYZ[0]);

			// get the uvw indices
			int uvw[] = new int[3];
			localToUVW(container, uvw, pp);

			String labxyz = "$yellow$lab xyz " + vecStr(labXYZ) + " cm";
			feedbackStrings.add(labxyz);
			String labRhoPhi = String.format(
					"$yellow$lab " + CedView.rhoPhi + " (%-6.2f, %-6.2f)",
					labRho, (Math.toDegrees(labPhi)));
			feedbackStrings.add(labRhoPhi);

			String sectxyz = "$orange$sector xyz " + vecStr(sectorXYZ) + " cm";
			feedbackStrings.add(sectxyz);
			String sectRhoPhi = String.format(
					"$orange$sector " + CedView.rhoPhi + " (%-6.2f, %-6.2f)",
					sectRho, (Math.toDegrees(sectPhi)));
			feedbackStrings.add(sectRhoPhi);

			// now add the strings
			// if ((uvw[0] > 0) && (uvw[1] > 0) && (uvw[2] > 0)) {
			if (true) {

				String locStr = "$lime green$loc xyz " + point3DString(lp) + " cm";
				feedbackStrings.add(locStr);

				if ((uvw[0] > 0) && (uvw[1] > 0) && (uvw[2] > 0)) {
					String uvwStr = "$lime green$U V W [" + uvw[0] + ", " + uvw[1] + ", " + uvw[2] + "]";
					feedbackStrings.add(uvwStr);
				}

				// any hits?

				if ((uvw[0] > 0) && (uvw[1] > 0) && (uvw[2] > 0)) {
					for (int stripType = 0; stripType < 3; stripType++) {
						if (_pcalView.showStrips(stripType)) {
							Vector<HitRecord> hits = PCAL.matchingHits(
									getSector(), stripType + 1,
									uvw[stripType]);

							if (hits != null) {
								for (HitRecord hit : hits) {
									PCAL.preliminaryFeedback(hit.hitIndex, feedbackStrings);
									DataSupport.truePidFeedback(PCAL.pid(), hit.hitIndex, feedbackStrings);
									PCAL.dgtzFeedback(hit.hitIndex, feedbackStrings);
								}
							}
						}
					}
				}

			}

		} // end contains

	}

	private String point3DString(Point3D p3d) {
		return String.format("(%-6.3f, %-6.3f, %-6.3f)", p3d.x(), p3d.y(),
				p3d.z());
	}

	// convert screen point to a uvw 1-based triplet
	private void localToUVW(IContainer container, int uvw[], Point pp) {
		for (int stripType = 0; stripType < 3; stripType++) {
			uvw[stripType] = -1;
			for (int stripIndex = 0; stripIndex < PCALGeometry.PCAL_NUMSTRIP[stripType]; stripIndex++) {

				// Polygon poly = _stripPoly[stripType][stripIndex];

				Polygon poly = stripPolygon(container, stripType, stripIndex);

				if ((poly != null) && (poly.contains(pp))) {
					uvw[stripType] = stripIndex + 1;
					break;
				}
			}
		}
	}
}
