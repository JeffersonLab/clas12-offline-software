package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ft.FTCALDetector;
import org.jlab.geom.detector.ft.FTCALFactory;
import org.jlab.geom.detector.ft.FTCALLayer;
import org.jlab.geom.detector.ft.FTCALSector;
import org.jlab.geom.detector.ft.FTCALSuperlayer;
import org.jlab.geom.prim.Point3D;

public class FTCALGeometry {

	private static ConstantProvider ftCalDataProvider;

	private static FTCALFactory ftCalFactory;

	private static FTCALDetector ftCalDetector;

	// there is only one sector
	private static FTCALSector ftCalSector;

	private static FTCALSuperlayer ftCalSuperlayer;

	// there is only one superlayer
	private static FTCALLayer ftCalLayer;

	// z offset is a shift in z (cm) to place the 3d view at the origin
	public static final float FTCAL_Z0 = 200f;

	// there are 332 paddles but the IDs are not 1..332 they are 1..475
	private static ScintillatorPaddle paddles[];
	private static short goodIds[];

	/**
	 * Initialize the FTCAL Geometry
	 */
	public static void initialize() {

		System.out.println("\n=====================================");
		System.out.println("=== FTCAL Geometry Initialization ===");
		System.out.println("=====================================");

		ftCalDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FTCAL);

		ftCalFactory = new FTCALFactory();
		ftCalDetector = ftCalFactory.createDetectorCLAS(ftCalDataProvider);

		// one sector and one superlayer
		ftCalSector = ftCalDetector.getSector(0);
		ftCalSuperlayer = ftCalSector.getSuperlayer(0);
		ftCalLayer = ftCalSuperlayer.getLayer(0);

		// get the components. Some entries will be null
		paddles = new ScintillatorPaddle[476];
		for (int i = 0; i < paddles.length; i++) {
			paddles[i] = null;
		}

        
		
		
		// there are 332 good ids, not sequential, first is 8, last is 475
		goodIds = new short[332];

		int count = 0;
		List<ScintillatorPaddle> padlist = ftCalLayer.getAllComponents();
		
		
		for (ScintillatorPaddle sp : padlist) {

			int id = sp.getComponentId();
			paddles[id] = sp;
			goodIds[count] = (short)id;

			// System.err.println("** PADDLE ID: " + id + "   good paddle: " +
			// (paddles[id] != null));

			count++;
		}

	}

	/**
	 * Get a scintillator paddle
	 * 
	 * @param componentId
	 *            the componentId
	 * @return the paddle, might be null
	 */
	public static ScintillatorPaddle getPaddle(int componentId) {
		// if ((paddle < 1) || (paddle > 332)) {
		// return null;
		// }
		return paddles[componentId];
	}

	/**
	 * Used by the 3D drawing
	 * 
	 * @param paddleId
	 *            the 1-based paddle 1..332
	 * @param coords
	 *            holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int paddleId, float[] coords) {

		Point3D v[] = new Point3D[8];

		ScintillatorPaddle paddle = getPaddle(paddleId);
		for (int i = 0; i < 8; i++) {
			v[i] = new Point3D(paddle.getVolumePoint(i));
		}

		for (int i = 0; i < 8; i++) {
			int j = 3 * i;
			coords[j] = (float) v[i].x();
			coords[j + 1] = (float) v[i].y();

			// note the offset!!!!
			coords[j + 2] = (float) v[i].z() - FTCAL_Z0;
		}
	}

	/**
	 * Obtain the paddle xy corners for 2D view
	 * 
	 * @param paddleId
	 *            the paddle ID 1..48
	 * @param wp
	 *            the four XY corners (cm)
	 */
	public static void paddleXYCorners(int paddleId, Point2D.Double[] wp) {
		ScintillatorPaddle paddle = getPaddle(paddleId);
		if (paddle == null) {
			return;
		}

		for (int i = 0; i < 4; i++) {
			Point3D p3d = new Point3D(paddle.getVolumePoint(i));
			wp[i].x = p3d.x();
			wp[i].y = p3d.y();
		}
	}

	/**
	 * Obtain the paddle 3D corners. Order: <br>
	 * 0: xmin, ymin, zmax <br>
	 * 1: xmax, ymin, zmax <br>
	 * 2: xmax, ymax, zmax <br>
	 * 3: xmin, ymax, zmax <br>
	 * 4: xmin, ymin, zmin <br>
	 * 5: xmax, ymin, zmin <br>
	 * 6: xmax, ymax, zmin <br>
	 * 7: xmin, ymax, zmin <br>
	 * 
	 * @param paddleId
	 *            the paddle ID 1..48
	 * @param corners
	 *            the eight XYZ corners (cm)
	 */
	public static void paddle3DCorners(int paddleId, Point3D corners[]) {
		ScintillatorPaddle paddle = getPaddle(paddleId);
		if (paddle == null) {
			return;
		}

		for (int i = 0; i < 8; i++) {
			corners[i] = paddle.getVolumePoint(i);

			// noye the offset
			double zoff = corners[i].z() - FTCAL_Z0;
			corners[i].setZ(zoff);
		}
	}

	/**
	 * Check whether the id is one of the good id
	 * 
	 * @param id
	 *            the 1-based id to check
	 * @return true if it is a good id
	 */
	public static boolean isGoodId(int id) {
		return paddles[id] != null;
	}

	/**
	 * Get all the good ids
	 * 
	 * @return all the good ids
	 */
	public static short[] getGoodIds() {
		return goodIds;
	}
	
	public static short getGoodId(int index) {
		return goodIds[index];
	}

	public static void main(String arg[]) {
		initialize();

		System.out.println("num sectors: " + ftCalDetector.getNumSectors());
		System.out.println("num supl: " + ftCalSector.getNumSuperlayers());
		System.out.println("num lay: " + ftCalSuperlayer.getNumLayers());
		System.out.println("num components: " + ftCalLayer.getNumComponents());

		double xmax = Double.NEGATIVE_INFINITY;
		double ymax = Double.NEGATIVE_INFINITY;
		double zmax = Double.NEGATIVE_INFINITY;
		double zmin = Double.POSITIVE_INFINITY;

		for (ScintillatorPaddle paddle : paddles) {
			if (paddle != null) {
				for (int i = 0; i < 8; i++) {
					Point3D p3d = new Point3D(paddle.getVolumePoint(i));

					xmax = Math.max(xmax, p3d.x());
					ymax = Math.max(ymax, p3d.y());
					zmax = Math.max(zmax, p3d.z());
					zmin = Math.min(zmin, p3d.z());
				}
			}
		}
		System.out.println("xmax: " + xmax);
		System.out.println("ymax: " + ymax);
		System.out.println("zmax: " + zmax);
		System.out.println("zmin: " + zmin);
		

	}

}
