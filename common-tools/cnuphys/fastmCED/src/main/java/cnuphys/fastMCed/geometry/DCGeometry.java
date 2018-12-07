package cnuphys.fastMCed.geometry;


import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCFactoryUpdated;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Triangle3D;


public class DCGeometry {

	private static ConstantProvider _dcDataProvider;
	private static DCDetector _dcDetector;
	private static DCSector sector0;

	private static double shortestWire;
	private static double longestWire;
	private static double minWireX;
	private static double maxWireX;
	private static double minWireY;
	private static double maxWireY;
	private static double minWireZ;
	private static double maxWireZ;

	/**
	 * Get the list of augmented detector hits (a la FastMC, not evio)
	 * Augmented so we can add more information
	 * 
	 * @param path
	 *            the path generated from a swim trajectory
	 * @return the list of hits FastMC geometry only.
	 */
	public static List<DetectorHit> getHits(Path3D path) {
		if (path == null) {
			return null;
		}
		return _dcDetector.getHits(path);
	}

	/**
	 * These are the drift chamber wires from the geometry service. The indices
	 * are 0-based: [superlayer 0:5][layer 0:5][wire 0:111]
	 */
	private static DriftChamberWire wires[][][];

	/**
	 * Initialize the DC Geometry by loading all the wires
	 */
	public static void initialize() {

		if (_dcDataProvider == null) {
			_dcDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.DC);
		}

		// arghh ugly hack until GEMC is modified

		DCFactoryUpdated dcFactory = new DCFactoryUpdated();
		_dcDetector = dcFactory.createDetectorCLAS(_dcDataProvider);

		sector0 = _dcDetector.getSector(0);

		shortestWire = Double.POSITIVE_INFINITY;
		longestWire = Double.NEGATIVE_INFINITY;
		minWireX = Double.POSITIVE_INFINITY;
		maxWireX = Double.NEGATIVE_INFINITY;
		minWireY = Double.POSITIVE_INFINITY;
		maxWireY = Double.NEGATIVE_INFINITY;
		minWireZ = Double.POSITIVE_INFINITY;
		maxWireZ = Double.NEGATIVE_INFINITY;

		wires = new DriftChamberWire[6][6][112];
		for (int suplay = 0; suplay < 6; suplay++) {
			DCSuperlayer sl = sector0.getSuperlayer(suplay);

			for (int lay = 0; lay < 6; lay++) {
				DCLayer dcLayer = sl.getLayer(lay);

				// if (suplay == 5) {
				// Shape3D shape = dcLayer.getBoundary(); // 2 faces
				// for (int i = 0; i < 2; i++) {
				// System.err.println("layer: " + (lay+1) + " face: [" + (i +
				// 1) + "] "
				// + shape.face(i));
				// }
				// }

				for (int w = 0; w < 112; w++) {
					DriftChamberWire dcw = dcLayer.getComponent(w);
					wires[suplay][lay][w] = dcw;

					Line3D line = dcw.getLine();
					double xx0 = line.origin().x();
					double yy0 = line.origin().y();
					double zz0 = line.origin().z();
					double xx1 = line.end().x();
					double yy1 = line.end().y();
					double zz1 = line.end().z();

					double wireLen = dcw.getLength();
					// double wireLen = line.length();

					shortestWire = Math.min(shortestWire, wireLen);
					longestWire = Math.max(longestWire, wireLen);
					minWireX = Math.min(minWireX, xx0);
					minWireX = Math.min(minWireX, xx1);
					maxWireX = Math.max(maxWireX, xx0);
					maxWireX = Math.max(maxWireX, xx1);
					minWireY = Math.min(minWireY, yy0);
					minWireY = Math.min(minWireY, yy1);
					maxWireY = Math.max(maxWireY, yy0);
					maxWireY = Math.max(maxWireY, yy1);
					minWireZ = Math.min(minWireZ, zz0);
					minWireZ = Math.min(minWireZ, zz1);
					maxWireZ = Math.max(maxWireZ, zz0);
					maxWireZ = Math.max(maxWireZ, zz1);

				}
			}
		}

