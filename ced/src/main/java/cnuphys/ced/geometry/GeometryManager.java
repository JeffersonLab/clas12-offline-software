package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.abs.AbstractComponent;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECSector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;

import cnuphys.bCNU.log.Log;
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
	 * Private constructor for the singleton.
	 */
	private GeometryManager() {

		// don't load if plugins only
		if (Ced.pluginsOnly()) {
			return;
		}

		// DC Geometry
		DCGeometry.initialize();

		// BMT micromegas geometry
		BMTGeometry.initialize();

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

		local_Cal_Sector0 = (new ECFactory())
				.createDetectorLocal(ecDataProvider).getSector(0);

		// get EC data
		ECGeometry.initialize();

		// get PCAL data
		PCALGeometry.initialize();

		// CND data
		CNDGeometry.initialize();

		// FTCal
		FTCALGeometry.initialize();
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
	 * Get the sector [1..6] from the phi value
	 * 
	 * @param phi the value of phi in degrees
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
	 * @param labX the lab x
	 * @param labY the lab y
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
	 * @param absPhi the absolute value of phi
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

	// /**
	// * Get the intersection of the given line with RELATIVE phi plane. All
	// * coordinates are in the sector system.
	// *
	// * @param xa
	// * sector x coordinate of one end point
	// * @param ya
	// * sector y coordinate of one end point
	// * @param za
	// * sector z coordinate of one end point
	// * @param xb
	// * sector x coordinate of other end point
	// * @param yb
	// * sector y coordinate of other end point
	// * @param zb
	// * sector z coordinate of other end point
	// * @param relativePhi
	// * the relative phi (relative to the midplane) [-30..30]
	// * @param wp
	// * will hold the intersection. wp.x will be the horizontal
	// * coordinate (i.e., z in the usual sector system) and wp.y will
	// * be the cylindrical r (or rho) in the sector system. wp.y is
	// * positive definite, so should be negated for lower sectors
	// * (4,5,6) in sector views
	// */
	// public static void getPlaneIntersection(double xa, double ya, double za,
	// double xb, double yb, double zb, double relativePhi,
	// Point2D.Double wp) {
	//
	// double tanphi = Math.tan(-Math.toRadians(relativePhi));
	//
	// // not t value depends only on x and y, not z
	// double t = getPlaneIntersectionT(xa, ya, xb, yb, tanphi);
	//
	// // get intersection
	// double x = xa + t * (xb - xa);
	// double y = ya + t * (yb - ya);
	// double z = za + t * (zb - za);
	//
	// wp.x = z;
	// wp.y = Math.hypot(x, y);
	// }

	// /**
	// * Gets the t for the line parameterization for the intersection of the
	// * given line with current relative phi. Note it only depends on the x and
	// y
	// * values of the wire endpoints, not the z.This is to be used only
	// primarily
	// * in sector views. After return, x = xa + T(xb-xa), etc.
	// *
	// * @param xa
	// * x coordinate of one end point
	// * @param ya
	// * y coordinate of one end point
	// * @param xb
	// * x coordinate of other end point
	// * @param yb
	// * y coordinate of other end point
	// * @param tanphi
	// * the tangent of the local phi, whith phi [-30, 30]
	// * @return the t value of the parameterization
	// */
	// public static double getPlaneIntersectionT(double xa, double ya, double
	// xb,
	// double yb, double tanphi) {
	//
	// double delx = xb - xa;
	// double dely = yb - ya;
	// return (xa * tanphi - ya) / (dely - delx * tanphi);
	// }
	//
	// public static void main(String arg[]) {
	// double phis[] = { -27, 27, 39, 67, 82, 119, 209, 212, 245, 270, 279,
	// 337 };
	//
	// for (double val : phis) {
	// System.err.println("val: " + val + " sect: " + getSector(val)
	// + " rel phi: " + getRelativePhi(val));
	// }
	// }

	public static List<BSTxyPanel> getBSTxyPanels() {
		return _bstXYpanels;
	}

	/**
	 * Obtain the 1-based sector from the xyz coordinates
	 * 
	 * @param clasP the lab xyz coordinates
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
	 * @param clasP the lab 3D Cartesian coordinates (not modified)
	 * @param sectorP the sector 3D Cartesian coordinates (modified)
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
		}
		else {
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
	 * Converts the lab 3D coordinates to sector 3D coordinates
	 * 
	 * @param x the lab (clas) x coordinate
	 * @param y the lab (clas) y coordinate
	 * @param z the lab (clas) z coordinate
	 * @param sectorP the sector 3D Cartesian coordinates (modified)
	 */
	public static void clasToSector(double x, double y, double z, Point3D sectorP) {
		Point3D clasP = new Point3D(x, y, z);
		clasToSector(clasP, sectorP);
	}


	/**
	 * Converts the sector 3D coordinates to clas (lab) 3D coordinates
	 * 
	 * @param sector the 1-based sector [1..6]
	 * @param clasP the lab 3D Cartesian coordinates (modified)
	 * @param sectorP the sector 3D Cartesian coordinates (not modified)
	 */
	public static void sectorToClas(int sector, Point3D clasP,
			Point3D sectorP) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in sectorToClas";
			Log.getInstance().error(wstr);
			return;
		}

		if (sector == 1) {
			clasP.setX(sectorP.x());
			clasP.setY(sectorP.y());
		}
		else {
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
	 * @param labXYZ the lab xyz coordinates
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
	 * @param labXYZ the lab 3D Cartesian coordinates
	 * @param sectorXYZ the sector 3D Cartesian coordinates
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
		}
		else {
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
	 * @param sector the 1-based sector [1..6]
	 * @param labXYZ the lab 3D Cartesian coordinates
	 * @param sectorXYZ the sector 3D Cartesian coordinates
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
		}
		else {
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
	 * @param sector the 1-based sector
	 * @param superlayer 0,1,2 for PCAL, EC_INNER, EC_OUTER
	 * @return
	 */
	public static Plane3D getCalFrontPlane(int sector, int superlayer) {
		if (superlayer == 0) {
			return PCALGeometry.getFrontPlane(sector);
		}
		else {
			return ECGeometry.getFrontPlane(sector, superlayer - 1);
		}
	}

	/**
	 * Lab (CLAS) 3D Cartesian coordinates to world graphical coordinates.
	 * 
	 * @param superlayer 0,1,2 for PCAL, EC_INNER, EC_OUTER
	 * @param labXYZ the lab 3D coordinates
	 * @param wp will hold the graphical world coordinates
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
	 * @param phi the angle in degrees
	 * @return the geometry package transformation
	 */
	public static Transformation3D toConstantPhi(double phi) {
		Transformation3D transform3D = new Transformation3D();
		transform3D.rotateZ(Math.toRadians(-90));
		transform3D.rotateX(Math.toRadians(-90));
		transform3D.rotateZ(Math.toRadians(phi)); // phi rotation
		
//		transform3D.rotateX(Math.toRadians(-90));
//		transform3D.rotateZ(Math.toRadians(phi)); // phi rotation

		return transform3D;
	}
	
	/**
	 * Get a world 2D polygon from a clas geo object like a FTOF slab.
	 * @param geoObj the geometric object
	 * @param projectionPlane the projection plane
	 * @param startIndex the firstIndex of the volume edges corresponding to a "long" edge
	 * @param count the number of such edges (should be contiguous!)
	 * @param wp will hold the world 2D polygon
	 * @param centroid optionally compute the centroid.
	 */
	public static void getProjectedPolygon(AbstractComponent geoObj, Plane3D projectionPlane, int startIndex, int count, Point2D.Double wp[], Point2D.Double centroid) {
		Point3D pp[] = new Point3D[count];
		for (int i = 0; i < count; i++) {
			pp[i] = new Point3D();
		}

		for (int i = 0; i <count; i++) {
			int index = startIndex + i;
			Line3D l3d = geoObj.getVolumeEdge(index);
			projectionPlane.intersection(l3d, pp[i]);
		}
		
		Vector<Line3D> lines = new Vector<Line3D>();
		for (int i = 0; i < count; i++) {
			int j = (i+1) % count;
			lines.add(new Line3D(pp[i], pp[j]));
		}
		
		for (int i = 0; i < count; i++) {
			wp[i].x = pp[i].z();
			wp[i].y = Math.hypot(pp[i].x(), pp[i].y());
		}
		
		if (centroid != null) {
			average(wp, centroid);
		}

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
	
	/**
	 * Project a space point. Projected by finding the closest point on the plane.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param projectionPlane the projection plane
	 * @param wp will hold the projected 2D world point
	 * @return the projected 3D space point
	 */
	public static Point3D projectedPoint(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {
		Point3D p1 = new Point3D(x, y, z);
		Vector3D normal = projectionPlane.normal();
		Point3D p2 = new Point3D(p1.x() + normal.x(),
				p1.y() + normal.y(), p1.z() + normal.z());
		Line3D perp = new Line3D(p1, p2);
		Point3D pisect = new Point3D();
		projectionPlane.intersection(perp, pisect);
		
		wp.x = pisect.z();
		wp.y = Math.hypot(pisect.x(), pisect.y());
		return pisect;
	}


	/**
	 * Get a plane of constant phi
	 * @param phi the azimutha angle in degrees
	 * @return a plane of constant phi
	 */
	public static Plane3D constantPhiPlane(double phi) {
		Point3D point = new Point3D(0, 0, 0);
		phi = Math.toRadians(phi);
		double cphi = Math.cos(phi);
		double sphi = Math.sin(phi);
		Vector3D norm = new Vector3D(sphi, -cphi, 0);
		return new Plane3D(point, norm);
	}

	/**
	 * allocate an array of allocated points
	 * 
	 * @param n the number of points
	 * @return an array of allocated points
	 */
	public static Point2D.Double[] allocate(int n) {
		Point2D.Double[] wp = new Point2D.Double[n];
		for (int i = 0; i < n; i++) {
			wp[i] = new Point2D.Double();
		}
		return wp;
	}

}
