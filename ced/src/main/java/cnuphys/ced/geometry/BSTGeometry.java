package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.detector.bst.BSTFactory;
import org.jlab.geom.detector.bst.BSTLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

public class BSTGeometry {

    /*
     * The BST Geometry is complicated. Currently from the geometry manager in
     * coatjava the get routines (e.g., getLine(sector, superlayer, layer) take
     * the three arguments <ul> <li>sector goes around a ring, the number of
     * sectors varies depending on the superlayer. The number of sectors are
     * {10, 14, 18, 24} for superlayers {0, 1, 2, 3} respectively. The first
     * sector is due north and the sector number increases in a clockwise
     * manner, <li> the four superlayers are like the rings, they are in the
     * range [0--3] <li> the layer is the inner and outer on each superlayer,
     * either 0 or 1. </ul>
     */

    // used for coordinate transformations
    private static DetectorTransformation _transform;

    private static ConstantProvider bstDataProvider = DataBaseLoader
	    .getConstantsBST();

    private static BSTFactory bstFactory = new BSTFactory();

    // the base layer that we will transform to get what we need
    private static BSTLayer _bstLayer0;
    private static BSTLayer _bstLayer1;

    // for putting in dead zone
    public static final double ZGAP = 1.67; // mm

    /** sectors per superlayer */
    public static final int[] sectorsPerSuperlayer = { 10, 14, 18, 24 };

    /**
     * Initialize the BST Geometry
     */
    public static void initialize() {

	System.out.println("\n=======================================");
	System.out.println("====  BST Geometry Inititialization [BSTGeometry] ====");
	System.out.println("=======================================");

	// create a ring layer
	_bstLayer0 = bstFactory.createRingLayer(bstDataProvider, 0, 0, 0);
	_bstLayer1 = bstFactory.createRingLayer(bstDataProvider, 0, 0, 1);

	// create a detector transform
	_transform = bstFactory.getDetectorTransform(bstDataProvider);
    }

    /**
     * Get the strip as a line
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
     * @return the strip as a line
     */
    public static Line3D getStrip(int sector, int superlayer, int layer,
	    int strip) {
	if (strip > 255) {
	    return null;
	}

	Line3D tempLine;
	if (layer == 0) {
	    tempLine = _bstLayer0.getComponent(strip).getLine();
	} else {
	    tempLine = _bstLayer1.getComponent(strip).getLine();
	}

	Transformation3D t3d = _transform.get(sector, superlayer, layer);
	Line3D line = new Line3D(tempLine);
	t3d.apply(line);
	return line;
    }

    /**
     * Get the points in the geometry service that were in the old file for
     * drawing in the BST views
     * 
     * @param sector
     *            the sector
     * @param superlayer
     *            the superlayer [0..3]
     * @param layer
     *            the layer [0,1]
     * @param vals
     *            holds for (10) numbers. All are in mm. The first four are x,
     *            y, x, y for drawing the xy view. The last siz are the six z
     *            values athat define (in z) the three active regions
     */
    public static void getLimitValues(int sector, int superlayer, int layer,
	    double vals[]) {
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
    public static Point2D.Double getStripMidpoint(int sector, int superlayer,
	    int layer, int strip) {

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
	// String.format("%4d %4d %4d STRIP = %4d  (%-11.5f  %-11.5f  %-11.5f)  (%-11.5f  %-11.5f  %-11.5f)   strip length: %-11.5f",
	// superlayer+1, sector+1, layer+1, strip+1,
	// mp3d1.x()*10, mp3d1.y()*10, mp3d1.z()*10,
	// mp3d2.x()*10, mp3d2.y()*10, mp3d2.z()*10, line.length()*10);
	// System.err.println(s);
	// }

	double vals[] = new double[10];
	for (int supl = 3; supl < 4; supl++) {
	    // for (int supl = 0; supl < 4; supl++) {
	    // for (int lay = 0; lay < 1; lay++) {
	    for (int lay = 1; lay < 2; lay++) {
		for (int sect = 0; sect < 1; sect++) {
		    // for (int sect = 0; sect < sectorsPerSuperlayer[supl];
		    // sect++) {
		    getLimitValues(sect, supl, lay, vals);

		    String s = String
			    .format("%4d %4d %4d  %-9.3f  %-9.3f  %-9.3f  %-9.3f  %-9.3f  %-9.3f  %-9.3f  %-9.3f %-9.3f  %-9.3f",
				    supl + 1, sect + 1, lay + 1, vals[0],
				    vals[1], vals[2], vals[3], vals[4],
				    vals[5], vals[6], vals[7], vals[8], vals[9]);
		    System.err.println(s);
		}
	    }
	}

	System.out.println("done.");
    }
}
