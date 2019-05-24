package cnuphys.swimtest;

import java.util.Random;

import cnuphys.swimZ.SwimZ;
import cnuphys.swimZ.SwimZException;
import cnuphys.swimZ.SwimZStateVector;

public class SmallDZTest {

	/**
	 * Compare the full machinery of SwimZ to the polynomial approximation
	 */

	public static void smallDZTest(long seed, int num, double delZMax) {

		Random rand = new Random(seed);

		int charge[] = new int[num];
		double pTot[] = new double[num];
		double theta[] = new double[num];
		double phi[] = new double[num];
		double x0[] = new double[num];
		double y0[] = new double[num];
		double z0[] = new double[num];
		double zTarg[] = new double[num];
		double stepSizeCM[] = new double[num];

		SwimZStateVector szV[] = new SwimZStateVector[num];

		SwimZ swimZ = new SwimZ();

		double hdata[] = new double[3];

		System.err.println("creating space...");
		for (int i = 0; i < num; i++) {
			charge[i] = ((rand.nextFloat() < 0.5f) ? -1 : 1);
			pTot[i] = SwimTest.randVal(1., 2., rand);
			theta[i] = SwimTest.randVal(5, 25, rand); // direction
			phi[i] = SwimTest.randVal(-10, 10, rand); // direction phi
			double rho = SwimTest.randVal(30, 70, rand);
			double philoc = Math.toRadians(SwimTest.randVal(-25, 25, rand)); // direction phi

			x0[i] = rho * Math.cos(philoc);
			y0[i] = rho * Math.sin(philoc);
			z0[i] = SwimTest.randVal(330, 420, rand) / 100;

//			zTarg[i] =  SwimTest.randVal(z0[i]-delZMax, z0[i]+delZMax, rand);
			zTarg[i] = SwimTest.randVal(z0[i], z0[i] + delZMax, rand);

			// swimZ uses CM
			szV[i] = new SwimZStateVector(x0[i], y0[i], z0[i], pTot[i], theta[i], phi[i]);

			stepSizeCM[i] = (zTarg[i] - szV[i].z) / delZMax;

		}

		SwimZStateVector stopSV = new SwimZStateVector();

		// prime the pump
		System.err.println("priming pump...");
		for (int i = 1; i < num / 2; i++) {
			try {
//				System.err.println("[" + (i+1) + "] Swim from z = " + szV[i].z + " to " + zTarg[i]);
//				
				int numStep = swimZ.adaptiveRK(charge[i], pTot[i], szV[i], stopSV, zTarg[i], stepSizeCM[i], hdata);
//				SwimTest.printSwimZCM(szTraj[i].last(), "Last for swimZ");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		long time;

		System.err.println("\nSwimZ");
		time = System.currentTimeMillis();

		for (int i = 0; i < num; i++) {
			try {
				int numStep = swimZ.adaptiveRK(charge[i], pTot[i], szV[i], stopSV, zTarg[i], stepSizeCM[i], hdata);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		SwimTest.printSwimZCM(stopSV, "Last for swimZ");

		System.err.println("\nSwimZ Parabolic");
		time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			try {
				swimZ.parabolicEstimate(charge[i], pTot[i], szV[i], stopSV, zTarg[i]);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		SwimTest.printSwimZCM(stopSV, "Last for swimZ Parabolic");

	}

	private static String timeString(long time, int num) {
		return String.format("%-7.4f ms ", (1. * time) / num);
	}

}
