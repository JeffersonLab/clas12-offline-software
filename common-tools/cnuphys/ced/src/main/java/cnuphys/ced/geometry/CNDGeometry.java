package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.cnd.CNDDetector;
import org.jlab.geom.detector.cnd.CNDFactory;
import org.jlab.geom.detector.cnd.CNDLayer;
import org.jlab.geom.detector.cnd.CNDSector;
import org.jlab.geom.detector.cnd.CNDSuperlayer;
import org.jlab.geom.prim.Point3D;

/**
 * Central Neutron Detector
 * 
 * @author heddle
 *
 */
public class CNDGeometry {

	private static ConstantProvider cndDataProvider;

	private static CNDFactory cndFactory;

	private static CNDDetector cndDetector;

	// there is only one sector
	private static CNDSector cndSector;

	// there is only one superlayer
	private static CNDSuperlayer cndSuperlayer;

	// there are three superlayers
	private static CNDLayer[] cndLayers;

	// there are 48 paddles per layer
	private static ScintillatorPaddle paddles[][];

	/**
	 * Initialize the CND Geometry by loading all the wires
	 */
	public static void initialize() {

		System.out.println("\n=====================================");
		System.out.println("==== CND Geometry Initialization ====");
		System.out.println("=====================================");

		cndDataProvider = GeometryFactory.getConstants(org.jlab.detector.base.DetectorType.CND);

		cndFactory = new CNDFactory();
		cndDetector = cndFactory.createDetectorCLAS(cndDataProvider);

		// one sector, one superlayer
		cndSector = cndDetector.getSector(0);
		cndSuperlayer = cndSector.getSuperlayer(0);

		// three layers
		cndLayers = new CNDLayer[3];
		paddles = new ScintillatorPaddle[3][48];
		for (int i = 0; i < cndLayers.length; i++) {
			cndLayers[i] = cndSuperlayer.getLayer(i);
			for (int j = 0; j < 48; j++) {
				
				
				paddles[i][j] = cndLayers[i].getComponent(j);
				
				//rotate do to geomtry change
				paddles[i][j].rotateZ(Math.toRadians(7.5));
				
			}
		}

	}
	
	/**
	 * Converts the numbering from Gagik's database to real. This should not
	 * be necessary but yet it is.
	 * @param geo the geo triplets where sect=1, layer=1..3, component = 1..48
	 * @param real the real triplets where sector = 1..24, layer=1..3, component = 1..2
	 */
	public static void geoTripletToRealTriplet(int geo[], int real[]) {
		int gS = geo[0];  //should be 1
		int gL = geo[1];  //1..3
		int gC = geo[2];  //1.48
		
		int t = 1 + (gC % 48);
		int s = 1 + ((t-1) / 2);
		int c = (t % 2) == 0 ? 2 : 1;
		
		real[0] = s;
		real[1] = gL;
		real[2] = c;
	}
	
	/**
	 * Converts the numbering from  real to Gagik's database to real. This should not
	 * be necessary but yet it is.
	 * @param geo the geo triplets where sect=1, layer=1..3, component = 1..48
	 * @param real the real triplets where sector = 1..24, layer=1..3, component = 1..2
	 */
	public static void realTripletToGeoTriplet(int geo[], int real[]) {
		int s = real[0];  //1..24
		int l = real[1];  //1..3
		int c = real[2];  //1..2
		
		int u = 2*(s-1) + c;
		int gC = (u-1) % 48;
		if (gC == 0) {
			gC = 48;
		}
		
		geo[0] = 1;
		geo[1] = l;
		geo[2] = gC;
	}


	/**
	 * Get a scintillator paddle
	 * 
	 * @param layer
	 *            the layer [1..3]
	 * @param paddle
	 *            the paddles [1..48]
	 * @return the paddle
	 */
	public static ScintillatorPaddle getPaddle(int layer, int paddle) {
		if ((layer < 1) || (layer > 3) || (paddle < 1) || (paddle > 48)) {
			return null;
		}
		return paddles[layer - 1][paddle - 1];
	}

