package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.detector.ec.ECSuperlayer;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

/**
 * Holds the EC geometery from the geometry packages
 * 
 * @author heddle
 *
 */
public class PCALGeometry {

	/** index for the geometry package */
	private static final int EC_PCAL = 0;

	/** constant for the u strip index */
	public static final int PCAL_U = 0;

	/** constant for the u strip index */
	public static final int PCAL_V = 1;

	/** constant for the u strip index */
	public static final int PCAL_W = 2;
	
	//** plane or "view" names */
	public static final String PLANE_NAMES[] = {"U", "V", "W"};

	/** there are 36 strips for u, v and w */
	public static final int PCAL_NUMSTRIP[] = { 68, 62, 62 };

	public static double TRI_XMIN;
	public static double TRI_XMAX;
	public static double TRI_YMIN;
	public static double TRI_YMAX;
	public static double TRI_DELX;
	public static double TRI_DELY;

	// deltaK separating front of inner from front of outer
	private static double _deltaK = 14.94;

	// the normal vector in sector xyz (cm) from the nominal target to the
	// front plNE
	private static Point3D _r0;

	// the inner and outer triangular boundaries used for the "hex" (fanned)
	// view
	// Index is for the three points
	private static Point3D[] _triangleIJK = new Point3D[3];

	// The strips.
	// First index if for strip type U, V or W,
	// 2nd index is for the strip index [0..(62 or 68)]
	// and the third index is the point index [0..3]
	private static Point3D[][][] _strips = new Point3D[3][68][4];

	// angles related to the _ro
	public static double THETA = Double.NaN; // radians
	public static double COSTHETA = Double.NaN;
	public static double SINTHETA = Double.NaN;
	public static double TANTHETA = Double.NaN;

	// limits of sector x
	private static double SECTXMAX = Double.NaN;
	private static double SECTXMIN = Double.NaN;

	// slopes of front planes
	private static double SLOPE = Double.NaN;

	// used for coordinate transformations
	private static Transformations _transformations;
	

	/**
	 * Obtain the separation between the front of the inner and the front of the
	 * outer in the k direction.
	 * 
	 * @return the separation in cm.
	 */
	public static double getDeltaK() {
		return _deltaK;
	}

	/**
	 * Get the normal vector in sector xyz (cm) from the nominal target to the
	 * front plane of the inner EC. All coordinates are in cm.
	 * 
	 * @return the normal vector for the given plane
	 */
	public static Point3D getR0() {
		return _r0;
	}

	/**
	 * Get the front plane of the PCAL
	 * 
	 * @param sector
	 *            the 1-based sector [1..6]
	 * @return the front plane of the PCAL
	 */
	public static Plane3D getFrontPlane(int sector) {
		Point3D clasr0 = new Point3D();
		GeometryManager.sectorToClas(sector, clasr0, _r0);
		Plane3D plane = new Plane3D(clasr0.x(), clasr0.y(), clasr0.z(),
				clasr0.x(), clasr0.y(), clasr0.z());
		return plane;
	}

	/**
	 * Get the coordinate transformation object
	 * 
	 * @return the coordinate transformations
	 */
	public static Transformations getTransformations() {
		return _transformations;
	}

	/**
	 * Final geometry preparation
	 */
	private static void finalizeGeometry() {

		// xlimits
		double x0 = _r0.x();
		SECTXMIN = x0 + getImin() * COSTHETA;
		SECTXMAX = x0 + getImax() * COSTHETA;

		TRI_XMIN = Double.POSITIVE_INFINITY;
		TRI_XMAX = Double.NEGATIVE_INFINITY;
		TRI_YMIN = Double.POSITIVE_INFINITY;
		TRI_YMAX = Double.NEGATIVE_INFINITY;

		// fix outer triangle boundary

		double sectorXYZ[] = new double[3];
		for (int i = 0; i < 3; i++) {
			Point3D pijk = _triangleIJK[i];
			ijkToSectorXYZ(pijk, sectorXYZ);
			TRI_XMIN = Math.min(TRI_XMIN, sectorXYZ[0]);
			TRI_XMAX = Math.max(TRI_XMAX, sectorXYZ[0]);
			TRI_YMIN = Math.min(TRI_YMIN, sectorXYZ[1]);
			TRI_YMAX = Math.max(TRI_YMAX, sectorXYZ[1]);
		}

		TRI_DELX = TRI_XMAX - TRI_XMIN;
		TRI_DELY = TRI_YMAX - TRI_YMIN;
	}

