package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geometry.prim.Line3d;

public class CTOFGeometry {

	//fake geomety
//	private static final double THETA0 = -90 - 7.5;
	private static final double THETA0 = 0;
	private static final double DTHETA = -7.5; //360/48
//	private static final double RINNER = 250.; //mm
//	private static final double ROUTER = 260.; //mm
	public static final double RINNER = 251.1; //mm
	public static final double ROUTER = RINNER + 30.226; //mm
	
	public static final int COUNT = 48;
	
	//the quads for the xy view
	private static Point2D.Double _quads[][];

	public static void initialize() {
		System.out.println("\n=====================================");
		System.out.println("===  CTOF Geometry Initialization ===");
		System.out.println("===  Using Simple Geometery ===");
		System.out.println("=====================================");
		initQuads();
	}
	
	//init the quads
	private static void initQuads() {
		_quads = new Point2D.Double[COUNT][];
		for (int i = 0; i < COUNT; i++) {
			double theta1 = THETA0 - i*DTHETA;
			double theta2 = theta1 - DTHETA;
			
//			System.out.println("Theta1 " + theta1 + "  Theta2 " + theta2);
			
			theta1 = Math.toRadians(theta1);
			theta2 = Math.toRadians(theta2);
			
			_quads[i] = new Point2D.Double[4];
			_quads[i][0] = new Point2D.Double(RINNER*Math.cos(theta1), RINNER*Math.sin(theta1));
			_quads[i][1] = new Point2D.Double(ROUTER*Math.cos(theta1), ROUTER*Math.sin(theta1));
			_quads[i][2] = new Point2D.Double(ROUTER*Math.cos(theta2), ROUTER*Math.sin(theta2));
			_quads[i][3] = new Point2D.Double(RINNER*Math.cos(theta2), RINNER*Math.sin(theta2));
		}
	}
	
	/**
	 * Get the quad for a paddle
	 * @param paddle the 1-based paddle
	 * @return the quad for a paddle
	 */
	public static Point2D.Double[] getQuad(int paddle)  {
		return _quads[paddle-1];
	}

	public static void paddleVertices(int paddleId, float[] coords) {
		Point2D.Double quad[] = getQuad(paddleId);
		//convert mm to cm
		float x1 = (float) quad[0].x/10;
		float x2 = (float) quad[3].x/10;
		float x3 = (float) quad[2].x/10;
		float x4 = (float) quad[1].x/10;
		float y1 = (float) quad[0].y/10;
		float y2 = (float) quad[3].y/10;
		float y3 = (float) quad[2].y/10;
		float y4 = (float) quad[1].y/10;
		
		
		//approx
		float len = (float) (35.4*2.54); //cm
		float z1 = -len/2;
		float z2 =  len/2;
				
		setCoords(1, x1, y1, z1, coords);
		setCoords(2, x2, y2, z1, coords);
		setCoords(3, x3, y3, z1, coords);
		setCoords(4, x4, y4, z1, coords);
		setCoords(5, x1, y1, z2, coords);
		setCoords(6, x2, y2, z2, coords);
		setCoords(7, x3, y3, z2, coords);
		setCoords(8, x4, y4, z2, coords);
	}

	private static void setCoords(int corner, float x, float y, float z, float[] coords) {
		int i = (corner-1)*3;
		coords[i] = x;
		coords[i+1] = y;
		coords[i+2] = z;
	}
	
	public static void main(String arg[]) {
		initialize();
		realGeoTest();
	}
	
	private static void realGeoTest() {
		CTOFGeant4Factory factory = new CTOFGeant4Factory();
		for(int ipad=11;ipad<=48;ipad++){
			Geant4Basic pad = factory.getPaddle(ipad);
			
			System.err.println();
//			Line3d lineX = pad.getLineX();
//			System.err.println("X: " + lineX.toString());
//			Line3d lineY = pad.getLineY();
//			System.err.println("Y: " + lineY.toString());
			Line3d lineZ = pad.getLineZ();
			System.err.println("Z: " + lineZ.toString());
		}

	}
}
