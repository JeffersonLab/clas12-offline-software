package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.detector.bst.BSTFactory;
import org.jlab.geom.detector.bst.BSTLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

public class BSTGeometry {

	// use coat java or peter's
	public static final boolean USECOATJAVA = true;

	// used for coordinate transformations
	private static DetectorTransformation _transform;

	private static ConstantProvider bstDataProvider = GeometryFactory
			.getConstants(org.jlab.detector.base.DetectorType.BST);

	private static BSTFactory bstFactory = new BSTFactory();

	// the base layer that we will transform to get what we need
	private static BSTLayer _svtLayer0;
	private static BSTLayer _svtLayer1;

	// for putting in dead zone
	public static final double ZGAP = 1.67; // mm

	/** sectors per superlayer */
	public static final int[] sectorsPerSuperlayer = { 10, 14, 18, 24 };

	/**
	 * Initialize the BST Geometry
	 */
	public static void initialize() {

		System.out.println("\n=====================================");
		System.out.println("===  BST Geometry Initialization  ===");
		System.out.println("=====================================");

		if (USECOATJAVA) {
			// create a ring layer
			_svtLayer0 = bstFactory.createRingLayer(bstDataProvider, 0, 0, 0);
			_svtLayer1 = bstFactory.createRingLayer(bstDataProvider, 0, 0, 1);

			// create a detector transform
			_transform = bstFactory.getDetectorTransform(bstDataProvider);
		} else {
		}
	}

	/**
	 * Get the strip as a line
	 * 
	 * @param sector
	 *            (0-based) number of sectors are {10, 14, 18, 24} for
	 *            superlayers {0, 1, 2, 3} respectively.
	 * @param superlayer
	 *            in the range [0..3]
	 * @param layer
	 *            either 0 or 1
	 * @param strip
	 *            should be in the range [0..255]
	 * @return the strip as a line
	 */
	public static Line3D getStrip(int sector, int superlayer, int layer, int strip) {
		if ((strip < 0) || (strip > 255)) {
//			System.err.println("Bad strip ID " + (strip + 1) + " in BST bank.");
			return null;
		}

		Line3D tempLine;
		try {
			if (layer == 0) {
				tempLine = _svtLayer0.getComponent(strip).getLine();
			} else {
				tempLine = _svtLayer1.getComponent(strip).getLine();
			}

			Transformation3D t3d = _transform.get(sector, superlayer, layer);
			Line3D line = new Line3D(tempLine);
			t3d.apply(line);
			return line;

		} catch (NullPointerException npe) {
			System.err.println("BST GetStrip " + npe.getMessage());
			System.err.println("sector: " + sector);
			System.err.println("superlayer: " + superlayer);
			System.err.println("layer: " + layer);
			System.err.println("strip: " + strip);
		}

		return null;

	}

	/**
	 * 
	 * @param sector
	 *            (0-based) number of sectors are {10, 14, 18, 24} for
	 *            superlayers {0, 1, 2, 3} respectively.
	 * @param superlayer
	 *            in the range [0..3]
	 * @param layer
	 *            either 0 or 1
	 * @param p3d
	 * @return
	 */
	// public static Point3D inverseRotate(int sector, int superlayer, int
	// layer, Point3D p3d) {
	// Transformation3D t3d = _transform.get(sector, superlayer,
	// layer).inverse();
	// Point3D p = new Point3D(p3d);
	// t3d.apply(p);
	// return p;
	// }

	/**
	 * Get the coordinates (a line) for a strip for 3D view
	 * 
	 * @param sector
	 *            the 1-based layer dependent sector
	 * @param layer
	 *            the "big" layer 1..8
	 * @param strip
	 *            the strip 1..256
	 * @param coords
	 *            (dim = 6) will hold line as [x1,y1,z1,x2,y2,z2] in cm
	 */
	public static void getStrip(int sector, int layer, int strip, float coords[]) {
		// geom service uses 0-based superlayer and layer
		int supl = ((layer - 1) / 2); // 0, 1, 2, 3 (ring)
		int lay = ((layer - 1) % 2); // 0, 1, 0, 1
		
		int numSect = BSTGeometry.sectorsPerSuperlayer[supl];

		//HACK fix mismatch reality vs. Geo database
		sector = GeometryManager.getInstance().svtSectorHack(numSect, sector);

//		System.err.print("STRIP: " + strip);
//		//HACK on STrip
//		strip = 256-strip;
//		System.err.println("    HACK STRIP: " + strip);

		// note supl and lay just computed as zero based
		Line3D line = getStrip(sector - 1, supl, lay, strip - 1);

		if (line != null) {
			coords[0] = (float) (line.origin().x());
			coords[1] = (float) (line.origin().y());
			coords[2] = (float) (line.origin().z());
			coords[3] = (float) (line.end().x());
			coords[4] = (float) (line.end().y());
			coords[5] = (float) (line.end().z());

		}
	}

