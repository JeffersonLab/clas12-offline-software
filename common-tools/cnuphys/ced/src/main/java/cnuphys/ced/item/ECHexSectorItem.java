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
import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.allec.ECView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.splot.plot.X11Colors;

/**
 * This is the "hex item" specific for EC views
 * 
 * @author heddle
 * 
 */
public class ECHexSectorItem extends HexSectorItem {

	// the view owner
	private ECView _ecView;

	public static final Color baseFillColor = new Color(139, 0, 0, 160);

	/**
	 * Get a hex sector item
	 * 
	 * @param layer  the logical layer
	 * @param sector the 1-based sector
	 */
	public ECHexSectorItem(LogicalLayer layer, ECView view, int sector) {
		super(layer, view, sector);
		_ecView = view;
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		super.drawItem(g, container);

		boolean inner = _ecView.displayInner();
		int plane = inner ? ECGeometry.EC_INNER : ECGeometry.EC_OUTER;

		for (int stripType = 0; stripType < 3; stripType++) {
			if (_ecView.showStrips(stripType)) {
				for (int stripIndex = 0; stripIndex < ECGeometry.EC_NUMSTRIP; stripIndex++) {

					Polygon poly = stripPolygon(container, plane, stripType, stripIndex);

					g.setColor(Color.white);
					g.fillPolygon(poly);

					// extension
					poly = extensionPolygon(container, plane, stripType, stripIndex, 1.0);
					g.setColor(X11Colors.getX11Color("Antique White", 128));
					g.fillPolygon(poly);

				}
			}
		}

		drawOutlines(g, container, plane, Color.lightGray);

		drawECHits(g, container, plane);

		drawIJKOrigin(g, container);

	}

	// draw strip outlines
	private void drawOutlines(Graphics g, IContainer container, int plane, Color color) {
		for (int stripType = 0; stripType < 3; stripType++) {
			if (_ecView.showStrips(stripType)) {
				for (int stripIndex = 0; stripIndex < ECGeometry.EC_NUMSTRIP; stripIndex++) {

					Polygon poly = stripPolygon(container, plane, stripType, stripIndex);

					g.setColor(color);
					// g.drawPolygon(_stripPoly[stripType][stripIndex]);
					g.drawPolygon(poly);

					// extension
					poly = extensionPolygon(container, plane, stripType, stripIndex, 1.0);
					g.setColor(X11Colors.getX11Color("coral", 128));
					g.drawPolygon(poly);

				}
			}
		}
	}

	// draw the hits
	private void drawECHits(Graphics g, IContainer container, int plane) {
		if (_ecView.isSingleEventMode()) {
			drawSingleEvent(g, container, plane);
		} else {
			drawAccumulatedHits(g, container, plane);
		}
	}

