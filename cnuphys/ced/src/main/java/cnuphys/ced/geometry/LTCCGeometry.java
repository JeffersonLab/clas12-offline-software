package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

public class LTCCGeometry {
	
	//place holder hardwired simple geometry from Valery
	private static final boolean _SIMPLEGEO = true;
	
	private static final double _thick = 8.0; //cm
	private static final double _ro = 670.0; //cm
	private static final double _tanAlpha = Math.tan(Math.toRadians(25.0));
	private static final double _thetao = 3.7; //degrees
	private static final double _thetaf = 34.7; //degrees
	private static double _xo;  //initial horizontal
	private static double _yo;  //initial vertical
	private static double _rf; //cm
	private static double _xf; //cm
	private static double _yf; //cm
	
	private static double _delX;
	private static double _delY;

	public static void initialize() {
		System.out.println("\n=====================================");
		System.out.println("===  LTCC Geometry Initialization ===");
		
		if (_SIMPLEGEO) {
			System.out.println("======== WARNING: SIMPLE GEOMETRY ====");
			
			_xo = _ro*Math.cos(Math.toRadians(_thetao));
			_yo = _ro*Math.sin(Math.toRadians(_thetao));
			
			double sintf = Math.sin(Math.toRadians(_thetaf));
			double costf = Math.cos(Math.toRadians(_thetaf));
			
			_rf = (_xo + _yo*_tanAlpha)/(sintf*_tanAlpha + costf);
			_xf = _rf*Math.cos(Math.toRadians(_thetaf));
			_yf = _rf*Math.sin(Math.toRadians(_thetaf));
			
			_delX = (_xf - _xo)/18.;
			_delY = (_yf - _yo)/18.;
			
		}
		
		System.out.println("=====================================");
	}
	
	
	/**
	 * Get the world polygon for the mirror in simple geometry
	 * @param ring the ring 1..18
	 * @param half  the half 1..2 (left/right)
	 * @param localPhi [-30..30] degrees
	 * @param wp holds the local polygon
	 */
	public static void getSimpleWorldPoly(int ring, 
			int half, double localPhi, Point2D.Double wp[]) {
		
		
		if (_SIMPLEGEO &&
				(ring > 0) && (ring < 19) &&
				(half > 0) && (half < 3)) {
			
			double x0 = _xo + (ring-1)*_delX;
			double y0 = _yo + (ring-1)*_delY;
			double x1 = x0 + _delX;
			double y1 = y0 + _delY;
			
		    double theta0 = Math.atan2(y0, x0);
		    double theta1 = Math.atan2(y1, x1);
		    
		    double tx0 = _thick*Math.cos(theta0);
		    double ty0 = _thick*Math.sin(theta0);
		    double tx1 = _thick*Math.cos(theta1);
		    double ty1 = _thick*Math.sin(theta1);
			
		    if (half == 2) {
		    	x0 += tx0;
		    	y0 += ty0;
		    	x1 += tx1;
		    	y1 += ty1;
		    }

		    double x2 = x1 + tx1;
		    double y2 = y1 + ty1;
		    double x3 = x0 + tx0;
		    double y3 = y0 + ty0;
			
			wp[0].setLocation(x0, y0);
			wp[1].setLocation(x1, y1);
			wp[2].setLocation(x2, y2);
			wp[3].setLocation(x3, y3);
		}
		else {
			for (int i = 0; i < 4; i++) {
				wp[i].setLocation(Double.NaN, Double.NaN);
			}
		}
	}
	
	
	public static void main(String arg[]) {
		LTCCGeometry.initialize();
	}

}
