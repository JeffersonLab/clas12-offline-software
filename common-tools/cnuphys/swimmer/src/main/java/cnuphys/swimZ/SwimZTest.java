package cnuphys.swimZ;

import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

/**
 * A set of methods for testing the swim Z package
 * 
 * @author heddle
 *
 */
public class SwimZTest {

	static int Q = -1;
	static double xo = 30.0; // cm
	static double yo = 0.0;
	static double zo = 300; // cm
	static double zf = 500; // cm
	static double p = 1.0; // GeV
	static double theta = 30; // deg
	static double phi = 10; // deg
	static double uniformStepSize = .02; // cm
	static double parabolicStepSize = .01; // cm
	static double adaptiveInitStepSize = 0.01;

	static double[] adaptiveAbsError = { 1.0e-5, 1.0e-5, 1.0e-5, 1.0e-5 };

	static double oldAccuracy = 1.0e-6; // m
	static double oldUniformStepSize = 0.1; // m
	static double oldAdaptiveInitStepSize = 0.01; // m

	// create a old style swimmer for comparison
	private static Swimmer swimmer;

	public static void main(String arg[]) {

		MagneticFields.getInstance().initializeMagneticFields();
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
//		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
//		swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());

		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());

		FastMath.setMathLib(FastMath.MathLib.SUPERFAST);
		int numTest = 20000;
//		testParabolicApproximation(numTest);
//		testOldUniform(numTest);
//		testOldAdaptive(numTest);
//		testAdaptiveEndpointOnly(numTest);
//		testCovMatProp(numTest);
//		testAdaptive(numTest);
		testUniform(numTest);
	}

	private static void header(String s) {
		System.out.println("-------------------------------------");
		System.out.println("**  " + s);
		System.out.println("-------------------------------------");
	}

	private static void footer(String s) {
		System.out.print("---------[end ");
		System.out.print(s);
		System.out.println(" ]-------------\n");
	}

	// test the new SwimZ parabolic approximation
	private static void testParabolicApproximation(int numTimes) {

		header("SwimZ PARABOLIC APPROX");

		// the new swimmer
		SwimZStateVector start = new SwimZStateVector(xo, yo, zo, p, theta, phi);

		SwimZResult result = null;
		SwimZStateVector last = null;

		SwimZ sz = new SwimZ();
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numTimes; i++) {
			try {
				result = sz.parabolicEstimate(Q, p, start, zf, parabolicStepSize);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;
		partialReport(result, timePerSwim, "PARABOLIC APPROX");
		footer("SwimZ PARABOLIC APPROX");
	}

	// test the new SwimZ uniform integrator
	private static void testUniform(int numTimes) {
//
//		header("SwimZ UNIFORM");
//
//		// the new swimmer
//		SwimZStateVector start = new SwimZStateVector(xo, yo, zo, p, theta, phi);
//
//		SwimZResult result = null;
//		SwimZStateVector last = null;
//
//		SwimZ sz = new SwimZ();
//		long startTime = System.currentTimeMillis();
//		for (int i = 0; i < numTimes; i++) {
//			try {
//				result = sz.uniformRK4(Q, p, start, zf, uniformStepSize);
//			} catch (SwimZException e) {
//				e.printStackTrace();
//			}
//		}
//		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;
//		partialReport(result, timePerSwim, "Z UNIFORM");
//		footer("SwimZ UNIFORM");
	}

	private static void testAdaptive(int numTimes) {
		header("SwimZ ADAPTIVE");

		// the new swimmer
		SwimZStateVector start = new SwimZStateVector(xo, yo, zo, p, theta, phi);

		SwimZResult result = null;
		double hdata[] = new double[3];

		SwimZ sz = new SwimZ();
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numTimes; i++) {
			try {
				result = sz.adaptiveRK(Q, p, start, zf, adaptiveInitStepSize, hdata);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;
		partialReport(result, timePerSwim, "Z ADAPTIVE");
		hdataReport(hdata, 1);
		footer("SwimZ ADAPTIVE");
	}

	private static void testAdaptiveEndpointOnly(int numTimes) {
		header("SwimZ ADAPTIVE ENDPOINT ONLY");

		// the new swimmer
		SwimZStateVector start = new SwimZStateVector(xo, yo, zo, p, theta, phi);
		SwimZStateVector stop = new SwimZStateVector(xo, yo, zo, p, theta, phi);

		double hdata[] = new double[3];
		int numStep = 0;

		SwimZ sz = new SwimZ();
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numTimes; i++) {
			try {
				numStep = sz.adaptiveRK(Q, p, start, stop, zf, adaptiveInitStepSize, hdata);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;
		System.out.println("Number of steps: " + numStep);
		partialReport(start, stop, timePerSwim, "Z ADAPTIVE");
		hdataReport(hdata, 1);
		footer("SwimZ ADAPTIVE ENDPOINT ONLY");

	}

	// test the old swimmer adaptive
	private static void testOldAdaptive(int numTimes) {
		header("Old Swimmer ADAPTIVE");
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= numTimes; i++) {
			oldAdaptive(Q, xo / 100, yo / 100, zo / 100, zf / 100, p, theta, phi, i == numTimes);
		}
		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;
		System.out.println("Old adaptive swimmer time to swim: " + timePerSwim);
		footer("Old Swimmer ADAPTIVE");
	}

	// test the old swimmer uniform
	private static void testOldUniform(int numTimes) {
		header("Old Swimmer UNIFORM");
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= numTimes; i++) {
			oldUniform(Q, xo / 100, yo / 100, zo / 100, zf / 100, p, theta, phi, i == numTimes);
		}
		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;
		System.out.println("Old uniform swimmer time to swim: " + timePerSwim);
		footer("Old Swimmer UNIFORM");
	}

	private static void partialReport(SwimZResult result, double timePerSwim, String name) {
		double p3v[] = result.getFinalThreeMomentum();
		// check
		double pf = Math.sqrt(p3v[0] * p3v[0] + p3v[1] * p3v[1] + p3v[2] * p3v[2]);

		printVect(result.getInitialThreeMomentum(), "po");
		printVect(result.getFinalThreeMomentum(), "pf");
		System.out.println(name + " [" + result.size() + "]");
		System.out.println("Initial state vector: " + result.first());
		System.out.println("Final state vector: " + result.last());

		double thetaPhi[] = result.getInitialThetaAndPhi();
		System.out.println(String.format("Initial theta = %-8.2f phi = %-8.2f deg", thetaPhi[0], thetaPhi[1]));
		thetaPhi = result.getFinalThetaAndPhi();
		System.out.println(String.format("Final theta = %-8.2f phi = %-8.2f deg", thetaPhi[0], thetaPhi[1]));

		System.out.println("p: " + pf + " GeV/c" + " time/swim: " + timePerSwim + " ms");
	}

	private static void partialReport(SwimZStateVector start, SwimZStateVector stop, double timePerSwim, String name) {
//		double p3v[] = result.getFinalThreeMomentum();
//		// check
//		double pf = Math.sqrt(p3v[0] * p3v[0] + p3v[1] * p3v[1] + p3v[2] * p3v[2]);

//		printVect(result.getInitialThreeMomentum(), "po");
//		printVect(result.getFinalThreeMomentum(), "pf");
		System.out.println("Initial state vector: " + start);
		System.out.println("Final state vector: " + stop);

//		double thetaPhi[] = result.getInitialThetaAndPhi();
//		System.out.println(String.format("Initial theta = %-8.2f phi = %-8.2f deg", thetaPhi[0], thetaPhi[1]));
//		thetaPhi = result.getFinalThetaAndPhi();
//		System.out.println(String.format("Final theta = %-8.2f phi = %-8.2f deg", thetaPhi[0], thetaPhi[1]));

//		System.out.println("p: " + pf + " GeV/c" + " time/swim: " + timePerSwim + " ms");
		System.out.println("time/swim: " + timePerSwim + " ms");
	}

	private static void printVect(double v[], String s) {
		String out = String.format("%s [%-12.5f, %-12.5f, %-12.5f]", s, v[0], v[1], v[2]);
		System.out.println(out);
	}

	private static void printSummary(String message, int nstep, double momentum, double Q[], double hdata[]) {
		System.out.println(message);
		double R = Math.sqrt(Q[0] * Q[0] + Q[1] * Q[1] + Q[2] * Q[2]);
		double norm = Math.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
		double P = momentum * norm;

		System.out.println("Number of steps: " + nstep);

		if (hdata != null) {
			System.out.println("min stepsize: " + hdata[0]);
			System.out.println("avg stepsize: " + hdata[1]);
			System.out.println("max stepsize: " + hdata[2]);
		}
		System.out.println(
				String.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f cm\nP = [%7.4e, %7.4e, %7.4e] |P| =  %9.6e GeV/c",
						100 * Q[0], 100 * Q[1], 100 * Q[2], 100 * R, P * Q[3], P * Q[4], P * Q[5], P));
		System.out.println("norm (should be 1): " + norm);
		if (hdata != null) {
			hdataReport(hdata, 100.);
		}
		System.out.println("--------------------------------------\n");
	}

	/**
	 * calls the old swimmer with uniform step size
	 * 
	 * @param xo    in METERS
	 * @param yo    in METERS
	 * @param zo    in METERS
	 * @param zf    in METERS
	 * @param p     in GeV
	 * @param theta degrees
	 * @param phi   degrees
	 */
	private static void oldUniform(int Q, double xo, double yo, double zo, double zf, double p, double theta,
			double phi, boolean printSumm) {

		// save about every 20th step
		double distanceBetweenSaves = 20 * oldUniformStepSize;

		SwimTrajectory traj = swimmer.swim(Q, xo, yo, zo, p, theta, phi, zf, oldAccuracy, 10.0, 10.0,
				oldUniformStepSize, distanceBetweenSaves);

		if (printSumm) {
			double lastY[] = traj.lastElement();
			printSummary("\nOLD SWIMMER result from fixed stepsize method with storage and Z cutoff at " + zf,
					20 * traj.size(), p, lastY, null);
		}
	}

	/**
	 * Calls the old adaptive method
	 * 
	 * @param xo    in METERS
	 * @param yo    in METERS
	 * @param zo    in METERS
	 * @param zf    in METERS
	 * @param p     in GeV
	 * @param theta degrees
	 * @param phi   degrees
	 */
	private static void oldAdaptive(int Q, double xo, double yo, double zo, double zf, double p, double theta,
			double phi, boolean printSumm) {

		double hdata[] = new double[3];

		try {
			SwimTrajectory traj = swimmer.swim(Q, xo, yo, zo, p, theta, phi, zf, oldAccuracy, 10, 10,
					oldAdaptiveInitStepSize, Swimmer.CLAS_Tolerance, hdata);

			if (printSumm) {
				double lastY[] = traj.lastElement();
				printSummary("\nOLD SWIMMER result from fixed stepsize method with storage and Z cutoff at " + zf,
						20 * traj.size(), p, lastY, hdata);
			}

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

	}

	private static void hdataReport(double hdata[], double toCM) {
		String s = String.format("Min step: %-12.5e   Avg Step: %-12.5f   Max Step: %-12.5f cm", toCM * hdata[0],
				toCM * hdata[1], toCM * hdata[2]);
		System.out.println(s);
	}

}