	// draw single event hit
	private void drawSingleEvent(Graphics g, IContainer container, int plane) {

		TdcAdcHitList hits = AllEC.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (TdcAdcHit hit : hits) {
				if (hit != null) {
					try {
						if ((hit.sector == getSector()) && (hit.layer > 3)) {
							int layer = hit.layer - 4; // 0..5
							int stack0 = layer / 3; // 000,111
							if (stack0 == plane) {
								int view0 = layer % 3; // 012012
								int strip0 = hit.component - 1;

								Polygon poly = stripPolygon(container, plane, view0, strip0);
								g.setColor(hits.adcColor(hit, AllEC.getInstance().getMaxECALAdc()));
								g.fillPolygon(poly);
								g.drawPolygon(poly);

								// extension

								int adcmax = Math.max(1, AllEC.getInstance().getMaxECALAdc());
								double fract = ((double) hit.averageADC() / (double) adcmax);
								fract = Math.max(0.15, Math.min(1.0, fract));

								// System.err.println("sector: " + hit.sector + " layer: " + hit.layer + " avg
								// ADC: " + hit.averageADC() + " " + " MAX: " +
								// AllEC.getInstance().getMaxECALAdc() + " fract: " + fract);

								poly = extensionPolygon(container, plane, view0, strip0, fract);
								g.setColor(Color.yellow);
								g.fillPolygon(poly);
								g.setColor(X11Colors.getX11Color("dark red"));
								g.drawPolygon(poly);

							}

						}
					} catch (Exception e) {
						Log.getInstance().exception(e);
					}
				} // hit not null
				else {
					Log.getInstance().warning("[ECHexSectorItem] null hit in ECAll hit list");
				}
			}
		}
	}

	// draw accumulated hits
	private void drawAccumulatedHits(Graphics g, IContainer container, int plane) {

		int medianHit = AccumulationManager.getInstance().getMedianECALCount(plane);

		int hits[][][][] = AccumulationManager.getInstance().getAccumulatedECALData();

		int sect0 = getSector() - 1;

		for (int view0 = 0; view0 < 3; view0++) {
			for (int strip0 = 0; strip0 < 36; strip0++) {
				if (_ecView.showStrips(view0)) {
					Polygon poly = stripPolygon(container, plane, view0, strip0);

					int hitCount = hits[sect0][plane][view0][strip0];
					if (hitCount > 0) {
						double fract = _ecView.getMedianSetting() * (((double) hitCount) / (1 + medianHit));

						Color color = _ecView.getColorScaleModel().getAlphaColor(fract, 128);

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
	 * @param pp   the screen coordinates
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
	 * @param wp   the world graphics coordinates
	 */
	public void ijkToWorld(Point3D pijk, Point2D.Double wp) {
		double sectorXYZ[] = new double[3];
		double labXYZ[] = new double[3];
		_ecView.ijkToSectorXYZ(pijk, sectorXYZ);
		GeometryManager.sectorXYZToLabXYZ(_sector, labXYZ, sectorXYZ);
		_ecView.labXYZToWorld(labXYZ, wp);
	}

	/**
	 * Get the polygon for a u, v or w strip
	 * 
	 * @param plane      either EC_INNER or EC_OUTER [0, 1]
	 * @param stripType  EC_U, EC_V, or EC_W [0..2]
	 * @param stripIndex the strip index [0..(EC_NUMSTRIP-1)]
	 * @return
	 */
	public Polygon stripPolygon(IContainer container, int plane, int stripType, int stripIndex) {
		Polygon poly = new Polygon();
		Point pp = new Point();
		
		for (int i = 0; i < 4; i++) {
			Point3D pijk = ECGeometry.getStripPoint(plane, stripType, stripIndex, i);
			ijkToScreen(container, pijk, pp);
			poly.addPoint(pp.x, pp.y);
		}

		return poly;

	}

	/**
	 * Get the world polygon for a strip
	 * 
	 * @param plane      either EC_INNER or EC_OUTER [0, 1]
	 * @param stripType  EC_U, EC_V, or EC_W [0..2]
	 * @param stripIndex the strip index [0..(EC_NUMSTRIP-1)]
	 */
	public void stripWorldPolygon(int plane, int stripType, int stripIndex, Point2D.Double wp[]) {
		for (int i = 0; i < 4; i++) {
			Point3D pijk = ECGeometry.getStripPoint(plane, stripType, stripIndex, i);
			ijkToWorld(pijk, wp[i]);
		}
	}

	// extend a line used to get the extension polygons
	private void extendLine(Point2D.Double wp0, Point2D.Double wp1, Point2D.Double wp, double t) {
		double delx = wp1.x - wp0.x;
		double dely = wp1.y - wp0.y;

		wp.x = wp0.x + delx * t;
		wp.y = wp0.y + dely * t;
	}

	/**
	 * Get the extension of the strip polygon
	 * 
	 * @param plane      inner or outer
	 * @param stripType  the type of strip UVW
	 * @param stripIndex the index of the strip
	 * @param work       workspace
	 * @param extension  the result
	 * @param fract      how filled is the extension
	 */
	private void extensionPolygon(int plane, int stripType, int stripIndex, Point2D.Double work[],
			Point2D.Double extension[], double fract) {

		double gap = 1.;
//		double len = fract*13;
		double len = fract * 20;
		stripWorldPolygon(plane, stripType, stripIndex, work);

		double d1 = work[1].distance(work[2]);
		double t1 = (1 + gap / d1);
		double t2 = (1 + (gap + len) / d1);

		double d2 = work[0].distance(work[3]);
		double t3 = (1 + gap / d2);
		double t4 = (1 + (gap + len) / d2);

		extendLine(work[1], work[2], extension[0], t1);
		extendLine(work[1], work[2], extension[1], t2);
		extendLine(work[0], work[3], extension[2], t4);
		extendLine(work[0], work[3], extension[3], t3);

//		if (stripType == ECGeometry.EC_W) {
//			extendLine(work[2], work[1], extension[0], t1);
//			extendLine(work[2], work[1], extension[1], t2);
//			extendLine(work[3], work[0], extension[2], t4);
//			extendLine(work[3], work[0], extension[3], t3);
//		}
//		else {
//			extendLine(work[1], work[2], extension[0], t1);
//			extendLine(work[1], work[2], extension[1], t2);
//			extendLine(work[0], work[3], extension[2], t4);
//			extendLine(work[0], work[3], extension[3], t3);
//		}
	}

	/**
	 * Get the extension of the strip polygon
	 * 
	 * @param container
	 * @param plane
	 * @param stripType
	 * @param stripIndex
	 * @param fract
	 * @return the screen polygon for the extension
	 */
	private Polygon extensionPolygon(IContainer container, int plane, int stripType, int stripIndex, double fract) {

		Polygon poly = new Polygon();
		Point pp = new Point();
		Point2D.Double[] work = new Point2D.Double[4];
		Point2D.Double[] extension = new Point2D.Double[4];

		for (int i = 0; i < 4; i++) {
			work[i] = new Point2D.Double();
			extension[i] = new Point2D.Double();
		}

		extensionPolygon(plane, stripType, stripIndex, work, extension, fract);

		for (int i = 0; i < 4; i++) {
			container.worldToLocal(pp, extension[i]);
			poly.addPoint(pp.x, pp.y);
		}
		return poly;
	}

	/**
	 * Converts a graphical world point to sector xyz
	 * 
	 * @param planeIndex the plane index, either EC_INNER or EC_OUTER
	 * @param wp         the world graphical point
	 * @param sectorXYZ  the sector xyz point
	 */
	public void worldToSectorXYZ(int planeIndex, Point2D.Double wp, double[] sectorXYZ) {
		Point2D.Double setct2D = new Point2D.Double();
		worldToSector2D(setct2D, wp);
		sectorXYZ[0] = setct2D.x;
		sectorXYZ[1] = setct2D.y;
		sectorXYZ[2] = ECGeometry.zFromX(planeIndex, setct2D.x);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {

			boolean inner = _ecView.displayInner();
			int plane = inner ? ECGeometry.EC_INNER : ECGeometry.EC_OUTER;

			// get the sector xyz coordinates
			double sectorXYZ[] = new double[3];
			worldToSectorXYZ(plane, wp, sectorXYZ);

			Point3D sp = new Point3D(sectorXYZ[0], sectorXYZ[1], sectorXYZ[2]);
			Point3D lp = new Point3D();
			ECGeometry.getTransformations(plane).sectorToLocal(lp, sp);

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
			localToUVW(container, plane, uvw, pp);

			// get the pixel
			// int pixel = ECGeometry.pixelFromUVW(uvw[0], uvw[1], uvw[2]);

			String labxyz = "$yellow$lab xyz " + vecStr(labXYZ) + " cm";
			feedbackStrings.add(labxyz);
			String labRhoPhi = String.format("$yellow$lab " + CedView.rhoPhi + " (%-6.2f, %-6.2f)", labRho,
					(Math.toDegrees(labPhi)));
			feedbackStrings.add(labRhoPhi);

			String sectxyz = "$orange$sector xyz " + vecStr(sectorXYZ) + " cm";
			feedbackStrings.add(sectxyz);
			String sectRhoPhi = String.format("$orange$sector " + CedView.rhoPhi + " (%-6.2f, %-6.2f)", sectRho,
					(Math.toDegrees(sectPhi)));
			feedbackStrings.add(sectRhoPhi);

			// now add the strings
			if ((uvw[0] > 0) && (uvw[1] > 0) && (uvw[2] > 0)) {

				String locStr = "$lime green$loc xyz " + point3DString(lp) + " cm";
				feedbackStrings.add(locStr);

				if ((uvw[0] > 0) && (uvw[1] > 0) && (uvw[2] > 0)) {
					String uvwStr = "$lime green$U V W [" + uvw[0] + ", " + uvw[1] + ", " + uvw[2] + "]";
					feedbackStrings.add(uvwStr);

					int pixel = ECGeometry.pixelFromUVW(uvw[0], uvw[1], uvw[2]);
					feedbackStrings.add("$lime green$pixel " + pixel);

					TdcAdcHitList hits = AllEC.getInstance().getHits();
					if ((hits != null) && !hits.isEmpty()) {
						for (int stype = 0; stype < 3; stype++) {
							int layer = 4 + 3 * plane + stype;
							TdcAdcHit hit = hits.get(getSector(), layer, uvw[stype]);
							if (hit != null) {
								hit.tdcAdcFeedback(AllEC.layerNames[layer], "strip", feedbackStrings);
							}
						}
					}

				}

//				// any hits?
//				if ((uvw[0] > 0) && (uvw[1] > 0) && (uvw[2] > 0)) {
//					for (int stripType = 0; stripType < 3; stripType++) {
//						if (_ecView.showStrips(stripType)) {
//							Vector<HitRecord> hits = EC.matchingHits(getSector(), plane + 1,
//											stripType + 1, uvw[stripType]);
//
//							if (hits != null) {
//								for (HitRecord hit : hits) {
//									EC.preliminaryFeedback(
//											hit.hitIndex,
//											feedbackStrings);
//									DataSupport.truePidFeedback(EC.pid(),
//											hit.hitIndex, feedbackStrings);
//									EC.dgtzFeedback(hit.hitIndex, feedbackStrings);
//								}
//							}
//						}
//					}
//				}
			}
		} // end contains

	}

	private String point3DString(Point3D p3d) {
		return String.format("(%-6.3f, %-6.3f, %-6.3f)", p3d.x(), p3d.y(), p3d.z());
	}

	// convert screen point to a uvw 1-based triplet
	private void localToUVW(IContainer container, int plane, int uvw[], Point pp) {
		for (int stripType = 0; stripType < 3; stripType++) {
			uvw[stripType] = -1;
			for (int stripIndex = 0; stripIndex < ECGeometry.EC_NUMSTRIP; stripIndex++) {

				Polygon poly = stripPolygon(container, plane, stripType, stripIndex);

				if ((poly != null) && (poly.contains(pp))) {
					uvw[stripType] = stripIndex + 1;
					break;
				}
			}
		}
	}

}
