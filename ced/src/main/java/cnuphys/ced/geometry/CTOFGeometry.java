package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

public class CTOFGeometry {

	//fake geomety
	private static final double THETA0 = 180;
	private static final double DTHETA = 7.5; //360/48
	private static final double RINNER = 250.; //mm
	private static final double ROUTER = 260.; //mm
	
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
		_quads = new Point2D.Double[48][];
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

	
	
	public static void main(String arg[]) {
		initialize();
	}
}
