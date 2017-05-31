package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

public class HTCCGeometry {
	
	//place holder hardwired simple geometry from Valery
	private static final boolean _SIMPLEGEO = true;
	private static final double _SIMPLETHICKNESS = 5; //cm fake thickness
	private static final double _SIMPLESTARTANGLE = 5; //degrees
	private static final double _SIMPLEDELANGLE = 7.5; //degrees
	private static final double _SIMPLERADIUS = 150; //cm
	

	public static void initialize() {
		System.out.println("\n=====================================");
		System.out.println("===  HTCC Geometry Initialization ===");
		
		if (_SIMPLEGEO) {
			System.out.println("======== WARNING: SIMPLE GEOMETRY ====");
		}
		
		System.out.println("=====================================");
	}
	
	/**
	 * Get the world polygon for the mirror in simple geometry
	 * @param ring the ring 1..4
	 * @param half  the half 1..2
	 * @param localPhi [-30..30] degrees
	 * @param wp holds the local polygon
	 */
	public static void getSimpleWorldPoly(int ring, 
			int half, double localPhi, Point2D.Double wp[]) {
		
		
		if (_SIMPLEGEO &&
				(ring > 0) && (ring < 5) &&
				(half > 0) && (half < 3)) {
			
			double r1 = (half == 1) ? _SIMPLERADIUS : _SIMPLERADIUS + _SIMPLETHICKNESS;
			double r2 = r1 + _SIMPLETHICKNESS;
			double theta2 = _SIMPLESTARTANGLE + (ring*_SIMPLEDELANGLE);
			double theta1 = theta2 - _SIMPLEDELANGLE;
			
			double cp = Math.cos(Math.toRadians(localPhi));
			
			theta1 = Math.toRadians(theta1);
			theta2 = Math.toRadians(theta2);
			
			double c1 = Math.cos(theta1);
			double c2 = Math.cos(theta2);
			double s1 = Math.sin(theta1)/cp;
			double s2 = Math.sin(theta2)/cp;
			
			wp[0].setLocation(r1*c1, r1*s1);
			wp[1].setLocation(r2*c1, r2*s1);
			wp[2].setLocation(r2*c2, r2*s2);
			wp[3].setLocation(r1*c2, r1*s2);
		}
		else {
			for (int i = 0; i < 4; i++) {
				wp[i].setLocation(Double.NaN, Double.NaN);
			}
		}
	}

}