	/**
	 * Get the triplet quad coordinates for 3D view
	 * 
	 * @param sector
	 *            the 1-based layer dependent sector
	 * @param layer
	 *            the "big" layer 1..8
	 * @param coords
	 *            (dim = 26) will hold quads as [x1, y1, z1, ... x4, y4, z4] for
	 *            quad 1 (12 numbers) [x1, y1, z1, ... x4, y4, z4] for quad 2
	 *            (12 numbers) [x1, y1, z1, ... x4, y4, z4] for quad 3 (12
	 *            numbers)
	 */

	public static void getLayerQuads(int sector, int layer, float coords[]) {
		
		// geom service uses 0-based superlayer and layer
		int supl = ((layer - 1) / 2); // 0, 1, 2, 3 (ring)
		int lay = ((layer - 1) % 2); // 0, 1, 0, 1

		int numSect = BSTGeometry.sectorsPerSuperlayer[supl];

		//HACK fix mismatch reality vs. Geo database
		sector = GeometryManager.getInstance().svtSectorHack(numSect, sector);
		
		
		double vals[] = new double[10];

		BSTGeometry.getLimitValues(sector - 1, supl, lay, vals);
		// covert to cm
		for (int i = 0; i < 10; i++) {
			vals[i] /= 10;
		}

		float x1 = (float) vals[0];
		float y1 = (float) vals[1];
		float x2 = (float) vals[2];
		float y2 = (float) vals[3];

		float z1 = (float) vals[4];
		float z2 = (float) vals[5];
		float z3 = (float) vals[6];
		float z4 = (float) vals[7];
		float z5 = (float) vals[8];
		float z6 = (float) vals[9];

		fillCoords(0, coords, x1, y1, x2, y2, z1, z2);
		fillCoords(12, coords, x1, y1, x2, y2, z3, z4);
		fillCoords(24, coords, x1, y1, x2, y2, z5, z6);
	}

	private static void fillCoords(int index, float coords[], float x1, float y1, float x2, float y2, float zmin,
			float zmax) {

		coords[index++] = x1;
		coords[index++] = y1;
		coords[index++] = zmin;
		coords[index++] = x1;
		coords[index++] = y1;
		coords[index++] = zmax;
		coords[index++] = x2;
		coords[index++] = y2;
		coords[index++] = zmax;
		coords[index++] = x2;
		coords[index++] = y2;
		coords[index++] = zmin;

	}

	/**
	 * Get the points in the geometry service that were in the old file for
	 * drawing in the BST views
	 * 
	 * @param sector
	 *            the 0-based sector
	 * @param superlayer
	 *            the superlayer [0..3]
	 * @param layer
	 *            the layer [0,1]
	 * @param vals
	 *            holds for (10) numbers. All are in mm. The first four are x,
	 *            y, x, y for drawing the xy view. The last six are the six z
	 *            values that define (in z) the three active regions
	 */
	public static void getLimitValues(int sector, int superlayer, int layer, double vals[]) {
		Line3D line = getStrip(sector, superlayer, layer, 0);
		Point3D mp3d1 = line.origin();
		line = getStrip(sector, superlayer, layer, 255);
		Point3D mp3d2 = line.origin();
		// convert to mm
		vals[0] = mp3d1.x() * 10;
		vals[1] = mp3d1.y() * 10;
		vals[2] = mp3d2.x() * 10;
		vals[3] = mp3d2.y() * 10;

		// Point2D.Double wp0 = getStripMidpoint(sector, superlayer, layer, 0);
		// Point2D.Double wp1 = getStripMidpoint(sector, superlayer, layer,
		// 255);
		// vals[0] = wp0.x*10;
		// vals[1] = wp0.y*10;
		// vals[2] = wp1.x*10;
		// vals[3] = wp1.y*10;

		double z0 = Double.POSITIVE_INFINITY;
		double z5 = Double.NEGATIVE_INFINITY;

		for (int strip = 0; strip < 256; strip++) {
			line = getStrip(sector, superlayer, layer, strip);
			Point3D p0 = line.origin();
			Point3D p1 = line.end();
			z0 = Math.min(z0, p0.z());
			z0 = Math.min(z0, p1.z());
			z5 = Math.max(z5, p0.z());
			z5 = Math.max(z5, p1.z());
		}
		// put in dead zone by hand
		z0 = z0 * 10; // mm
		z5 = z5 * 10; // mm
		double del = (z5 - z0) / 3;
		double z1 = z0 + del - ZGAP / 2;
		double z2 = z1 + ZGAP;
		double z3 = z5 - del - ZGAP / 2;
		double z4 = z3 + ZGAP;

		vals[4] = z0;
		vals[5] = z1;
		vals[6] = z2;
		vals[7] = z3;
		vals[8] = z4;
		vals[9] = z5;

	}