	/**
	 * Get a point from the IJK boundary
	 * 
	 * @param pointIndex
	 *            the point index [0..2]
	 * @return the corresponding point
	 */
	public static Point3D getTrianglePoint(int pointIndex) {
		return _triangleIJK[pointIndex];
	}

	/**
	 * Get a point from a u, v or w strip
	 * 
	 * @param stripType
	 *            EC_U, EC_V, or EC_W [0..2]
	 * @param stripIndex
	 *            the strip index [0..(PCAL_NUMSTRIP-1)]
	 * @param pointIndex
	 *            the point index [0..3]
	 * @return
	 */
	public static Point3D getStripPoint(int stripType, int stripIndex,
			int pointIndex) {
		return _strips[stripType][stripIndex][pointIndex];
	}

	/**
	 * Get the minimum value if the sector x coordinate in cm
	 * 
	 * @return the minimum value of the i coordinate
	 */
	public static double getXmin() {
		return SECTXMIN;
	}

	/**
	 * Get the minimum value if the sector x coordinate in cm
	 * 
	 * @return the minimum value of the i coordinate
	 */
	public static double getXmax() {
		return SECTXMAX;
	}

	/**
	 * Get the minimum value if the i coordinate in cm
	 * 
	 * @return the minimum value of the i coordinate
	 */
	public static double getImin() {
		return _triangleIJK[0].x();
	}

	/**
	 * Get the maximum value if the i coordinate in cm
	 * 
	 * @return the maximum value of the i coordinate
	 */
	public static double getImax() {
		return _triangleIJK[1].x();
	}

	/**
	 * Get the minimum value if the j coordinate in cm
	 * 
	 * @return the minimum value of the j coordinate
	 */
	public static double getJmin() {
		return _triangleIJK[2].y();
	}

	/**
	 * Get the maximum value if the j coordinate in cm
	 * 
	 * @return the maximum value of the j coordinate
	 */
	public static double getJmax(int planeIndex) {
		return _triangleIJK[1].y();
	}

	/**
	 * For the front face of a given plane, compute z from x
	 * 
	 * @param x
	 *            the x coordinate in cm
	 * @return the z coordinate in cm
	 */
	public static double zFromX(double x) {
		double x0 = _r0.x();
		double z0 = _r0.z();
		return z0 + (x - x0) / SLOPE;
	}

	/**
	 * Get the triangle for a given view for 3D
	 * 
	 * @param sector
	 *            the sector 1..6
	 * @param view
	 *            (aka layer) 1..3 for u, v, w
	 * @param coords
	 *            will hold the corners as [x1, y1, z1, ..., x3, y3, z3]
	 */
	public static void getViewTriangle(int sector, int view, float coords[]) {
		// in geometry package, PCAL is same sa EC with stack (superlayer) = 0
		ECGeometry.getViewTriangle(sector, 0, view, coords);

	}

	/**
	 * Get the strips for use by 3D view
	 * 
	 * @param sector
	 *            the sector 1..6
	 * @param view
	 *            (aka layer) 1..3 for u, v, w
	 * @param strip
	 *            1..36
	 * @param coords
	 *            holds the eight corners as [x1, y1, z1..x8, y8, z8]
	 */
	public static void getStrip(int sector, int view, int strip, float coords[]) {
		// in geometry package, PCAL is same sa EC with stack (superlayer) = 0
		ECGeometry.getStrip(sector, 0, view, strip, coords);
	}

	/**
	 * Obtain the shell (for sector views) for the whole PCAL correct for the
	 * relative phi.
	 * 
	 * @param stripType
	 *            should be PCAL_U, PCAL_V, or PCAL_W
	 * @param projectionPlane
	 *            the projection plane
	 * @return the shell for the whole panel.
	 */
	public static Point2D.Double[] getShell(int stripType, Plane3D projectionPlane) {

		Point2D.Double wp[] = new Point2D.Double[4];
		for (int i = 0; i < 4; i++) {
			wp[i] = new Point2D.Double();
		}

		// get last visible (intersecting) strip
		int lastIndex = PCAL_NUMSTRIP[stripType] - 1;
		while (!doesProjectedPolyFullyIntersect(stripType, lastIndex, projectionPlane)) {
			lastIndex--;
			if (lastIndex < 1) {
				return null;
			}
		}

		Point2D.Double lastPP[] = null;
		lastPP = getIntersections(stripType, lastIndex, projectionPlane, true);
		
		int firstIndex = 0;
			
		while (!doesProjectedPolyFullyIntersect(stripType, firstIndex, projectionPlane)) {
			firstIndex++;
		}
		Point2D.Double firstPP[] = null;
		firstPP = getIntersections(stripType, firstIndex, projectionPlane, true);
		
		if (lastPP[0].y > firstPP[0].y) {
			wp[0] = lastPP[0];
			wp[1] = firstPP[1];
			wp[2] = firstPP[2];
			wp[3] = lastPP[3];
		} else {
			wp[0] = firstPP[0];
			wp[1] = lastPP[1];
			wp[2] = lastPP[2];
			wp[3] = firstPP[3];

		}

		return wp;
	}

