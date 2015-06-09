package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.log.Log;

public class DCGeometry {

    private static ConstantProvider dcDataProvider;
    private static DCDetector dcDetector;
    private static DCSector sector0;

    private static double shortestWire;
    private static double longestWire;
    private static double minWireX;
    private static double maxWireX;
    private static double minWireY;
    private static double maxWireY;
    private static double minWireZ;
    private static double maxWireZ;

    public static boolean inited = false;

    /**
     * These are the drift chamber wires from the geometry service. The indices
     * are 0-based: [superlayer 0:5][layer 0:5][wire 0:111]
     */
    private static DriftChamberWire wires[][][];

    /**
     * Initialize the DC Geometry by loading all the wires
     */
    public static void init() {
	if (inited) {
	    return;
	}

	System.out.println("\n=======================================");
	System.out.println("====  DC Geometry Inititialization ====");
	System.out.println("=======================================");

	dcDataProvider = DataBaseLoader.getDriftChamberConstants();

	dcDetector = (new DCFactory()).createDetectorCLAS(dcDataProvider);

	sector0 = dcDetector.getSector(0);

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
     * Get the midpoint of the untransformed wire in sector 0 NOTE: the indices
     * are 1-based
     * 
     * @param superlayer
     *            the superlayer [1..6]
     * @param layer
     *            the layer [1..6]
     * @param wire
     *            the wire [1..112]
     * @return the mid point of the wire in sector 0
     */
    public static Point3D getMidPoint(int superlayer, int layer, int wire) {
	return wires[superlayer - 1][layer - 1][wire - 1].getLine().midpoint();
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
     * @param transform3D
     *            the transformation to the constant phi
     * @return the lines of the "hexagon", or null if no intersection.
     */
    public static List<Line3D> getHexagon(int superlayer, int layer, int wire,
	    Transformation3D transform3D) {

	DriftChamberWire dcw = wires[superlayer - 1][layer - 1][wire - 1];
	return dcw.getVolumeCrossSection(transform3D);
    }

    /**
     * For the given parameters, compute the intersecing polygon
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
     *            the world polygon, already allocated with 6 points. Note the
     *            last point in the polygon is NOT the same as the first.
     * @param centroid
     *            if null, ignored. If not null, will hold the center of the
     *            polygon
     * @return the number of good points in the polygon (should be 6)
     */
    public static int worldPolygon(int superlayer, int layer, int wire,
	    Transformation3D transform3D, Point2D.Double wp[],
	    Point2D.Double centroid) {
	List<Line3D> lines = getHexagon(superlayer, layer, wire, transform3D);
	return worldPolygon(lines, wp, centroid);
    }

    /**
     * Given a list of lines return from getHexagon, this computes the world
     * polygon and the center
     * 
     * @param lines
     *            the lines from getHexagon
     * @param wp
     *            the world polygon, already allocated with 6 points. Note the
     *            last point in the polygon is NOT the same as the first.
     * @param centroid
     *            if null, ignored. If not null, will hold the center of the
     *            polygon
     * @return the number of good points in the polygon (should be 6)
     */
    public static int worldPolygon(List<Line3D> lines, Point2D.Double wp[],
	    Point2D.Double centroid) {

	int size = (lines == null) ? 0 : lines.size();
//	System.err.println(x);

	if (size < 6) {
	    size = 0;
	}
	if (size > 6) {
//	    System.err.println("Bad size in worldPolygon [DCGeometry] = "
//		    + size);
//	    
	    size = 6;
	}

	for (int i = 0; i < size; i++) {
	    if (lines == null) {
		wp[i].x = Double.NaN;
		wp[i].y = Double.NaN;
	    } else {
		Point3D p = lines.get(i).origin();
		wp[i].x = p.x();
		wp[i].y = p.y();
	    }
	}

	if (centroid != null) {
	    if (lines == null) {
		centroid.x = Double.NaN;
		centroid.y = Double.NaN;
	    } else {
		average(wp, centroid);
	    }
	}

	return size;
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
    public static Point2D.Double getCenter(int superlayer, int layer, int w,
	    Transformation3D transform3D) {
	Point2D.Double wpoly[] = GeometryManager.allocate(6);
	Point2D.Double centroid = new Point2D.Double();

	List<Line3D> lines = getHexagon(superlayer, layer, w, transform3D);
	int size = worldPolygon(lines, wpoly, centroid);
	
	return (size != 6) ? null: centroid;
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
    public static void getLayerExtendedPoints(int superLayer, int layer,
	    Transformation3D transform3D, Point2D.Double wp[]) {
	
	
	int firstWire = 1;
	Point2D.Double first = null;
	Point2D.Double second = getCenter(superLayer, layer, 1, transform3D);;
	while ((firstWire < 111) && ((first == null) || (second == null))) {
	    first = second;
	    second = getCenter(superLayer, layer, firstWire+1, transform3D);
	    if ((first == null) || (second == null)) {
		firstWire++;
	    }
	    else {
		break;
	    }
	}
	
	if (firstWire > 110) {
	    return;
	}
		
//	System.err.println("FIRST WIRE: " + firstWire);
	
	Point2D.Double last = getCenter(superLayer, layer, 112, transform3D);
	Point2D.Double nexttolast = getCenter(superLayer, layer, 111,
		transform3D);
	
	if (first == null) System.err.println("null first in getLayerExtendedPoints supl: " + 
	superLayer + " layer: " + layer);
	if (second == null) System.err.println("null second in getLayerExtendedPoints");
	if (last == null) System.err.println("null last in getLayerExtendedPoints");
	if (nexttolast == null) System.err.println("null nexttolast in getLayerExtendedPoints");

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
    public static void getLayerPolygon(int superLayer, int layer,
	    Transformation3D transform3D, Point2D.Double wp[]) {
	
	Point2D.Double thisLayer[] = GeometryManager.allocate(2);
	Point2D.Double nextLayer[] = GeometryManager.allocate(2);

	int nl = ((layer % 2) == 0) ? layer - 1 : layer + 1;
	
	getLayerExtendedPoints(superLayer, layer, transform3D, thisLayer);
	getLayerExtendedPoints(superLayer, nl, transform3D, nextLayer);
	
	double delx = Math.abs(nextLayer[1].x - thisLayer[1].x)/2;
	double dely = Math.abs(nextLayer[1].y - thisLayer[1].y)/2;
	
//	System.err.println("DELX: " + delx);
//	System.err.println("DELY: " + dely);
	
	wp[0].setLocation(thisLayer[0].x-delx, thisLayer[0].y-dely); 
	wp[1].setLocation(thisLayer[1].x-delx, thisLayer[1].y-dely); 
	wp[2].setLocation(thisLayer[1].x+delx, thisLayer[1].y+dely); 
	wp[3].setLocation(thisLayer[0].x+delx, thisLayer[0].y+dely); 
	
	// close?
	if (wp.length == 5) {
	    wp[4].x = wp[0].x;
	    wp[4].y = wp[0].y;
	}
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
    public static void getSuperLayerPolygon(int superLayer,
	    Transformation3D transform3D, Point2D.Double wp[]) {
	Point2D.Double layerBoundary[] = GeometryManager.allocate(4);

	getLayerPolygon(superLayer, 1, transform3D, layerBoundary);
	wp[0].setLocation(layerBoundary[0]);
	wp[1].setLocation(layerBoundary[1]);
	getLayerPolygon(superLayer, 6, transform3D, layerBoundary);
	wp[2].setLocation(layerBoundary[2]);
	wp[3].setLocation(layerBoundary[3]);

	// close?
	if (wp.length == 5) {
	    wp[4].x = wp[0].x;
	    wp[4].y = wp[0].y;
	}

    }

    // extend a point
    private static void extPoint(Point2D.Double p0, Point2D.Double p1,
	    Point2D.Double ext) {
	
	if ((p0 == null) || (p1 == null) ||(ext == null)) {
	    System.err.println("null point in DCGeometry::extPoint");
	    return;
	}
	
	ext.x = p0.x + (p0.x - p1.x);
	ext.y = p0.y + (p0.y - p1.y);
    }

    // faster than centroid, and good enough
    private static void average(Point2D.Double wp[], Point2D.Double centroid) {
	double xsum = 0;
	double ysum = 0;

	int size = wp.length;
	for (int i = 0; i < size; i++) {
	    xsum += wp[i].x;
	    ysum += wp[i].y;
	}

	centroid.x = xsum / size;
	centroid.y = ysum / size;
    }

    public static void main(String arg[]) {
	DCGeometry.init();

	int superlayer = 4;
	int layer = 3;
	int wire = 36;

	DriftChamberWire dcw = getWire(superlayer, layer, wire);
	Line3D line = dcw.getLine();

	System.err.println("MP from DC Wire: " + dcw.getMidpoint());
	System.err.println("MP from DC Line: " + line.midpoint());
	System.err.println("Len from DC Wire: " + dcw.getLength());
	System.err.println("Len from DC Line: " + dcw.getLine().length());
	System.err.println("DC Line: " + line);

	transReport(superlayer, layer, wire, 0.);
	transReport(superlayer, layer, wire, 15.);
	transReport(superlayer, layer, wire, -15.);
	
	Transformation3D transform0 = GeometryManager.toConstantPhi(15);


	 System.err.println("Test extended layer points");
	 Point2D.Double first = getCenter(superlayer, layer, 1, transform0);
	 Point2D.Double last = getCenter(superlayer, layer, 112, transform0);
	 
	 Point2D.Double extWp[] = GeometryManager.allocate(2);
	 getLayerExtendedPoints(superlayer, layer, transform0, extWp);
	 System.err.println("first: " + first);
	 System.err.println("ext0: " + extWp[0]);
	 System.err.println("last: " + last);
	 System.err.println("ext1: " + extWp[1]);
	
	 System.err.println("\nTest extended layer points");

	for (int lay = 1; lay <= 6; lay++) {
	    System.err.println("\nLAYER: " + lay);
	    Point2D.Double corners[] = GeometryManager.allocate(4);
	    getLayerExtendedPoints(superlayer, lay, transform0, extWp);
	    getLayerPolygon(superlayer, lay, transform0, corners);
	    System.err.println("extended point 0 " + extWp[0]);
	    System.err.println("extended point 1 " + extWp[1]);

	    for (int i = 0; i < 4; i++) {
		System.err.println("corner[" + i + "]  " + corners[i]);
	    }
	}

	// System.err.println("\nTest contains");
	// getSuperLayerPolygon(superlayer, transform0, corners);
	// for (int lay = 1; lay <= 6; lay++) {
	// for (int w = 1; w <= 112; w++) {
	// Point2D.Double p2d = getCenter(superlayer, lay, w, transform0);
	// System.err.print(WorldGraphicsUtilities.contains(corners, p2d)
	// + " ");
	// if (((w + 1) % 10) == 0)
	// System.err.println();
	// }
	// }

    }

    private static void transReport(int superlayer, int layer, int wire,
	    double phi) {
	System.err.println("\n** AFTER transformation phi = " + +phi);
	Point2D.Double wpoly[] = GeometryManager.allocate(6);
	Point2D.Double centroid = new Point2D.Double();
	Transformation3D transform3D = GeometryManager.toConstantPhi(phi);

	List<Line3D> lines = getHexagon(superlayer, layer, wire, transform3D);
	int size = worldPolygon(lines, wpoly, centroid);

	if (size > 0) {
	    for (int i = 0; i < 1; i++) {
		System.err.println("lines[" + (i + 1) + "] = " + lines.get(i));
	    }
	    System.err.println("Poly centroid: " + centroid + "   poly size: "
		    + size);

	} else {
	    System.err.println("No intesection, phi too big");
	}

    }
}