	/**
	 * Used by the 3D drawing
	 * 
	 * @param layer
	 *            the 1-based layer 1..3
	 * @param paddleId
	 *            the 1-based paddle 1..48
	 * @param coords
	 *            holds 8*3 = 24 values [x1, y1, z1, ..., x8, y8, z8]
	 */
	public static void paddleVertices(int layer, int paddleId, float[] coords) {

		Point3D v[] = new Point3D[8];

		ScintillatorPaddle paddle = getPaddle(layer, paddleId);
		for (int i = 0; i < 8; i++) {
			v[i] = new Point3D(paddle.getVolumePoint(i));
		}

		for (int i = 0; i < 8; i++) {
			int j = 3 * i;
			coords[j] = (float) v[i].x();
			coords[j + 1] = (float) v[i].y();
			coords[j + 2] = (float) v[i].z();
		}
	}

	/**
	 * Obtain the paddle xy corners for 2D view
	 * 
	 * @param layer
	 *            the layer 1..3
	 * @param paddleId
	 *            the paddle ID 1..48
	 * @param wp
	 *            the four XY corners (cm)
	 */
	public static void paddleXYCorners(int layer, int paddleId,
			Point2D.Double[] wp) {
		ScintillatorPaddle paddle = getPaddle(layer, paddleId);
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
	 * @param layer
	 *            the layer 1..3
	 * @param paddleId
	 *            the paddle ID 1..48
	 * @param corners
	 *            the eight XYZ corners (cm)
	 */
	public static void paddle3DCorners(int layer, int paddleId,
			Point3D corners[]) {
		ScintillatorPaddle paddle = getPaddle(layer, paddleId);
		if (paddle == null) {
			return;
		}

		for (int i = 0; i < 8; i++) {
			corners[i] = paddle.getVolumePoint(i);
		}

	}

	public static void main(String arg[]) {
		initialize();
		
		//test conversion geo to real and back
		
		int gS = 1;
		int geo[] = new int[3];
		int real[] = new int[3];
		int geo2[] = new int[3];
		for (int gL = 1; gL <= 3; gL++) {
			for (int gC = 1; gC <= 48; gC++) {
				geo[0] = gS;
				geo[1] = gL;
				geo[2] = gC;
				
				geoTripletToRealTriplet(geo, real);
				realTripletToGeoTriplet(geo2, real);
				
				System.out.println(String.format("geo: [%d, %d, %d] real: [%d, %d, %d] geo2: [%d, %d, %d]", geo[0], geo[1], geo[2], real[0], real[1], real[2], geo2[0], geo2[1], geo2[2] ));
				
				if ((geo[0] != geo2[0]) || (geo[1] != geo2[1]) || (geo[2] != geo2[2])) {
					System.out.println("BAD CONVERSION  ");
				}
			}
		}
		
		

//		System.out.println("num sectors: " + cndDetector.getNumSectors());
//		System.out.println("num supl: " + cndSector.getNumSuperlayers());
//		System.out.println("num lay: " + cndSuperlayer.getNumLayers());
//
//		double xmax = Double.NEGATIVE_INFINITY;
//		double ymax = Double.NEGATIVE_INFINITY;
//		double zmax = Double.NEGATIVE_INFINITY;
//		double zmin = Double.POSITIVE_INFINITY;
//
//		for (int layerId = 1; layerId <= 3; layerId++) {
//			System.out.println("layer: " + layerId + " has "
//					+ cndLayers[layerId - 1].getNumComponents() + " paddles");
//
//			for (int paddleId = 1; paddleId <= 48; paddleId++) {
//				ScintillatorPaddle paddle = getPaddle(layerId, paddleId);
//				for (int i = 0; i < 8; i++) {
//					Point3D p3d = new Point3D(paddle.getVolumePoint(i));
//
//					xmax = Math.max(xmax, p3d.x());
//					ymax = Math.max(ymax, p3d.y());
//					zmax = Math.max(zmax, p3d.z());
//					zmin = Math.min(zmin, p3d.z());
//				}
//			}
//		}
//
//		System.out.println("xmax: " + xmax);
//		System.out.println("ymax: " + ymax);
//		System.out.println("zmax: " + zmax);
//		System.out.println("zmin: " + zmin);
//
//		ScintillatorPaddle paddle = getPaddle(2, 12);
//		System.out.println("num edges: " + paddle.getNumVolumeEdges());
//
//		for (int i = 0; i < 8; i++) {
//			Point3D p3d = new Point3D(paddle.getVolumePoint(i));
//			System.out.println("Point [" + (i + 1) + "] " + p3d);
//		}
	}

}
