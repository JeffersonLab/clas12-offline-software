package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.detector.ec.ECSector;
import org.jlab.geom.detector.ec.ECSuperlayer;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;

/**
 * Holds the EC geometry from the geometry packages
 * 
 * @author heddle
 *
 */
public class ECGeometry {

	/** constant for the inner EC */
	public static final int EC_INNER = 0;

	/** constant for the outer EC */
	public static final int EC_OUTER = 1;

	/** constant for the u strip index */
	public static final int EC_U = 0;

	/** constant for the u strip index */
	public static final int EC_V = 1;

	/** constant for the u strip index */
	public static final int EC_W = 2;
	
	//** stack names names */
	public static final String STACK_NAMES[] = {"Inner", "Outer"};

	//** plane or "view" names */
	public static final String PLANE_NAMES[] = {"U", "V", "W"};


	/** there are 36 strips for u, v and w */
	public static final int EC_NUMSTRIP = 36;

	public static double TRI_XMIN;
	public static double TRI_XMAX;
	public static double TRI_YMIN;
	public static double TRI_YMAX;
	public static double TRI_DELX;
	public static double TRI_DELY;

	// corners on the mid plane for the sector view
	private static FourPoints2D _corners[] = new FourPoints2D[2];

	// deltaK separating front of inner from front of outer
	private static double[] _deltaK = new double[2];

	// the normal vector in sector xyz (cm) from the nominal target to the
	// front planes of the inner EC. All coordinates are in cm. First index
	// [0,1]
	// is for plane second is for coordinate
	private static Point3D _r0[] = new Point3D[2];

	// vector in the same direction as r0 that goes to the back if the outer
	// plane
	private static Point3D _rf = new Point3D();

	// the inner and outer triangular boundaries used for the "hex" (fanned)
	// view
	// First index is for plane (inner and outer) second index is for the three
	// points
	private static Point3D[][] _triangleIJK = new Point3D[2][3];

	// The strips. First index is for plane (inner and outer)
	// second index if for strip stype EC_U, EC_V or EC_W,
	// third index is for the strip index [0..35]
	// and the fourth index is the point index [0..3]
	private static Point3D[][][][] _strips = new Point3D[2][3][EC_NUMSTRIP][4];

	// angles related to the _ro
	public static double THETA = Double.NaN; // radians
	public static double COSTHETA = Double.NaN;
	public static double SINTHETA = Double.NaN;
	public static double TANTHETA = Double.NaN;

	// limits of sector x
	private static double[] SECTXMAX = { Double.NaN, Double.NaN };
	private static double[] SECTXMIN = { Double.NaN, Double.NaN };

	// slopes of front planes
	private static double[] SLOPES = { Double.NaN, Double.NaN };

	private static Transformations _transformations[] = new Transformations[2];

	/**
	 * Obtain the separation between the front of the inner and the front of the
	 * outer in the k direction.
	 * 
	 * @return the separation in cm.
	 */
	public static double getDeltaK(int index) {
		return _deltaK[index];
	}

	/**
	 * Get the normal vector in sector xyz (cm) from the nominal target to the
	 * front plane of the inner EC. All coordinates are in cm.
	 * 
	 * @param index
	 *            the plane, EC_INNER or EC_OUTER
	 * @return the normal vector for the given plane
	 */
	public static Point3D getR0(int index) {
		return _r0[index];
	}

	/**
	 * Get the front plane of the PCAL
	 * 
	 * @param sector
	 *            the 1-based sector [1..6]
	 * @param plane
	 *            EC_INNER or EC_OUTER
	 * @return the front plane of the PCAL
	 */
	public static Plane3D getFrontPlane(int sector, int plane) {
		Point3D clasr0 = new Point3D();
		GeometryManager.sectorToClas(sector, clasr0, _r0[plane]);
		Plane3D plane3D = new Plane3D(clasr0.x(), clasr0.y(), clasr0.z(),
				clasr0.x(), clasr0.y(), clasr0.z());
		return plane3D;
	}

