package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ftof.FTOFDetector;
import org.jlab.geom.detector.ftof.FTOFFactory;
import org.jlab.geom.detector.ftof.FTOFLayer;
import org.jlab.geom.detector.ftof.FTOFSector;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

public class FTOFGeometry {

	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	
	//the overall detector
	private static FTOFDetector _ftofDetector;
	
	//first index -s sector 0..5
	//2nd index = panel type (superlayer) 0..2 for 1A, 1B, 2
	private static FTOFLayer[][] _ftofLayers = new FTOFLayer[6][3];

	public static int numPaddles[] = new int[3];

	// ftof panels (one sector stored--in sector cs--all assumed to be the same)
	private static FTOFPanel _ftofPanel[] = new FTOFPanel[3];
	private static String ftofNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };

	private static ConstantProvider tofDataProvider = 
			GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.FTOF);

	// only need sector 0
	private static FTOFSector _clas_sector0;

	/**
	 * Get the array of (3) forward time of flight panels.
	 * 
	 * @return the ftofPanel array
	 */
	public static FTOFPanel[] getFtofPanel() {
		return _ftofPanel;
	}
		
	/**
	 * Get the layer hits
	 * @param sect0 the 0-based sector 0..5
	 * @param ptype the panel type (0,1,2) = (1A, 1B, 2)
	 * @param path the 3D path
	 * @return the layer hits
	 */
	public static List<DetectorHit> getHits(int sect0, int ptype, Path3D path) {
		if (path == null) {
			return null;
		}
		return _ftofLayers[sect0][ptype].getHits(path);
	}

	/**
	 * Initialize the FTOF Geometry
	 */
	public static void initialize() {
		System.out.println("\n=====================================");
		System.out.println("===  FTOF Geometry Initialization ===");
		System.out.println("=====================================");

		_ftofDetector = (new FTOFFactory())
				.createDetectorCLAS(tofDataProvider);
		
		for (int sect = 0; sect < 6; sect++) {
			FTOFSector ftofSector = _ftofDetector.getSector(sect);
			for (int ptype = 0; ptype < 3; ptype++) {
				//only one layer (0)
				_ftofLayers[sect][ptype] = ftofSector.getSuperlayer(ptype).getLayer(0);
			}
		}
		
		_clas_sector0 = _ftofDetector.getSector(0);

		// here superlayers are panels 1a, 1b, 2
		for (int superLayer = 0; superLayer < 3; superLayer++) {

			// there is only a layer 0
			FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superLayer)
					.getLayer(0);
			
			
			numPaddles[superLayer] = ftofLayer.getNumComponents();

			_ftofPanel[superLayer] = new FTOFPanel(ftofNames[superLayer],
					numPaddles[superLayer]);
		}

	}

	/**
	 * 
	 * @param superlayer
	 *            PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId
	 *            the 1-based paddle id
	 */
	public static ScintillatorPaddle getPaddle(int superLayer, int paddleId) {
		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superLayer).getLayer(
				0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleId - 1);
		return paddle;
	}

	/**
	 * Used by the 3D drawing
	 * 
	 * @param sector
	 *            the 1-based sector
	 * @param superlayer
	 *            PANEL_1A, PANEL_1B or PANEL_12 (0, 1, 2)
	 * @param paddleId
	 *            the 1-based paddle ID
	 * @param coords
	 *            holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int sector, int superlayer, int paddleId,
			float[] coords) {

		Point3D v[] = new Point3D[8];

		ScintillatorPaddle paddle = getPaddle(superlayer, paddleId);
		for (int i = 0; i < 8; i++) {
			v[i] = new Point3D(paddle.getVolumePoint(i));
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
	 * Chech paddle intersection
	 * @param superlayer the 0 based superlayer for 1A, 1B, 2
	 * @param paddleid the 0 based paddle id
	 * @return if the projected polygon fully intersects the plane
	 */
	public static boolean doesProjectedPolyFullyIntersect(int superlayer, int paddleid, Plane3D projectionPlane) {

		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleid);
		boolean isects = false;

		try {
			isects = GeometryManager.doesProjectedPolyIntersect(paddle, projectionPlane, 6, 4);
		} catch (Exception e) {

			System.err.println("Exception in FTOFGeometry doesProjectedPolyFullyIntersect");
			System.err.println("panel: " + ftofNames[superlayer] + " paddleID: " + paddleid);
		}

		return isects;
	}

	/**
	 * Get the intersections with a constant phi plane. If the paddle does not
	 * intersect (happens as phi grows) return null;
	 * 
	 * @param superlayer
	 *            0, 1 or 2 for 1A, 1B, 2
	 * @param paddleid
	 *            the 0-based paddle id
	 * @param projectionPlane
	 *            the projection plane
	 */
	public static boolean getIntersections(int superlayer,
			int paddleid, Plane3D projectionPlane, Point2D.Double wp[]) {
		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(
				0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleid);
		return GeometryManager.getProjectedPolygon(paddle, projectionPlane, 6, 4, wp, null);
	}

	/**
	 * Get the length of a paddle in cm
	 * @param superlayer
	 *            0, 1 or 2 for 1A, 1B, 2
	 * @param paddleid
	 *            the 0-based paddle id
	 * @return the length of the paddle
	 */
	public static double getLength(int superlayer, int paddleId) {
		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleId);
		return paddle.getLength();
	}
	
	/**
	 * Get an array of all the lengths
	 * @param superlayer
	 *            0, 1 or 2 for 1A, 1B, 2
	 * @return an array of all the paddle lengths
	 */
	public static double[] getLengths(int superlayer) {
		double[] length = new double[numPaddles[superlayer]];
		for (int i = 0; i < length.length; i++) {
			length[i] = getLength(superlayer, i);
		}
		return length;
	}
	
	public static void main(String arg[]) {
		FTOFGeometry.initialize();

		int superLayer = PANEL_1B;
		int paddleId = 20;
		
		ScintillatorPaddle paddle = getPaddle(superLayer, paddleId);

		System.err.println("Num vertex points: " + paddle.getNumVolumePoints());
		for (int i = 0; i < 4; i++) {
			System.err.println(paddle.getVolumePoint(i));
		}
	}
}