	/**
	 * Convert ijk coordinates to sector xyz
	 * 
	 * @param pijk
	 *            the ijk coordinates
	 * @param sectorXYZ
	 *            the sector xyz coordinates
	 */
	public static void ijkToSectorXYZ(Point3D localP, double[] sectorXYZ) {

		Point3D sectorP = new Point3D();
		_transformations.localToSector(localP, sectorP);
		sectorXYZ[0] = sectorP.x();
		sectorXYZ[1] = sectorP.y();
		sectorXYZ[2] = sectorP.z();

		// sectorXYZ[0] = _r0.x() + (pijk.x() * COSTHETA) + (pijk.z() *
		// SINTHETA);
		// sectorXYZ[1] = pijk.y();
		// sectorXYZ[2] = _r0.z() + (pijk.z() * COSTHETA) - (pijk.x() *
		// SINTHETA);
	}

	/**
	 * Initialize the EC Geometry
	 */
	public static void initialize() {

		System.out.println("\n=====================================");
		System.out.println("===  PCAL Geometry Initialization ===");
		System.out.println("=====================================");

		// obtain the transformations
		_transformations = new Transformations(DetectorType.PCAL);

		_r0 = new Point3D(0, 0, 0);
		_transformations.localToSector(_r0);

		THETA = Math.atan2(_r0.x(), _r0.z());
		COSTHETA = Math.cos(THETA);
		SINTHETA = Math.sin(THETA);
		TANTHETA = Math.tan(THETA);

		ECSuperlayer ecSuperlayer = GeometryManager.local_Cal_Sector0
				.getSuperlayer(EC_PCAL);

		ECLayer[] ecLayer = new ECLayer[3];

		ecLayer[PCAL_U] = ecSuperlayer.getLayer(PCAL_U);
		ecLayer[PCAL_V] = ecSuperlayer.getLayer(PCAL_V);
		ecLayer[PCAL_W] = ecSuperlayer.getLayer(PCAL_W);

		// get the strips
		// The strips. First index is for plane (inner and outer)
		// second index if for strip stype EC_U, EC_V or EC_W,
		// third index is for the strip index [0..35]
		// and the fourth index is the point index [0..3]

		double minI = Double.POSITIVE_INFINITY;
		double maxI = Double.NEGATIVE_INFINITY;
		double minJ = Double.POSITIVE_INFINITY;
		double maxJ = Double.NEGATIVE_INFINITY;

		for (int stripType = 0; stripType < 3; stripType++) {
			for (int stripId = 0; stripId < PCAL_NUMSTRIP[stripType]; stripId++) {
				ScintillatorPaddle strip = ecLayer[stripType]
						.getComponent(stripId);
				_strips[stripType][stripId][0] = strip.getVolumePoint(4);
				_strips[stripType][stripId][1] = strip.getVolumePoint(5);
				_strips[stripType][stripId][2] = strip.getVolumePoint(1);
				_strips[stripType][stripId][3] = strip.getVolumePoint(0);

				Point3D p0 = _strips[stripType][stripId][0];
				Point3D p1 = _strips[stripType][stripId][0];
				Point3D p2 = _strips[stripType][stripId][0];
				Point3D p3 = _strips[stripType][stripId][0];

				minI = Math.min(minI, p0.x());
				maxI = Math.max(maxI, p0.x());
				minJ = Math.min(minJ, p0.y());
				maxJ = Math.max(maxJ, p0.y());

				minI = Math.min(minI, p1.x());
				maxI = Math.max(maxI, p1.x());
				minJ = Math.min(minJ, p1.y());
				maxJ = Math.max(maxJ, p1.y());

				minI = Math.min(minI, p2.x());
				maxI = Math.max(maxI, p2.x());
				minJ = Math.min(minJ, p2.y());
				maxJ = Math.max(maxJ, p2.y());

				minI = Math.min(minI, p3.x());
				maxI = Math.max(maxI, p3.x());
				minJ = Math.min(minJ, p3.y());
				maxJ = Math.max(maxJ, p3.y());

			}
		}

		// triangles
		// private static Point3D[][] _triangleIJK = new Point3D[2][3];

		_triangleIJK[0] = new Point3D(minI, 0, 0);
		_triangleIJK[1] = new Point3D(maxI, maxJ, 0);
		_triangleIJK[2] = new Point3D(maxI, minJ, 0);

		_triangleIJK[0] = new Point3D(minI, 0, 0);
		_triangleIJK[1] = new Point3D(maxI, maxJ, 0);
		_triangleIJK[2] = new Point3D(maxI, minJ, 0);

		// sector view midplane shells

		Point3D rP0 = new Point3D(minI, 0, 0);
		Point3D rP1 = new Point3D(minI, 0, _deltaK);
		Point3D rP2 = new Point3D(maxI, 0, _deltaK);
		Point3D rP3 = new Point3D(maxI, 0, 0);

		_transformations.localToSector(rP0);
		_transformations.localToSector(rP1);
		_transformations.localToSector(rP2);
		_transformations.localToSector(rP3);

		double dely = rP0.x() - rP3.x();
		double delx = rP0.z() - rP3.z();
		SLOPE = dely / delx;

		finalizeGeometry();
	} // initialize