	/**
	 * Get the coordinate transformation object
	 * 
	 * @param index
	 *            the plane, EC_INNER or EC_OUTER
	 * @return the coordinate transformations
	 */
	public static Transformations getTransformations(int index) {
		return _transformations[index];
	}

	/**
	 * Final geometry preparation
	 */
	private static void finalizeGeometry() {

		// xlimits
		for (int i = 0; i < 2; i++) {
			double x0 = _r0[i].x();
			SECTXMIN[i] = x0 + getImin(i) * COSTHETA;
			SECTXMAX[i] = x0 + getImax(i) * COSTHETA;
		}

		// perpendicular vector to outside of outer plane in midplane
		_rf.setY(0);
		FourPoints2D corners = getCorners(EC_OUTER);

		Point2D.Double p2 = corners.get(1);

		_rf.setZ((p2.x + TANTHETA * p2.y) / (1 + TANTHETA * TANTHETA));
		_rf.setX(_rf.z() * TANTHETA);

		TRI_XMIN = Double.POSITIVE_INFINITY;
		TRI_XMAX = Double.NEGATIVE_INFINITY;
		TRI_YMIN = Double.POSITIVE_INFINITY;
		TRI_YMAX = Double.NEGATIVE_INFINITY;

		// fix outer triangle boundary

		double sectorXYZ[] = new double[3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				Point3D pijk = _triangleIJK[j][i];
				ijkToSectorXYZ(j, pijk, sectorXYZ);
				TRI_XMIN = Math.min(TRI_XMIN, sectorXYZ[0]);
				TRI_XMAX = Math.max(TRI_XMAX, sectorXYZ[0]);
				TRI_YMIN = Math.min(TRI_YMIN, sectorXYZ[1]);
				TRI_YMAX = Math.max(TRI_YMAX, sectorXYZ[1]);
			}
		}

