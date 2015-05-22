package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ftof.FTOFDetector;
import org.jlab.geom.detector.ftof.FTOFFactory;
import org.jlab.geom.detector.ftof.FTOFLayer;
import org.jlab.geom.detector.ftof.FTOFSector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

import cnuphys.bCNU.log.Log;

public class FTOFGeometry {

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
	System.out.println("\n=======================================");
	System.out.println("====  FTOF Geometry Inititialization ====");
	System.out.println("=======================================");

	// to get intersection with midplane
	Transformation3D transform3D = GeometryManager.toConstantPhi(0);

	FTOFDetector ftofDetector = (new FTOFFactory())
		.createDetectorCLAS(tofDataProvider);
	_clas_sector0 = ftofDetector.getSector(0);

	for (int superLayer = 0; superLayer < 3; superLayer++) { // 3
								 // superlayers
								 // 1a, 1b, 2
	    FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superLayer)
		    .getLayer(0);
	    int numPaddle = ftofLayer.getNumComponents();
	    _ftofPanel[superLayer] = new FTOFPanel(ftofNames[superLayer],
		    numPaddle);
	}

	String message = "Got FTOF panels from common geometry clas-geometry package.";
	Log.getInstance().config(message);
	System.out.println(message);
    }

    /**
     * Get the intersections with a constant phi plane. If the paddle does not
     * intersect (happens as phi grows) return null;
     * 
     * @param superlayer
     *            0, 1 or 2 for 1A, 1B, 2
     * @param paddleid
     *            the 0-based paddle id
     * @param transform3D
     *            the transformation to the constant phi
     * @return the intersection points (z component will be 0).
     */
    public static Point2D.Double[] getIntersections(int superlayer,
	    int paddleid, Transformation3D transform3D) {
	FTOFLayer ftofLayer = _clas_sector0.getSuperlayer(superlayer).getLayer(
		0);
	ScintillatorPaddle paddle = ftofLayer.getComponent(paddleid);
	List<Line3D> lines = paddle.getVolumeCrossSection(transform3D);
	// perhaps no intersection

	if ((lines == null) || (lines.size() < 4)) {
	    return null;
	}

	Point3D[] pnts = new Point3D[4];
	for (int i = 0; i < 4; i++) {
	    pnts[i] = lines.get(i).end();
	}

	// note reordering
	Point2D.Double p2d[] = new Point2D.Double[4];

	p2d[0] = new Point2D.Double(pnts[2].x(), pnts[2].y());
	p2d[1] = new Point2D.Double(pnts[3].x(), pnts[3].y());
	p2d[2] = new Point2D.Double(pnts[0].x(), pnts[0].y());
	p2d[3] = new Point2D.Double(pnts[1].x(), pnts[1].y());
	return p2d;
    }
}
