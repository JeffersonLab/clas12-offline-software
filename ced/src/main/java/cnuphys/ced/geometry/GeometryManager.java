package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECSector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.ced.frame.Ced;

public class GeometryManager {

    /**
     * Singleton
     */
    private static GeometryManager instance;

    // BSTxy panels
    private static Vector<BSTxyPanel> _bstXYpanels;

    // cal sector 0 in clas coordinates
    public static ECSector clas_Cal_Sector0;

    // cal sector 0 in local coordinates
    public static ECSector local_Cal_Sector0;

    /**
     * The wire endpoints for sector 1. Other sectors will rotate. The indices
     * are superlayer 0..5, layer 0..7, wire 0..113. Layers 0 and 7 are guard
     * layers. Wires 0 and 113 are guard wires.
     */
    protected static double x0[][][];
    protected static double y0[][][];
    protected static double z0[][][];
    protected static double x1[][][];
    protected static double y1[][][];
    protected static double z1[][][];
    protected static double shortestWire;
    protected static double longestWire;
    protected static double minWireX;
    protected static double maxWireX;
    protected static double minWireY;
    protected static double maxWireY;
    protected static double minWireZ;
    protected static double maxWireZ;

    /**
     * Private constructor for the singleton.
     */
    private GeometryManager() {
	// get the drift chamber wire endpoints
	getWireEndpoints();
	getWireLimits();

	// get the FTOF geometry
	FTOFGeometry.initialize();

	// get BST data
	BSTGeometry.initialize();
	getBSTPanels();

	ConstantProvider ecDataProvider = DataBaseLoader
		.getCalorimeterConstants();
	ECDetector clas_Cal_Detector = (new ECFactory())
		.createDetectorCLAS(ecDataProvider);

	// cal sector 0 in clas coordinates
	clas_Cal_Sector0 = clas_Cal_Detector.getSector(0);

	local_Cal_Sector0 = (new ECFactory()).createDetectorLocal(
		ecDataProvider).getSector(0);

	// get EC data
	ECGeometry.initialize();

	// get PCAL data
	PCALGeometry.initialize();

    }

    /**
     * Public access to the singleton manager.
     * 
     * @return the GeometryManager singleton.
     */
    public static GeometryManager getInstance() {
	if (instance == null) {
	    instance = new GeometryManager();
	}
	return instance;
    }

    // read the bst geometry
    private void getBSTPanels() {
	// TODO use the providers when ready

	System.out.println("\n=======================================");
	System.out.println("====  BST Geometry Inititialization ====");
	System.out.println("=======================================");

	// use the geometry service

	double vals[] = new double[10];
	for (int bigLayer = 1; bigLayer <= 8; bigLayer++) {
	    // geom service uses superlayer and layer
	    int supl = ((bigLayer - 1) / 2); // 0, 1, 2, 3
	    int lay = ((bigLayer - 1) % 2); // 0, 1, 0, 1

	    for (int sector = 1; sector <= BSTGeometry.sectorsPerSuperlayer[supl]; sector++) {
		// public static void getLimitValues(int sector, int superlayer,
		// int layer, double vals[]) {
		BSTGeometry.getLimitValues(sector - 1, supl, lay, vals);
		if (_bstXYpanels == null) {
		    _bstXYpanels = new Vector<BSTxyPanel>();
		}
		_bstXYpanels.add(new BSTxyPanel(sector, bigLayer, vals));
	    }
	}

    }

