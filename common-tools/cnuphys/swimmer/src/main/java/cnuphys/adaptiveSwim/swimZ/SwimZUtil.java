package cnuphys.adaptiveSwim.swimZ;

/**
 * Static utility functions
 * @author heddle
 *
 */
public class SwimZUtil {
	
	//small number test
	private static final double TINY = 1e-16;


	/**
	 * Stuff a SwimZ state vector from the usual 6D variables
	 * @param x The x coordinate of the vertex in meters
	 * @param y The y coordinate of the vertex in meters
	 * @param p The momentum in GeV/c
	 * @param theta The polar angle in degrees
	 * @param phi The azimuthal angle in degrees
	 * @param uSwimZ will hold the SwimZ state vector (x, y, tx = px/pz, ty = py/pz)
	 */
	public static void swimZToSwim(double x, double y, double p, double theta, double phi, double[] uSwimZ) {

		double thetaR = Math.toRadians(theta);
		double pz = p*Math.cos(thetaR);
		
		if (Math.abs(pz) < TINY) {
			stuffU(Double.NaN, Double.NaN, Double.NaN, Double.NaN, uSwimZ);
			return;
		}
		
		double phiR = Math.toRadians(phi);
		double pSinTheta = p*Math.sin(thetaR);
		double px = pSinTheta*Math.cos(phiR);
		double py = pSinTheta*Math.sin(phiR);
		
		stuffU(x, y, px/pz, py/pz, uSwimZ);
		
	}
	
	
	/**
	 * Stuff the state vector with the values
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param tx the x track slope px/pz
	 * @param ty the y track slope py/pz
	 * @param uSwimZ the state vector
	 */
	public static void stuffU(double x, double y, double tx, double ty, double[] uSwimZ) {
		uSwimZ[0] = x;
		uSwimZ[1] = y;
		uSwimZ[2] = tx;
		uSwimZ[3] = ty;
	}
}