	/**
	 * Get the XY coordinates of the midpoint of the line
	 * 
	 * @param sector
	 *            number of sectors are {10, 14, 18, 24} for superlayers {0, 1,
	 *            2, 3} respectively.
	 * @param superlayer
	 *            in the range [0..3]
	 * @param layer
	 *            either 0 or 1
	 * @param strip
	 *            should be in the range [0..255]
	 * @return the midpoint of the strip, with the z component dropped
	 */
	public static Point2D.Double getStripMidpoint(int sector, int superlayer, int layer, int strip) {

		if (strip > 256) {
			return null;
		}

		Line3D line = getStrip(sector, superlayer, layer, strip);
		Point3D mp3d = line.midpoint();
		return new Point2D.Double(mp3d.x(), mp3d.y());
	}

	public static void main(String arg[]) {
		initialize();

		// int superlayer = 3;
		// int sector = 2;
		// int layer = 0;
		//
		// for (int strip = 0; strip < 256; strip++) {
		// Line3D line = getStrip(sector, superlayer, layer, strip);
		//
		// Point3D mp3d1 = line.origin();
		// Point3D mp3d2 = line.end();
		//
		// String s =
		// String.format("%4d %4d %4d STRIP = %4d (%-11.5f %-11.5f %-11.5f)
		// (%-11.5f %-11.5f %-11.5f) strip length: %-11.5f",
		// superlayer+1, sector+1, layer+1, strip+1,
		// mp3d1.x()*10, mp3d1.y()*10, mp3d1.z()*10,
		// mp3d2.x()*10, mp3d2.y()*10, mp3d2.z()*10, line.length()*10);
		// System.err.println(s);
		// }

		// double vals[] = new double[10];
		// for (int supl = 3; supl < 4; supl++) {
		// // for (int supl = 0; supl < 4; supl++) {
		// // for (int lay = 0; lay < 1; lay++) {
		// for (int lay = 1; lay < 2; lay++) {
		// for (int sect = 0; sect < 1; sect++) {
		// // for (int sect = 0; sect < sectorsPerSuperlayer[supl];
		// // sect++) {
		// getLimitValues(sect, supl, lay, vals);
		//
		// String s = String
		// .format("%4d %4d %4d %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f
		// %-9.3f %-9.3f %-9.3f",
		// supl + 1, sect + 1, lay + 1, vals[0],
		// vals[1], vals[2], vals[3], vals[4],
		// vals[5], vals[6], vals[7], vals[8], vals[9]);
		// System.err.println(s);
		// }
		// }
		// }

		float coords[] = new float[6];
		for (int strip = 1; strip <= 256; strip++) {
			getStrip(1, 8, strip, coords);
			String s = String.format("strip %4d  coords: [%-9.3f  %-9.3f  %-9.3f]  [%-9.3f  %-9.3f  %-9.3f]  ", strip,
					coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
			System.err.println(s);
		}

		double vals[] = new double[10];
		getLimitValues(0, 3, 1, vals);

		System.out.println("done.");
		String s = String.format("%-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f %-9.3f", vals[0],
				vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8], vals[9]);
		System.err.println(s);
	}
}