    /**
     * Get the driftchamber wire endpoints (units are cm)
     */
    private void getWireEndpoints() {

	System.out.println("\n=======================================");
	System.out.println("====  DC Geometry Inititialization ====");
	System.out.println("=======================================");

	// The wire endpoints for sector 1. Other sectors will rotate. The
	// indices
	// are superlayer 0..5, layer 0..7, wire 0..113. Layers 0 and 7 are
	// guard layers. Wires 0 and 113 are guard wires.

	int numSL = GeoConstants.NUM_SUPERLAYER;
	int numLplus2 = GeoConstants.NUM_LAYER + 2; // plus 2 for guard
	// layers
	int numWplus2 = GeoConstants.NUM_WIRE + 2; // +2 for guard wires

	x0 = new double[numSL][numLplus2][numWplus2];
	y0 = new double[numSL][numLplus2][numWplus2];
	z0 = new double[numSL][numLplus2][numWplus2];
	x1 = new double[numSL][numLplus2][numWplus2];
	y1 = new double[numSL][numLplus2][numWplus2];
	z1 = new double[numSL][numLplus2][numWplus2];

	// try common geometry first
	if (getWireEndpointsFromClasGeometry()) {
	    return;
	}

	// try from file
	File file = FileUtilities.findFile(Ced.dataPath, "sector_1_wires.dat");

	System.out.println("Attempting to read DC geomtery file: ["
		+ (file == null ? "null" : file.getPath()) + "]\n");

	if ((file != null) && file.exists()) {
	    Log.getInstance().info(
		    "Wire endpoint file found: " + file.getPath());

	    try {
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		while (true) {
		    String s = bufferedReader.readLine();
		    if (s == null) {
			break;
		    } else {
			if (!s.startsWith("!")) {
			    String tokens[] = FileUtilities.tokens(s, " ");

			    // -1 converts to zero based
			    int superlayer = Integer.parseInt(tokens[0]) - 1;
			    int layer = Integer.parseInt(tokens[1]) - 1;
			    int wire = Integer.parseInt(tokens[2]) - 1;

			    double xx0 = Double.parseDouble(tokens[3]);
			    double yy0 = Double.parseDouble(tokens[4]);
			    double zz0 = Double.parseDouble(tokens[5]);
			    double xx1 = Double.parseDouble(tokens[6]);
			    double yy1 = Double.parseDouble(tokens[7]);
			    double zz1 = Double.parseDouble(tokens[8]);

			    x0[superlayer][layer][wire] = xx0;
			    y0[superlayer][layer][wire] = yy0;
			    z0[superlayer][layer][wire] = zz0;
			    x1[superlayer][layer][wire] = xx1;
			    y1[superlayer][layer][wire] = yy1;
			    z1[superlayer][layer][wire] = zz1;
			}
		    }
		}
		fileReader.close();
	    } catch (FileNotFoundException e) {
		Log.getInstance().exception(e);
		e.printStackTrace();
	    } catch (IOException e) {
		Log.getInstance().exception(e);
		e.printStackTrace();
	    }

	    Log.getInstance().info("Successfully read in wire endpoints.");

	} else {
	    Log.getInstance().warning(
		    "Wire endpoint file not found at: "
			    + ((file == null) ? "???" : file.getPath()));

	    System.out.println("Wire endpoint file not found at: "
		    + ((file == null) ? "???" : file.getPath()));
	}

    }