		// Log.getInstance().info("Shortest Wire: " + shortestWire);
		// Log.getInstance().info("Longest Wire: " + longestWire);
		// System.err.println("shortest wire: " + shortestWire);
		// System.err.println("longest wire: " + longestWire);
		// System.err.println("minX: " + minWireX);
		// System.err.println("maxX: " + maxWireX);
		// System.err.println("minY: " + minWireY);
		// System.err.println("maxY: " + maxWireY);
		// System.err.println("minZ: " + minWireZ);
		// System.err.println("maxZ: " + maxWireZ);
		//
		// System.err.println("Done initing DC Geometry");

	}

	/**
	 * Used by the 3D drawing
	 * 
	 * @param sector
	 *            the 1-based sector
	 * @param superlayer
	 *            1 based superlayer [1..6]
	 * @param coords
	 *            holds 6*3 = 18 values [x1, y1, z1, ..., x6, y6, z6]
	 */
	public static void superLayerVertices(int sector, int superlayer, float[] coords) {

		DCSuperlayer sl = sector0.getSuperlayer(superlayer - 1);
		DCLayer dcLayer1 = sl.getLayer(0);
		DCLayer dcLayer6 = sl.getLayer(5);
		Shape3D shape1 = dcLayer1.getBoundary();
		Shape3D shape6 = dcLayer6.getBoundary();

		Triangle3D triangle1 = (Triangle3D) shape1.face(1);
		Triangle3D triangle6 = (Triangle3D) shape6.face(1);

		for (int i = 0; i < 3; i++) {
			Point3D v1 = new Point3D(triangle1.point(i));
			Point3D v6 = new Point3D(triangle6.point(i));

			if (sector > 1) {
				v1.rotateZ(Math.toRadians(60 * (sector - 1)));
				v6.rotateZ(Math.toRadians(60 * (sector - 1)));
			}

			int j = 3 * i;
			int k = j + 9;

			coords[j] = (float) v1.x();
			coords[j + 1] = (float) v1.y();
			coords[j + 2] = (float) v1.z();

			coords[k] = (float) v6.x();
			coords[k + 1] = (float) v6.y();
			coords[k + 2] = (float) v6.z();
		}

	}

	/**
	 * Get the length of the shortest wire in cm
	 * 
	 * @return the length of the shortest wire in cm
	 */
	public static double getShortestWire() {
		return shortestWire;
	}

	/**
	 * Get the length of the longest wire in cm
	 * 
	 * @return the length of the longest wire in cm
	 */
	public static double getLongestWire() {
		return longestWire;
	}

	/**
	 * Get the minimum x of a wire in the sector system (cm)
	 * 
	 * @return the minimum x of a wire in the sector system (cm)
	 */
	public static double getMinWireX() {
		return minWireX;
	}

	/**
	 * Get the maximum x coordinate of all the wires.
	 * 
	 * @return the max xin cm
	 */
	public static double getMaxWireX() {
		return maxWireX;
	}

	/**
	 * Get the minimum y coordinate of all the wires.
	 * 
	 * @return the min y in cm
	 */
	public static double getMinWireY() {
		return minWireY;
	}

	/**
	 * Get the absolute value of the largest y coordinate of any wire.
	 * 
	 * @return the absolute value of the largest y coordinate of any wire.
	 */
	public static double getAbsMaxWireY() {
		return Math.max(Math.abs(minWireY), maxWireY);
	}

	/**
	 * Get the absolute value of the largest x coordinate of any wire.
	 * 
	 * @return the absolute value of the largest x coordinate of any wire.
	 */
	public static double getAbsMaxWireX() {
		return Math.max(Math.abs(minWireX), maxWireX);
	}

	/**
	 * Get the maximum y coordinate of all the wires.
	 * 
	 * @return the max y in cm
	 */
	public static double getMaxWireY() {
		return maxWireY;
	}

	/**
	 * Get the minimum z coordinate of all the wires.
	 * 
	 * @return the min z in cm
	 */
	public static double getMinWireZ() {
		return minWireZ;
	}

	/**
	 * Get the maximum z coordinate of all the wires.
	 * 
	 * @return the max z in cm
	 */
	public static double getMaxWireZ() {
		return maxWireZ;
	}

	/**
	 * Get the midpoint of the untransformed wire in sector 1 NOTE: the indices
	 * are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @return the mid point of the wire in sector 1
	 */
	public static Point3D getMidPoint(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1].getLine().midpoint();
	}

	/**
	 * Get the wire in given sector NOTE: the indices are 1-based
	 * 
	 * @param sector
	 *            the 1-based sector [1..6]
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @return the wire transformed to the given sector
	 */
	public static Line3D getWire(int sector, int superlayer, int layer, int wire) {
		DriftChamberWire dcwire = getWire(superlayer, layer, wire);

		Line3D line = new Line3D(dcwire.getLine());
		if (sector > 1) {
			line.rotateZ(Math.toRadians(60 * (sector - 1)));
		}
		return line;
	}

	/**
	 * Get the wire in sector 0 NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @return the untransformed wire in sector 0
	 */
	public static DriftChamberWire getWire(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1];
	}

	/**
	 * Get the origin of the wire in sector 0 NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @return the origin (one end) of the wire in sector 0
	 */
	public static Point3D getOrigin(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1].getLine().origin();
	}

	/**
	 * Get the end of the wire in sector 0 NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @return the end (one end) of the wire in sector 0
	 */
	public static Point3D getEnd(int superlayer, int layer, int wire) {
		return wires[superlayer - 1][layer - 1][wire - 1].getLine().end();
	}

	/**
	 * Get the intersections of a dcwire with a constant phi plane. If the wire
	 * does not intersect (happens as phi grows) return null;
	 * 
	 * NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @param projectionPlane
	 *            the projection plane
	 */
	public static boolean getHexagon(int superlayer, int layer, int wire, Plane3D projectionPlane, Point2D.Double wp[],
			Point2D.Double centroid) {

		DriftChamberWire dcw = DCGeometry.getWire(superlayer, layer, wire);
		return GeometryManager.getProjectedPolygon(dcw, projectionPlane, 10, 6, wp, centroid);
	}
	
	public static boolean doesHexagonIntersect(int superlayer, int layer, int wire, Plane3D projectionPlane) {
		DriftChamberWire dcw = DCGeometry.getWire(superlayer, layer, wire);
		return GeometryManager.doesProjectedPolyIntersect(dcw, projectionPlane, 10, 6);
	}

	/**
	 * Get the approximate center of the projected hexagon
	 * 
	 * NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @param transform3D
	 *            the transformation to the constant phi
	 * @return the approximate center of the projected hexagon
	 */
	public static Point2D.Double getCenter(int superlayer, int layer, int wire, Plane3D projectionPlane) {

		Point2D.Double centroid = new Point2D.Double();

		DriftChamberWire dcw = DCGeometry.getWire(superlayer, layer, wire);
		Line3D l3D = dcw.getLine();
		Point3D p3 = new Point3D();
		projectionPlane.intersection(l3D, p3);

		centroid.x = p3.z();
		centroid.y = Math.hypot(p3.x(), p3.y());

		return centroid;
	}

	/**
	 * Get a super layer plane
	 * 
	 * @param sector
	 *            the 1-based sector (result IS sector dependent)
	 * @param superlayer
	 *            the 1-based superlayer
	 * @return the plane perpendicular to the wires for this superlayer
	 */
	public static Plane3D getSuperlayerPlane(int sector, int superlayer) {
		// can use arbitrary wire
		Line3D l3d = getWire(sector, superlayer, 3, 55);
		Point3D origin = new Point3D(0, 0, 0);
		return new Plane3D(origin, l3d.direction());
	}

	/**
	 * Get the position of the sense wire in sector 0
	 * 
	 * NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @param phi
	 *            the relative phi (degrees)
	 * 
	 * @return the position of the sense wire
	 */
	public static Point2D.Double getCenter(int superlayer, int layer, int wire, double phi) {

		double tanPhi = Math.tan(Math.toRadians(phi));
		DriftChamberWire dcw = wires[superlayer - 1][layer - 1][wire - 1];
		Line3D line3D = dcw.getLine();
		double x0 = line3D.origin().x();
		double y0 = line3D.origin().y();
		double z0 = line3D.origin().z();
		double x1 = line3D.end().x();
		double y1 = line3D.end().y();
		double z1 = line3D.end().z();
		double dx = x1 - x0;
		double dy = y1 - y0;
		double dz = z1 - z0;
		double t = (x0 * tanPhi - y0) / (dy - dx * tanPhi);

		double x = x0 + t * dx;
		double y = y0 + t * dy;
		double z = z0 + t * dz;

		Point2D.Double centroid = new Point2D.Double();

		centroid.x = z;
		centroid.y = Math.hypot(x, y);
		return centroid;

	}

	/**
	 * Get a point on either side of a layer
	 * 
	 * NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param wire
	 *            the wire [1..112]
	 * @param transform3D
	 *            the transformation to the constant phi
	 * @param wp
	 *            on return will hold the two extended points. The 0 point will
	 *            be to the "right" of wire 0. The 1 point will be to the left
	 *            of wire 111.
	 */
	public static void getLayerExtendedPoints(int superLayer, int layer, Plane3D projectionPlane, Point2D.Double wp[]) {

		Point2D.Double hexagon[] = GeometryManager.allocate(6);

		getHexagon(superLayer, layer, 1, projectionPlane, hexagon, null);
		Point2D.Double first = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 2, projectionPlane, hexagon, null);
		Point2D.Double second = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 111, projectionPlane, hexagon, null);
		Point2D.Double nexttolast = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		getHexagon(superLayer, layer, 112, projectionPlane, hexagon, null);
		Point2D.Double last = new Point2D.Double(hexagon[0].x, hexagon[0].y);

		extPoint(first, second, wp[0]);
		extPoint(last, nexttolast, wp[1]);
	}

	/**
	 * Get the boundary of a layer
	 * 
	 * NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param layer
	 *            the layer [1..6]
	 * @param transform3D
	 *            the transformation to the constant phi
	 * @param wp
	 *            a four point layer boundary
	 */
	public static void getLayerPolygon(int superLayer, int layer, Plane3D plane, Point2D.Double wp[]) {

		Point2D.Double hex[] = GeometryManager.allocate(6);

		int firstWire = 1;
		while ((firstWire < 112) && !getHexagon(superLayer, layer, firstWire, plane, hex, null)) {
			firstWire++;
		}
//		System.err.println("FIRST WIRE: " + firstWire);

//		getHexagon(superLayer, layer, 1, plane, hex, null);
		assignFromHex(wp, 0, hex, 0);
		assignFromHex(wp, 11, hex, 3);
		assignFromHex(wp, 12, hex, 4);
		assignFromHex(wp, 13, hex, 5);

		int sindex = Math.max(13, firstWire+8);
		getHexagon(superLayer, layer, sindex, plane, hex, null);
		assignFromHex(wp, 1, hex, 0);
		assignFromHex(wp, 10, hex, 3);

		sindex = Math.max(57, sindex+12);
		getHexagon(superLayer, layer, 57, plane, hex, null);
		assignFromHex(wp, 2, hex, 0);
		assignFromHex(wp, 9, hex, 3);

		sindex = Math.max(99, sindex+29);
		getHexagon(superLayer, layer, 99, plane, hex, null);
		assignFromHex(wp, 3, hex, 0);
		assignFromHex(wp, 8, hex, 3);

		getHexagon(superLayer, layer, 112, plane, hex, null);
		assignFromHex(wp, 4, hex, 0);
		assignFromHex(wp, 5, hex, 1);
		assignFromHex(wp, 6, hex, 2);
		assignFromHex(wp, 7, hex, 3);
	}

	/**
	 * Get the boundary of a super layer
	 * 
	 * NOTE: the indices are 1-based
	 * 
	 * @param superlayer
	 *            the superlayer [1..6]
	 * @param transform3D
	 *            the transformation to the constant phi
	 * @param wp
	 *            a four point super layer boundary
	 */
	public static void getSuperLayerPolygon(int superLayer, Plane3D projectionPlane, Point2D.Double wp[]) {

		Point2D.Double lb[] = GeometryManager.allocate(14);
		getLayerPolygon(superLayer, 1, projectionPlane, lb);
		wp[0].setLocation(lb[12]);
		wp[1].setLocation(lb[13]);
		wp[2].setLocation(lb[0]);
		wp[3].setLocation(lb[1]);
		wp[4].setLocation(lb[2]);
		wp[5].setLocation(lb[3]);
		wp[6].setLocation(lb[4]);
		wp[7].setLocation(lb[5]);
		wp[8].setLocation(lb[6]);

		getLayerPolygon(superLayer, 2, projectionPlane, lb);
		wp[9].setLocation(lb[5]);
		wp[10].setLocation(lb[6]);
		wp[32].setLocation(lb[12]);
		wp[33].setLocation(lb[13]);

		getLayerPolygon(superLayer, 3, projectionPlane, lb);
		wp[11].setLocation(lb[5]);
		wp[12].setLocation(lb[6]);
		wp[30].setLocation(lb[12]);
		wp[31].setLocation(lb[13]);

		getLayerPolygon(superLayer, 4, projectionPlane, lb);
		wp[13].setLocation(lb[5]);
		wp[14].setLocation(lb[6]);
		wp[28].setLocation(lb[12]);
		wp[29].setLocation(lb[13]);

		getLayerPolygon(superLayer, 5, projectionPlane, lb);
		wp[15].setLocation(lb[5]);
		wp[16].setLocation(lb[6]);
		wp[26].setLocation(lb[12]);
		wp[27].setLocation(lb[13]);

		getLayerPolygon(superLayer, 6, projectionPlane, lb);
		wp[17].setLocation(lb[5]);
		wp[18].setLocation(lb[6]);
		wp[19].setLocation(lb[7]);
		wp[20].setLocation(lb[8]);
		wp[21].setLocation(lb[9]);
		wp[22].setLocation(lb[10]);
		wp[23].setLocation(lb[11]);
		wp[24].setLocation(lb[12]);
		wp[25].setLocation(lb[13]);

	}

	private static void assignFromHex(Point2D.Double wp[], int wpIndex, Point2D.Double hex[], int hexIndex) {
		Point2D.Double p = new Point2D.Double(hex[hexIndex].x, hex[hexIndex].y);
		wp[wpIndex] = p;
	}

	// private static void expand(Point2D.Double wp0, Point2D.Double wp1, double
	// factor) {
	// double delx = factor * (wp1.x - wp0.x);
	// double dely = factor * (wp1.y - wp0.y);
	//
	// double x0 = wp0.x;
	// double y0 = wp0.y;
	//
	// wp0.x = wp1.x - delx;
	// wp0.y = wp1.y - dely;
	// wp1.x = x0 + delx;
	// wp1.y = y0 + dely;
	// }

	// extend a point
	private static void extPoint(Point2D.Double p0, Point2D.Double p1, Point2D.Double ext) {

		if ((p0 == null) || (p1 == null) || (ext == null)) {
			System.err.println("null point in DCGeometry.extPoint");
			return;
		}

		ext.x = p0.x + (p0.x - p1.x);
		ext.y = p0.y + (p0.y - p1.y);
	}

}