		TRI_DELX = TRI_XMAX - TRI_XMIN;
		TRI_DELY = TRI_YMAX - TRI_YMIN;
	}

	/**
	 * Get a point from the IJK boundary
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @param pointIndex
	 *            the point index [0..2]
	 * @return the corresponding point
	 */
	public static Point3D getTrianglePoint(int planeIndex, int pointIndex) {
		return _triangleIJK[planeIndex][pointIndex];
	}

	/**
	 * Get a point from a u, v or w strip
	 * 
	 * @param planeIndex
	 *            either EC_INNER or EC_OUTER [0, 1]
	 * @param stripType
	 *            EC_U, EC_V, or EC_W [0..2]
	 * @param stripIndex
	 *            the strip index [0..(EC_NUMSTRIP-1)]
	 * @param pointIndex
	 *            the point index [0..3]
	 * @return
	 */
	public static Point3D getStripPoint(int planeIndex, int stripType,
			int stripIndex, int pointIndex) {
		return _strips[planeIndex][stripType][stripIndex][pointIndex];
	}

	/**
	 * Get the minimum value if the sector x coordinate in cm
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @return the minimum value of the i coordinate
	 */
	public static double getXmin(int planeIndex) {
		return SECTXMIN[planeIndex];
	}

	/**
	 * Get the minimum value if the sector x coordinate in cm
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @return the minimum value of the i coordinate
	 */
	public static double getXmax(int planeIndex) {
		return SECTXMAX[planeIndex];
	}

	/**
	 * Get the minimum value if the i coordinate in cm
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @return the minimum value of the i coordinate
	 */
	public static double getImin(int planeIndex) {
		return _triangleIJK[planeIndex][0].x();
	}

	/**
	 * Get the maximum value if the i coordinate in cm
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @return the maximum value of the i coordinate
	 */
	public static double getImax(int planeIndex) {
		return _triangleIJK[planeIndex][1].x();
	}

	/**
	 * Get the minimum value if the j coordinate in cm
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @return the minimum value of the j coordinate
	 */
	public static double getJmin(int planeIndex) {
		return _triangleIJK[planeIndex][2].y();
	}

	/**
	 * Get the maximum value if the j coordinate in cm
	 * 
	 * @param planeIndex
	 *            the plane index (EC_INNER or EC_OUTER) [0,1]
	 * @return the maximum value of the j coordinate
	 */
	public static double getJmax(int planeIndex) {
		return _triangleIJK[planeIndex][1].y();
	}

	/**
	 * For the front face of a given plane, compute z from x
	 * 
	 * @param planeIndex
	 *            EC_INNER or EC_OUTER [0,1]
	 * @param x
	 *            the x coordinate in cm
	 * @return the z coordinate in cm
	 */
	public static double zFromX(int planeIndex, double x) {
		double x0 = _r0[planeIndex].x();
		double z0 = _r0[planeIndex].z();
		return z0 + (x - x0) / SLOPES[planeIndex];
	}

	/**
	 * Get the midplane sector cs corners in cm
	 * 
	 * @param index
	 *            should be EC_INNER or EC_OUTER
	 * @return the corners for the inner or outer EC
	 */
	private static FourPoints2D getCorners(int index) {
		return _corners[index];
	}

	/**
	 * Obtain the shell (for sector views) for the whole inner or outer EC
	 * correct for the relative phi.
	 * 
	 * @param planeIndex
	 *            should be EC_INNER or EC_OUTER
	 * @param stripType
	 *            should be EC_U, EC_V, or EC_W
	 * @param projectionPlane 
	 *            the projection plane
	 * @return the shell for the whole panel.
	 */
	public static Point2D.Double[] getShell(int planeIndex, int stripType,
			Plane3D projectionPlane) {

		Point2D.Double wp[] = GeometryManager.allocate(4);
		
		// get last visible (intersecting) strip
		int lastIndex = EC_NUMSTRIP - 1;
//		while (!doesProjectedPolyFullyIntersect(planeIndex, stripType, lastIndex, projectionPlane)) {
//			lastIndex--;
//			if (lastIndex < 1) {
//				return null;
//			}
//		}

		Point2D.Double lastPP[] = null;
		lastPP = getIntersections(planeIndex, stripType, lastIndex, projectionPlane, true);
		
		int firstIndex = 0;
			
//		while (!doesProjectedPolyFullyIntersect(planeIndex, stripType, firstIndex, projectionPlane)) {
//			firstIndex++;
//		}
		Point2D.Double firstPP[] = null;
		firstPP = getIntersections(planeIndex, stripType, firstIndex, projectionPlane, true);

		
//		if ((planeIndex == ECGeometry.EC_INNER) && (stripType ==  ECGeometry.EC_W)) {
//			System.err.println("FIRSTINDEX: " + firstIndex + "  LASTINDEX: " + lastIndex);
//		}

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
	 * Converts 1-based uvw triplets to a pixel. NOTE: not all uvw triplets are
	 * "real". For example, there is no (36, 36, 36) triplet; those strips do
	 * not intersect. Proper triplets have u + w + w = (2N+1) or (2N+2), where N
	 * = 36. Possible uvw triplets always yield a positive pixel value from
	 * [1..1296].
	 * 
	 * @param u
	 *            the 1.based [1..36] u strip
	 * @param v
	 *            the 1 based [1..36] v strip
	 * @param w
	 *            the 1 based [1..36] w strip
	 * @return the pixel. Should be [1..1296]
	 */
	public static int pixelFromUVW(int u, int v, int w) {
		return (u * (u - 1) + v - w + 1);
	}

	/**
	 * Converts a pixel back to a uvw triplet
	 * 
	 * @param pixel
	 *            the pixel. Only meaningful for pixels in the range [1..1296]
	 * @param uvw
	 *            holds the 1-based strip ids
	 */
	public static void uvwFromPixel(int pixel, int uvw[]) {
		double root = Math.sqrt(pixel - 1.0);
		int r = (int) root;
		int i = pixel - r * r;
		uvw[EC_W] = EC_NUMSTRIP + 1 - (i + 1) / 2;
		uvw[EC_V] = uvw[EC_W] + i - r - 1;
		uvw[EC_U] = r + 1;

	}

	/**
	 * Is this uvw triplet a good pixel?
	 * 
	 * @param u
	 *            the 1-based u strip [1..36]
	 * @param v
	 *            the 1-based v strip [1..36]
	 * @param w
	 *            the 1-based w strip [1..36]
	 * @return <code>true</code> if this combination is actually realized.
	 */
	public static boolean goodPixel(int u, int v, int w) {
		int sum = u + v + w;
		return (sum == (2 * EC_NUMSTRIP + 1)) || (sum == (2 * EC_NUMSTRIP + 2));
	}

	/**
	 * Convert ijk coordinates to sector xyz
	 * 
	 * @param localP
	 *            the ijk coordinates
	 * @param sectorXYZ
	 *            the sector xyz coordinates
	 */
	public static void ijkToSectorXYZ(int plane, Point3D localP,
			double[] sectorXYZ) {

		Point3D sectorP = new Point3D();
		_transformations[plane].localToSector(localP, sectorP);
		sectorXYZ[0] = sectorP.x();
		sectorXYZ[1] = sectorP.y();
		sectorXYZ[2] = sectorP.z();
	}

	/**
	 * Initialize the EC Geometry
	 */
	public static void initialize() {

		System.out.println("\n=====================================");
		System.out.println("====  EC Geometry Initialization ====");
		System.out.println("=====================================");

		// obtain the transformations
		_transformations[EC_INNER] = new Transformations(DetectorType.EC_INNER);
		_transformations[EC_OUTER] = new Transformations(DetectorType.EC_OUTER);

		for (int plane = 0; plane < 2; plane++) {
			_r0[plane] = new Point3D(0, 0, 0);
			_transformations[plane].localToSector(_r0[plane]);
		}

		THETA = Math.atan2(_r0[EC_INNER].x(), _r0[EC_INNER].z());
		COSTHETA = Math.cos(THETA);
		SINTHETA = Math.sin(THETA);
		TANTHETA = Math.tan(THETA);

		ECSuperlayer clas_ecSuperlayer[] = new ECSuperlayer[2];

		// note the indices in the getSuperlayer are 1 and 2 instead
		// of 0 and 1 because the geo package uses 0 for PCAL
		clas_ecSuperlayer[EC_INNER] = GeometryManager.local_Cal_Sector0
				.getSuperlayer(1);
		clas_ecSuperlayer[EC_OUTER] = GeometryManager.local_Cal_Sector0
				.getSuperlayer(2);

		ECLayer ecLayer[][] = new ECLayer[2][3];

		for (int plane = 0; plane < 2; plane++) {
			for (int stripType = 0; stripType < 3; stripType++) {
				ecLayer[plane][stripType] = clas_ecSuperlayer[plane]
						.getLayer(stripType);
			}
		}

		Point3D zeroP = new Point3D(0, 0, 0);

		double rmag[] = new double[2];

		rmag[0] = _r0[0].distance(zeroP);
		rmag[1] = _r0[1].distance(zeroP);

		_deltaK[EC_INNER] = rmag[1] - rmag[0];
		_deltaK[EC_OUTER] = 1.5 * _deltaK[EC_INNER];

		// get the strips
		// The strips. First index is for plane (inner and outer)
		// second index if for strip stype EC_U, EC_V or EC_W,
		// third index is for the strip index [0..35]
		// and the fourth index is the point index [0..3]

		double minI[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double maxI[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		double minJ[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double maxJ[] = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };

		for (int plane = 0; plane < 2; plane++) {
			for (int stripType = 0; stripType < 3; stripType++) {
				for (int stripId = 0; stripId < EC_NUMSTRIP; stripId++) {
					ScintillatorPaddle strip = ecLayer[plane][stripType]
							.getComponent(stripId);
					_strips[plane][stripType][stripId][0] = strip
							.getVolumePoint(4);
					_strips[plane][stripType][stripId][1] = strip
							.getVolumePoint(5);
					_strips[plane][stripType][stripId][2] = strip
							.getVolumePoint(1);
					_strips[plane][stripType][stripId][3] = strip
							.getVolumePoint(0);

					Point3D p0 = _strips[plane][stripType][stripId][0];
					Point3D p1 = _strips[plane][stripType][stripId][0];
					Point3D p2 = _strips[plane][stripType][stripId][0];
					Point3D p3 = _strips[plane][stripType][stripId][0];

					minI[plane] = Math.min(minI[plane], p0.x());
					maxI[plane] = Math.max(maxI[plane], p0.x());
					minJ[plane] = Math.min(minJ[plane], p0.y());
					maxJ[plane] = Math.max(maxJ[plane], p0.y());

					minI[plane] = Math.min(minI[plane], p1.x());
					maxI[plane] = Math.max(maxI[plane], p1.x());
					minJ[plane] = Math.min(minJ[plane], p1.y());
					maxJ[plane] = Math.max(maxJ[plane], p1.y());

					minI[plane] = Math.min(minI[plane], p2.x());
					maxI[plane] = Math.max(maxI[plane], p2.x());
					minJ[plane] = Math.min(minJ[plane], p2.y());
					maxJ[plane] = Math.max(maxJ[plane], p2.y());

					minI[plane] = Math.min(minI[plane], p3.x());
					maxI[plane] = Math.max(maxI[plane], p3.x());
					minJ[plane] = Math.min(minJ[plane], p3.y());
					maxJ[plane] = Math.max(maxJ[plane], p3.y());
				}
			}
		} // plane loop

		// triangles

		_triangleIJK[EC_INNER][0] = new Point3D(minI[EC_INNER], 0, 0);
		_triangleIJK[EC_INNER][1] = new Point3D(maxI[EC_INNER], maxJ[EC_INNER],
				0);
		_triangleIJK[EC_INNER][2] = new Point3D(maxI[EC_INNER], minJ[EC_INNER],
				0);

		_triangleIJK[EC_OUTER][0] = new Point3D(minI[EC_OUTER], 0, 0);
		_triangleIJK[EC_OUTER][1] = new Point3D(maxI[EC_OUTER], maxJ[EC_OUTER],
				0);
		_triangleIJK[EC_OUTER][2] = new Point3D(maxI[EC_OUTER], minJ[EC_OUTER],
				0);

		// sector view midplane shells

		_corners[EC_INNER] = new FourPoints2D();
		_corners[EC_OUTER] = new FourPoints2D();

		Point3D rP0 = new Point3D(minI[EC_INNER], 0, 0);
		Point3D rP1 = new Point3D(minI[EC_INNER], 0, _deltaK[EC_INNER]);
		Point3D rP2 = new Point3D(maxI[EC_INNER], 0, _deltaK[EC_INNER]);
		Point3D rP3 = new Point3D(maxI[EC_INNER], 0, 0);

		_transformations[EC_INNER].localToSector(rP0);
		_transformations[EC_INNER].localToSector(rP1);
		_transformations[EC_INNER].localToSector(rP2);
		_transformations[EC_INNER].localToSector(rP3);

		_corners[EC_INNER].add(rP0.z(), rP0.x());
		_corners[EC_INNER].add(rP1.z(), rP1.x());
		_corners[EC_INNER].add(rP2.z(), rP2.x());
		_corners[EC_INNER].add(rP3.z(), rP3.x());

		double dely = _corners[EC_INNER].getPoints()[0].y
				- _corners[EC_INNER].getPoints()[3].y;
		double delx = _corners[EC_INNER].getPoints()[0].x
				- _corners[EC_INNER].getPoints()[3].x;
		SLOPES[EC_INNER] = dely / delx;

		// outer
		rP0 = new Point3D(minI[EC_OUTER], 0, 0);
		rP1 = new Point3D(minI[EC_OUTER], 0, _deltaK[EC_OUTER]);
		rP2 = new Point3D(maxI[EC_OUTER], 0, _deltaK[EC_OUTER]);
		rP3 = new Point3D(maxI[EC_OUTER], 0, 0);

		_transformations[EC_OUTER].localToSector(rP0);
		_transformations[EC_OUTER].localToSector(rP1);
		_transformations[EC_OUTER].localToSector(rP2);
		_transformations[EC_OUTER].localToSector(rP3);

		_corners[EC_OUTER].add(rP0.z(), rP0.x());
		_corners[EC_OUTER].add(rP1.z(), rP1.x());
		_corners[EC_OUTER].add(rP2.z(), rP2.x());
		_corners[EC_OUTER].add(rP3.z(), rP3.x());

		dely = _corners[EC_OUTER].getPoints()[0].y
				- _corners[EC_OUTER].getPoints()[3].y;
		delx = _corners[EC_OUTER].getPoints()[0].x
				- _corners[EC_OUTER].getPoints()[3].x;
		SLOPES[EC_OUTER] = dely / delx;

		finalizeGeometry();

	} // initialize

	/**
	 * Get the triangle for a given view for 3D
	 * 
	 * @param sector
	 *            the sector 1..6
	 * @param stack
	 *            (aka the superlayer) 1..2 for inner and outer
	 * @param view
	 *            (aka layer) 1..3 for u, v, w
	 * @param coords
	 *            will hold the corners as [x1, y1, z1, ..., x3, y3, z3]
	 */
	public static void getViewTriangle(int sector, int stack, int view,
			float coords[]) {
		// argh the geometry pakage superlayers are 1,2 rather than 0,1 because
		// they use 0 for PCAL. So stack does not need the -1, but view still
		// does.

		ECSector esect = GeometryManager.clas_Cal_Sector0;
		ECSuperlayer ecsl = esect.getSuperlayer(stack);

		ECLayer ecLayer = ecsl.getLayer(view - 1);

		// NOTE, each ec layer has one face, a triangle

		Triangle3D t3d = (Triangle3D) ecLayer.getBoundary().face(0);

		// translation

		double delK = 14.94; // PCAL val
		if (stack > 0) {
			delK = _deltaK[stack - 1];
		}
		// System.err.println("deltaK = " + delK);

		double dist = (view - 1) * (delK / 3);
		double xt = dist * Math.sin(Math.toRadians(25));
		double yt = 0;
		double zt = dist * Math.cos(Math.toRadians(25));

		for (int i = 0; i < 3; i++) {
			int j = 3 * i;
			Point3D corner = new Point3D(t3d.point(i));
			corner.translateXYZ(xt, yt, zt);

			if (sector > 1) {
				corner.rotateZ(Math.toRadians(60 * (sector - 1)));
			}

			coords[j] = (float) corner.x();
			coords[j + 1] = (float) corner.y();
			coords[j + 2] = (float) corner.z();
		}
	}

	/**
	 * Get the strips for use by 3D view
	 * 
	 * @param sector
	 *            the sector 1..6
	 * @param stack
	 *            (aka the superlayer) 1..2 for inner and outer
	 * @param view
	 *            (aka layer) 1..3 for u, v, w
	 * @param strip
	 *            1..36
	 * @param coords
	 *            holds the eight corners as [x1, y1, z1..x8, y8, z8]
	 */
	public static void getStrip(int sector, int stack, int view, int strip,
			float coords[]) {
		// argh the geometry pakage superlayers are 1,2 rather than 0,1 because
		// they use 0 for PCAL. So stack does not need the -1, but view still
		// does.
		// So does strip

		ECSector esect = GeometryManager.clas_Cal_Sector0;
		ECSuperlayer ecsl = esect.getSuperlayer(stack);
		ECLayer ecLayer = ecsl.getLayer(view - 1);

		// NOTE, each ec layer has one face, a triangle

		// for (int i = 0; i < 10; i++) {
		// Face3D face = ecLayer.getBoundary().face(i);
		// System.err.println("FACE[ " + (i+1) + "] = " + face);
		// }

		ScintillatorPaddle paddle = ecLayer.getComponent(strip - 1);

		Point3D v[] = new Point3D[8];

		double delK = 14.94; // PCAL val
		if (stack > 0) {
			delK = _deltaK[stack - 1];
		}

		double dist = (view - 1) * (delK / 3);
		double xt = dist * Math.sin(Math.toRadians(25));
		double yt = 0;
		double zt = dist * Math.cos(Math.toRadians(25));

		for (int i = 0; i < 8; i++) {
			v[i] = new Point3D(paddle.getVolumePoint(i));
			v[i].translateXYZ(xt, yt, zt);
		}

		if (sector > 1) {
			for (int i = 0; i < 8; i++) {
				v[i].rotateZ(Math.toRadians(60 * (sector - 1)));
			}
		}

		for (int i = 0; i < 8; i++) {
			int j = 3 * i;
			coords[j] = (float) v[i].x();
			coords[j + 1] = (float) v[i].y();
			coords[j + 2] = (float) v[i].z();
		}
	}
	
	/**
	 * 
	 * @param superlayer
	 *            0, 1 (EC_INNER or EC_OUTER)
	 * @param layer
	 *            EC_U, EC_V, EC_W
	 * @param stripid
	 *            the 0-based paddle id
	 * @param projectionPlane 
	 *            the projection plane
	 * @return <code>true</code> if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int superlayer, int layer,
			int stripid, 
			Plane3D projectionPlane) {
		
		ECLayer ecLayer = GeometryManager.clas_Cal_Sector0.getSuperlayer(
				superlayer + 1).getLayer(layer);

		ScintillatorPaddle strip = ecLayer.getComponent(stripid);
		return GeometryManager.doesProjectedPolyIntersect(strip, projectionPlane, 6, 4);
	}


	/**
	 * Get the intersections of a with a constant phi plane. If the paddle does
	 * not intersect (happens as phi grows) return null;
	 * 
	 * @param superlayer
	 *            0, 1 (EC_INNER or EC_OUTER)
	 * @param layer
	 *            EC_U, EC_V, EC_W
	 * @param stripid
	 *            the 0-based paddle id
	 * @param projectionPlane 
	 *            the projection plane
	 * @return the intersection points (z component will be 0).
	 */
	public static Point2D.Double[] getIntersections(int superlayer, int layer,
			int stripid, Plane3D projectionPlane, boolean offset) {
		// argh the geometry package superlayers are 1,2 rather than 0,1 because
		// they use 0 for PCAL--hence the +1

		ECLayer ecLayer = GeometryManager.clas_Cal_Sector0.getSuperlayer(
				superlayer+1).getLayer(layer);
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
			if (layer == EC_V) {
				double del = _deltaK[superlayer] / 3;
				offsetLine(p2d[0], p2d[1], del - 1);
				offsetLine(p2d[2], p2d[3], del - 1);
			} else if (layer == EC_W) {
				double del = 2 * _deltaK[superlayer] / 3;
				offsetLine(p2d[0], p2d[1], del - 2);
				offsetLine(p2d[2], p2d[3], del - 2);
			}

			offsetLine(p2d[2], p2d[3], (0.9 * (_deltaK[superlayer] / 3)) - 1);
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

	public static void main(String arg[]) {

		GeometryManager.getInstance();

		int sector = 1;
		int stack = 1;
		int view = 1;
		int strip = 36;

		float coords[] = new float[24];

		getStrip(sector, stack, view, strip, coords);

		for (int i = 0; i < 8; i++) {
			int j = 3 * i;

			System.err.println(String.format("%8.1f, %8.1f, %8.1f", coords[j],
					coords[j + 1], coords[j + 2]));
		}

		System.err.println();
		view = 3;
		getStrip(sector, stack, view, strip, coords);

		for (int i = 0; i < 8; i++) {
			int j = 3 * i;

			System.err.println(String.format("%8.1f, %8.1f, %8.1f", coords[j],
					coords[j + 1], coords[j + 2]));
		}

	}

}