	/**
	 * @param layer
	 *            PCAL_U, PCAL_V, PCAL_W
	 * @param stripid
	 *            the 0-based paddle id
	 * @param projectionPlane
	 *            the projection plane
	 * @return <code>true</code> if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int layer,
			int stripid, 
			Plane3D projectionPlane) {
		
		ECLayer ecLayer = GeometryManager.clas_Cal_Sector0.getSuperlayer(
				EC_PCAL).getLayer(layer);
		ScintillatorPaddle strip = ecLayer.getComponent(stripid);
		Point2D.Double wp[] = GeometryManager.allocate(4);
		return GeometryManager.doesProjectedPolyIntersect(strip, projectionPlane, 6, 4);
	}

	/**
	 * Get the intersections of a with a constant phi plane. If the paddle does
	 * not intersect (happens as phi grows) return null;
	 * 
	 * @param layer
	 *            PCAL_U, PCAL_V, PCAL_W
	 * @param stripid
	 *            the 0-based paddle id
	 * @param projectionPlane
	 *            the projection plane
	 * @return the intersection points (z component will be 0).
	 */
	public static Point2D.Double[] getIntersections(int layer, int stripid,
			Plane3D projectionPlane, boolean offset) {

		ECLayer ecLayer = GeometryManager.clas_Cal_Sector0.getSuperlayer(
				EC_PCAL).getLayer(layer);
		ScintillatorPaddle strip = ecLayer.getComponent(stripid);
		Point2D.Double wp[] = GeometryManager.allocate(4);
		boolean isects = GeometryManager.getProjectedPolygon(strip, projectionPlane, 6, 4, wp, null);
		
		// note reordering
		Point2D.Double p2d[] = new Point2D.Double[4];

		p2d[0] = new Point2D.Double(wp[2].x, wp[2].y);
		p2d[1] = new Point2D.Double(wp[3].x, wp[3].y);
		p2d[2] = new Point2D.Double(wp[0].x, wp[0].y);
		p2d[3] = new Point2D.Double(wp[1].x, wp[1].y);

		if (offset) {
			// move
			if (layer == PCAL_V) {
				double del = _deltaK / 3;
				offsetLine(p2d[0], p2d[1], del - 1);
				offsetLine(p2d[2], p2d[3], del - 1);
			} else if (layer == PCAL_W) {
				double del = 2 * _deltaK / 3;
				offsetLine(p2d[0], p2d[1], del - 2);
				offsetLine(p2d[2], p2d[3], del - 2);
			}

			offsetLine(p2d[2], p2d[3], (0.9 * (_deltaK / 3)) - 1);
		}

		return p2d;
	}

	private static void offsetLine(Point2D.Double start, Point2D.Double end,
			double len) {
		double delx = len * COSTHETA;
		double dely = len * SINTHETA;
		start.x += delx;
		start.y += dely;
		end.x += delx;
		end.y += dely;
	}

}