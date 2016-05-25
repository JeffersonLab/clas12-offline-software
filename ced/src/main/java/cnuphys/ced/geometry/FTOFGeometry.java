package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ftof.FTOFDetector;
import org.jlab.geom.detector.ftof.FTOFFactory;
import org.jlab.geom.detector.ftof.FTOFLayer;
import org.jlab.geom.detector.ftof.FTOFSector;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;

public class FTOFGeometry {

	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;

	public static int numPaddles[] = new int[3];

	// ftof panels (one sector stored--in sector cs--all assumed to be the same)
	private static FTOFPanel _ftofPanel[] = new FTOFPanel[3];
	private static String ftofNames[] = { "Panel 1A", "Panel 1B", "Panel 2" };

	private static ConstantProvider tofDataProvider = DataBaseLoader
			.getTimeOfFlightConstants();

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

	public static void initialize() {
		System.out.println("\n=====================================");
		System.out.println("===  FTOF Geometry Initialization ===");
		System.out.println("=====================================");

		FTOFDetector ftofDetector = (new FTOFFactory())
				.createDetectorCLAS(tofDataProvider);
		_clas_sector0 = ftofDetector.getSector(0);

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
	 * Get the intersections with a constant phi plane. If the paddle does not
	 * intersect (happens as phi grows) return null;
	 * 
	 * @param superlayer
	 *            0, 1 or 2 for 1A, 1B, 2
	 * @param paddleid
	 *            the 0-based paddle id
	 * @param projectionPlane
	 *            the projection plane
	 * @return the intersection points (z component will be 0).
	 */
	public static void getIntersections(int superlayer,
			int paddleid, Plane3D projectionPlane, Point2D.Double wp[]) {
		FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(
				0);
		ScintillatorPaddle paddle = ftofLayer.getComponent(paddleid);
		GeometryManager.getProjectedPolygon(paddle, projectionPlane, 6, 4, wp, null);
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