    private void getWireLimits() {

	shortestWire = Double.POSITIVE_INFINITY;
	longestWire = Double.NEGATIVE_INFINITY;
	minWireX = Double.POSITIVE_INFINITY;
	maxWireX = Double.NEGATIVE_INFINITY;
	minWireY = Double.POSITIVE_INFINITY;
	maxWireY = Double.NEGATIVE_INFINITY;
	minWireZ = Double.POSITIVE_INFINITY;
	maxWireZ = Double.NEGATIVE_INFINITY;

	for (int supl = 0; supl < 6; supl++) {
	    for (int lay = 0; lay < 8; lay++) {
		for (int w = 0; w < 114; w++) {
		    double xx0 = x0[supl][lay][w];
		    double yy0 = y0[supl][lay][w];
		    double zz0 = z0[supl][lay][w];
		    double xx1 = x1[supl][lay][w];
		    double yy1 = y1[supl][lay][w];
		    double zz1 = z1[supl][lay][w];

		    double delX = xx1 - xx0;
		    double delY = yy1 - yy0;
		    double delZ = zz1 - zz0;
		    double wireLen = Math.sqrt(delX * delX + delY * delY + delZ
			    * delZ);

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

	Log.getInstance().info("Shortest Wire: " + shortestWire);
	Log.getInstance().info("Longest Wire: " + longestWire);
	// System.err.println("minX: " + minWireX);
	// System.err.println("maxX: " + maxWireX);
	// System.err.println("minY: " + minWireY);
	// System.err.println("maxY: " + maxWireY);
	// System.err.println("minZ: " + minWireZ);
	// System.err.println("maxZ: " + maxWireZ);

    } // getWireLimits

    // try to get the endpoints from Gagik's package
    private boolean getWireEndpointsFromClasGeometry() {
	try {
	    DCGeometry.loadArrays();
	    String message = "Got wire endpoints from common geometry clas-geometry package.";
	    Log.getInstance().config(message);
	    System.out.println(message);
	    return true;
	} catch (Exception e) {
	    System.err.println("Error getting DC geometry from clas-geometry.");
	    e.printStackTrace();
	    return false;
	}
    }

    /**
     * Get the wire endpoint coordinate for sector 1. Other sectors will rotate.
     * The indices are superlayer 1..6, layer 1..8, wire 1..114. Superlayers 1
     * and 8 are guard layers. Wires 1 and 114 are guard wires.
     * 
     * @return the y0 array
     */
    public double[][][] getX0() {
	return x0;
    }

    /**
     * Get the wire endpoint coordinate for sector 1. Other sectors will rotate.
     * The indices are superlayer 1..6, layer 1..8, wire 1..114. Superlayers 1
     * and 8 are guard layers. Wires 1 and 114 are guard wires.
     * 
     * @return the y0 array
     */
    public double[][][] getY0() {
	return y0;
    }

    /**
     * Get the wire endpoint coordinate for sector 1. Other sectors will rotate.
     * The indices are superlayer 1..6, layer 1..8, wire 1..114. Superlayers 1
     * and 8 are guard layers. Wires 1 and 114 are guard wires.
     * 
     * @return the z0 array
     */
    public double[][][] getZ0() {
	return z0;
    }

    /**
     * Get the wire endpoint coordinate for sector 1. Other sectors will rotate.
     * The indices are superlayer 1..6, layer 1..8, wire 1..114. Superlayers 1
     * and 8 are guard layers. Wires 1 and 114 are guard wires.
     * 
     * @return the x1 array
     */
    public double[][][] getX1() {
	return x1;
    }

    /**
     * Get the wire endpoint coordinate for sector 1. Other sectors will rotate.
     * The indices are superlayer 1..6, layer 1..8, wire 1..114. Superlayers 1
     * and 8 are guard layers. Wires 1 and 114 are guard wires.
     * 
     * @return the y1 array
     */
    public double[][][] getY1() {
	return y1;
    }

    /**
     * Get the wire endpoint coordinate for sector 1. Other sectors will rotate.
     * The indices are superlayer 1..6, layer 1..8, wire 1..114. Superlayers 1
     * and 8 are guard layers. Wires 1 and 114 are guard wires.
     * 
     * @return the z1 array
     */
    public double[][][] getZ1() {
	return z1;
    }

    /**
     * Get the sector [1..6] from the phi value
     * 
     * @param phi
     *            the value of phi in degrees
     * @return the sector [1..6]
     */
    public static int getSector(double phi) {
	// convert phi to [0..360]

	while (phi < 0) {
	    phi += 360.0;
	}
	while (phi > 360.0) {
	    phi -= 360.0;
	}

	if ((phi > 30.0) && (phi <= 90.0)) {
	    return 2;
	}
	if ((phi > 90.0) && (phi <= 150.0)) {
	    return 3;
	}
	if ((phi > 150.0) && (phi <= 210.0)) {
	    return 4;
	}
	if ((phi > 210.0) && (phi <= 270.0)) {
	    return 5;
	}
	if ((phi > 270.0) && (phi <= 330.0)) {
	    return 6;
	}
	return 1;
    }

    /**
     * Get the sector [1..6] from the lab x and y coordinates
     * 
     * @param labX
     *            the lab x
     * @param labY
     *            the lab y
     * @return the sector [1..6]
     */
    public static int getSector(double labX, double labY) {
	double phi = Math.atan2(labY, labX);
	return getSector(Math.toDegrees(phi));
    }

    /**
     * Obtains the relative phi (relative to the midplane of the appropriate
     * sector)
     * 
     * @param absPhi
     *            the absolute value of phi
     * @return the relative phi (i.e., the "slider" value)
     */
    public static double getRelativePhi(double absPhi) {
	while (absPhi < 360.0) {
	    absPhi += 360.0;
	}

	while (absPhi > 30.0) {
	    absPhi -= 60.0;
	}

	return absPhi;
    }

    /**
     * Get the intersection of the given line with RELATIVE phi plane. All
     * coordinates are in the sector system.
     * 
     * @param xa
     *            sector x coordinate of one end point
     * @param ya
     *            sector y coordinate of one end point
     * @param za
     *            sector z coordinate of one end point
     * @param xb
     *            sector x coordinate of other end point
     * @param yb
     *            sector y coordinate of other end point
     * @param zb
     *            sector z coordinate of other end point
     * @param relativePhi
     *            the relative phi (relative to the midplane) [-30..30]
     * @param wp
     *            will hold the intersection. wp.x will be the horizontal
     *            coordinate (i.e., z in the usual sector system) and wp.y will
     *            be the cylindrical r (or rho) in the sector system. wp.y is
     *            positive definite, so should be negated for lower sectors
     *            (4,5,6) in sector views
     */
    public static void getPlaneIntersection(double xa, double ya, double za,
	    double xb, double yb, double zb, double relativePhi,
	    Point2D.Double wp) {

	double tanphi = Math.tan(-Math.toRadians(relativePhi));

	// not t value depends only on x and y, not z
	double t = getPlaneIntersectionT(xa, ya, xb, yb, tanphi);

	// get intersection
	double x = xa + t * (xb - xa);
	double y = ya + t * (yb - ya);
	double z = za + t * (zb - za);

	wp.x = z;
	wp.y = Math.hypot(x, y);
    }

    /**
     * Gets the t for the line parameterization for the intersection of the
     * given line with current relative phi. Note it only depends on the x and y
     * values of the wire endpoints, not the z.This is to be used only primarily
     * in sector views. After return, x = xa + T(xb-xa), etc.
     * 
     * @param xa
     *            x coordinate of one end point
     * @param ya
     *            y coordinate of one end point
     * @param xb
     *            x coordinate of other end point
     * @param yb
     *            y coordinate of other end point
     * @param tanphi
     *            the tangent of the local phi, whith phi [-30, 30]
     * @return the t value of the parameterization
     */
    public static double getPlaneIntersectionT(double xa, double ya, double xb,
	    double yb, double tanphi) {

	double delx = xb - xa;
	double dely = yb - ya;
	return (xa * tanphi - ya) / (dely - delx * tanphi);
    }

    public static void main(String arg[]) {
	double phis[] = { -27, 27, 39, 67, 82, 119, 209, 212, 245, 270, 279,
		337 };

	for (double val : phis) {
	    System.err.println("val: " + val + "  sect: " + getSector(val)
		    + "  rel phi: " + getRelativePhi(val));
	}
    }

    public static List<BSTxyPanel> getBSTxyPanels() {
	return _bstXYpanels;
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

    public static double getAbsMaxWireY() {
	return Math.max(Math.abs(minWireY), maxWireY);
    }

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
     * Obtain the 1-based sector from the xyz coordinates
     * 
     * @param clasP
     *            the lab xyz coordinates
     * @return the sector [1..6]
     */
    public static int clasToSectorNumber(Point3D clasP) {
	double phi = Math.toDegrees(Math.atan2(clasP.y(), clasP.x()));
	double ang = phi + 30;
	while (ang < 0) {
	    ang += 360;
	}
	return 1 + (int) (ang / 60);
    }

    /**
     * Converts the lab 3D coordinates to sector 3D coordinates
     * 
     * @param clasP
     *            the lab 3D Cartesian coordinates (not modified)
     * @param sectorP
     *            the sector 3D Cartesian coordinates (modified)
     */
    public static void clasToSector(Point3D clasP, Point3D sectorP) {
	int sector = clasToSectorNumber(clasP);
	if ((sector < 1) || (sector > 6)) {
	    String wstr = "Bad sector: " + sector + " in clasToSector";
	    Log.getInstance().error(wstr);
	    return;
	}

	if (sector == 1) {
	    sectorP.setX(clasP.x());
	    sectorP.setY(clasP.y());
	} else {
	    double x = clasP.x();
	    double y = clasP.y();
	    double midPlanePhi = Math.toRadians(60 * (sector - 1));
	    double cosPhi = Math.cos(midPlanePhi);
	    double sinPhi = Math.sin(midPlanePhi);
	    sectorP.setX(cosPhi * x + sinPhi * y);
	    sectorP.setY(-sinPhi * x + cosPhi * y);
	}

	// z coordinates are the same
	sectorP.setZ(clasP.z());
    }

    /**
     * Converts the sector 3D coordinates to clas (lab) 3D coordinates
     * 
     * @param sector
     *            the 1-based sector [1..6]
     * @param clasP
     *            the lab 3D Cartesian coordinates (modified)
     * @param sectorP
     *            the sector 3D Cartesian coordinates (not modified)
     */
    public static void sectorToClas(int sector, Point3D clasP, Point3D sectorP) {

	if ((sector < 1) || (sector > 6)) {
	    String wstr = "Bad sector: " + sector + " in sectorToClas";
	    Log.getInstance().error(wstr);
	    return;
	}

	if (sector == 1) {
	    clasP.setX(sectorP.x());
	    clasP.setY(sectorP.y());
	} else {
	    double x = sectorP.x();
	    double y = sectorP.y();
	    double midPlanePhi = Math.toRadians(60 * (sector - 1));
	    double cosPhi = Math.cos(midPlanePhi);
	    double sinPhi = Math.sin(midPlanePhi);
	    clasP.setX(cosPhi * x - sinPhi * y);
	    clasP.setY(sinPhi * x + cosPhi * y);
	}

	// z coordinates are the same
	clasP.setZ(sectorP.z());
    }

    /**
     * Obtain the 1-based sector from the xyz coordinates
     * 
     * @param labXYZ
     *            the lab xyz coordinates
     * @return the sector [1..6]
     */
    public static int labXYZToSectorNumber(double labXYZ[]) {
	double phi = Math.toDegrees(Math.atan2(labXYZ[1], labXYZ[0]));
	double ang = phi + 30;
	while (ang < 0) {
	    ang += 360;
	}
	return 1 + (int) (ang / 60);
    }

    /**
     * Converts the lab 3D coordinates to sector 3D coordinates
     * 
     * @param labXYZ
     *            the lab 3D Cartesian coordinates
     * @param sectorXYZ
     *            the sector 3D Cartesian coordinates
     */
    public static void labXYZToSectorXYZ(double labXYZ[], double sectorXYZ[]) {
	int sector = labXYZToSectorNumber(labXYZ);
	if ((sector < 1) || (sector > 6)) {
	    String wstr = "Bad sector: " + sector + " in labXYZToSectorXYZ";
	    Log.getInstance().error(wstr);
	    return;
	}

	if (sector == 1) {
	    sectorXYZ[0] = labXYZ[0];
	    sectorXYZ[1] = labXYZ[1];
	} else {
	    double x = labXYZ[0];
	    double y = labXYZ[1];
	    double midPlanePhi = Math.toRadians(60 * (sector - 1));
	    double cosPhi = Math.cos(midPlanePhi);
	    double sinPhi = Math.sin(midPlanePhi);
	    sectorXYZ[0] = cosPhi * x + sinPhi * y;
	    sectorXYZ[1] = -sinPhi * x + cosPhi * y;
	}

	// z coordinates are the same
	sectorXYZ[2] = labXYZ[2];
    }

    /**
     * Converts the lab 3D coordinates to sector 3D coordinates
     * 
     * @param sector
     *            the 1-based sector [1..6]
     * @param labXYZ
     *            the lab 3D Cartesian coordinates
     * @param sectorXYZ
     *            the sector 3D Cartesian coordinates
     */
    public static void sectorXYZToLabXYZ(int sector, double labXYZ[],
	    double sectorXYZ[]) {

	if ((sector < 1) || (sector > 6)) {
	    String wstr = "Bad sector: " + sector + " in sectorXYZToLabXYZ";
	    Log.getInstance().error(wstr);
	    return;
	}

	if (sector == 1) {
	    labXYZ[0] = sectorXYZ[0];
	    labXYZ[1] = sectorXYZ[1];
	} else {
	    double x = sectorXYZ[0];
	    double y = sectorXYZ[1];
	    double midPlanePhi = Math.toRadians(60 * (sector - 1));
	    double cosPhi = Math.cos(midPlanePhi);
	    double sinPhi = Math.sin(midPlanePhi);
	    labXYZ[0] = cosPhi * x - sinPhi * y;
	    labXYZ[1] = sinPhi * x + cosPhi * y;

	}

	// z coordinates are the same
	labXYZ[2] = sectorXYZ[2];
    }

    /**
     * Get the front plane of a calorimeter detector
     * 
     * @param sector
     *            the 1-based sector
     * @param superlayer
     *            0,1,2 for PCAL, EC_INNER, EC_OUTER
     * @return
     */
    public static Plane3D getCalFrontPlane(int sector, int superlayer) {
	if (superlayer == 0) {
	    return PCALGeometry.getFrontPlane(sector);
	} else {
	    return ECGeometry.getFrontPlane(sector, superlayer - 1);
	}
    }

    /**
     * Lab (CLAS) 3D Cartesian coordinates to world graphical coordinates.
     * 
     * @param superlayer
     *            0,1,2 for PCAL, EC_INNER, EC_OUTER
     * @param labXYZ
     *            the lab 3D coordinates
     * @param wp
     *            will hold the graphical world coordinates
     */
    public static void cal_labXYZToWorld(int superlayer, double labXYZ[],
	    Point2D.Double wp) {

	Point3D start = new Point3D(0, 0, 0);
	Point3D end = new Point3D(labXYZ[0], labXYZ[1], labXYZ[2]);

	int sector = getSector(labXYZ[0], labXYZ[1]);

	Plane3D plane = getCalFrontPlane(sector, superlayer);
	Line3D line3D = new Line3D(start, end);

	Point3D iPoint = new Point3D();
	plane.intersection(line3D, iPoint);
	wp.x = iPoint.x();
	wp.y = iPoint.y();

	// Path3D path = new Path3D();

	// path.addPoint(start);
	// path.addPoint(end);

	// //TODO find a way where I do not have to cache the detector
	// List<DetectorHit> isects =
	// GeometryManager.clas_Cal_Detector.getLayerHits(path);
	//
	// if ((isects != null) && (!isects.isEmpty())) {
	//
	// for (DetectorHit hit : isects) {
	// if ((hit.getSuperlayerId() == superlayer) && (hit.getLayerId() ==
	// layer)) {
	// Point3D hp = hit.getPosition();
	// wp.x = hp.x();
	// wp.y = hp.y();
	//
	// //TEST using plane
	// int sector = getSector(labXYZ[0], labXYZ[1]);
	// Plane3D plane = PCALGeometry.getFrontPlane(sector);
	// Line3D line3D = new Line3D(start, end);
	//
	// Point3D iPoint = new Point3D();
	// plane.intersection(line3D, iPoint);
	//
	// System.err.println("\nfrom getHits: " + hp);
	// System.err.println("from plane: " + iPoint);
	// return;
	// }
	// }
	// }
	//
	// wp.x = labXYZ[0];
	// wp.y = labXYZ[1];

    }

    /**
     * Geometry package transformation to phi=constant plane
     * 
     * @param phi
     *            the angle in degrees
     * @return the geometry package transformation
     */
    public static Transformation3D toConstantPhi(double phi) {
	Transformation3D transform3D = new Transformation3D();
	transform3D.rotateZ(Math.toRadians(-90));
	transform3D.rotateX(Math.toRadians(-90));
	transform3D.rotateZ(Math.toRadians(phi)); // phi rotation
	return transform3D;
    }

}
