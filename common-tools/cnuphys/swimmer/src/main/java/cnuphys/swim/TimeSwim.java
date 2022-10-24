package cnuphys.swim;

import java.util.Random;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.magfield.FieldProbe;
import cnuphys.rk4.IDerivative;

/**
 * 
 * @author heddle
 *
 */
public class TimeSwim {
	
	//are we debugging?
	private static final boolean DEBUG = true;

	/**
	 * Find the point of closest approach along the simultaneous swims of two
	 * particles. Uses time (not pathlength) as independent variable. Uses a uniform
	 * stepsize swimmer. ASSUMPTION: closest approach is the step before the
	 * distance between the particles starts getting bigger.
	 * 
	 * @param lund1    the Lund id of particle 1
	 * @param x1       the vertex x position of particle 1 (m)
	 * @param y1       the vertex y position of particle 1 (m)
	 * @param z1       the vertex z position of particle 1 (m)
	 * @param p1       the momentum of particle 1 (GeV)
	 * @param theta1   the polar angle of particle 1 (degrees)
	 * @param phi1     the azimuthal angle of particle 1 (degrees)
	 * @param lund2    the Lund id of particle 2
	 * @param x2       the vertex x position of particle 2 (m)
	 * @param y2       the vertex y position of particle 2 (m)
	 * @param z2       the vertex z position of particle 2 (m)
	 * @param p2       the momentum of particle 2 (GeV)
	 * @param theta2   the polar angle of particle 2 (degrees)
	 * @param phi2     the azimuthal angle of particle 2 (degrees)
	 * @param stepsize the uniform step size in meters
	 * @param u1       particle 1 state vector at closest point
	 * @param u2       particle 2 state vector at closest point
	 * @return the distance of closest approach in meters
	 */
	public static double closestApproach(int lund1, double x1, double y1, double z1, double p1, double theta1,
			double phi1, int lund2, double x2, double y2, double z2, double p2, double theta2, double phi2,
			double stepsize, double[] u1, double[] u2) {
		
		final FieldProbe probe = FieldProbe.factory();


		// get the lund ids
		LundId lundId1 = LundSupport.getInstance().get(lund1);
		LundId lundId2 = LundSupport.getInstance().get(lund2);

		double mass1 = lundId1.getMass();
		double mass2 = lundId2.getMass();
		
		int q1 = lundId1.getCharge();
		int q2 = lundId2.getCharge();
		
		double gamma1 = getGamma(lundId1, p1);
		double gamma2 = getGamma(lundId2, p2);
		
		double vel1 = Swimmer.C * p1 / (mass1 * gamma1);
		double vel2 = Swimmer.C * p2 / (mass2 * gamma2);


		if (DEBUG) {
			System.out.println("particle1 = " + lundId1.getName() + "    particle2 = " + lundId2.getName());
			System.out.println("mass1 = " + mass1 + "     mass2 = " + mass2);
			System.out.println("q1 = " + q1 + "     q2 = " + q2);
			System.out.println("p1 = " + p1 + "     p2 = " + p2);
			System.out.println("gamma1 = " + gamma1 + "     gamma2 = " + gamma2);
			System.out.println("vel1 = " + vel1 + "     vel2 = " + vel2);
		}

	
		return 0;
	}
	
	//get the energy in GeV from mass and momentum
	private static double getEnergy(LundId lid, double p) {
		double m = lid.getMass();
		return Math.sqrt(p*p + m*m);
	}
	
	//get the relativistic gamma from mass and momentum
	private static double getGamma(LundId lid, double p) {
		double m = lid.getMass();
		double e = getEnergy(lid, p);
		return e/m;
	}
	
	public static void main(String arg[]) {
		int lund1 = -211;
		int lund2 = 2212;
		
		Random rand = new Random();
		double x1 = 10*rand.nextDouble();
		double y1 = 10*rand.nextDouble();
		double z1 = 10*rand.nextDouble();
		double x2 = 10*rand.nextDouble();
		double y2 = 10*rand.nextDouble();
		double z2 = 10*rand.nextDouble();
		
		double p1 = 0.4*rand.nextDouble(); //GeV
		double p2 = 0.4*rand.nextDouble(); //GeV
		
		double theta1 = 180*rand.nextDouble();
		double theta2 = 180*rand.nextDouble();
		
		double phi1 = 360*rand.nextDouble();
		double phi2 = 360*rand.nextDouble();
		
		//state vectors
		double u1[] = new double[6];
		double u2[] = new double[6];
		
		//step size in m
		double stepSize = 1.0e-4;
		
		double doca = closestApproach(lund1, x1, y1, z1, p1, theta1, phi1,
				lund2, x2, y2, z2, p2, theta2, phi2, stepSize, u1, u2);
	}

}
