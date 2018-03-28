package cnuphys.swim;

import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.swimZ.SwimZ;

public class SwimTest {

	static int Q = -1;
	static double xo = 30.0; // cm
	static double yo = 0.0;
	static double zo = 300; // cm
	static double zf = 500; // cm
	static double p = 5.0; // GeV
	static double theta = 30; // deg
	static double phi = 10; // deg
	static double uniformStepSize = .02; // cm
	static double adaptiveInitStepSize = 0.01;

	// these will be used to create a DefaultStopper
	private static final double rmax = 6.0; // m
	private static final double maxPathLength = 8.5; // m

	// get some fit results
	private static final double hdata[] = new double[3];

	static double[] adaptiveAbsError = { 1.0e-5, 1.0e-5, 1.0e-5, 1.0e-5 };

	static double oldAccuracy = 1.0e-6; // m
	static double oldUniformStepSize = 0.1; // m
	static double oldAdaptiveInitStepSize = 0.01; // m

	// create a old style swimmer for comparison
	private static Swimmer swimmer;

	public static void main(String arg[]) {

		MagneticFields.getInstance().initializeMagneticFields();
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		// swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());

		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());

		MagneticField.setMathLib(MagneticField.MathLib.FAST);
		// MagneticField.setMathLib(MagneticField.MathLib.DEFAULT);
		FieldProbe.cache(true);
		int numTest = 10000;
		// testParabolicApproximation(numTest);
		// testOldUniform(numTest);
		// testOldAdaptive(numTest);
		// testAdaptiveEndpointOnly(numTest);
		// testCovMatProp(numTest);
		// testAdaptive(numTest);
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

	// test the new SwimZ uniform integrator
	private static void testUniform(int numTimes) {

		header("Swim UNIFORM");

		double ztarget = 2.75; // where integration should stop
		double accuracy = 10e-6; // 10 microns
		double stepSize = 5e-3; // m

		// save about every 20th step
		double distanceBetweenSaves = 20 * stepSize;
		SwimTrajectory traj = null;
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numTimes; i++) {

			traj = swimmer.swim(-1, xo, yo, zo, p, theta, phi, ztarget, accuracy, rmax, maxPathLength, stepSize,
					distanceBetweenSaves);
		}
		double timePerSwim = ((double) (System.currentTimeMillis() - startTime)) / numTimes;

		printSummary("\nresult from uniform step stepsize method with storage", 20 * traj.size(), p, traj.lastElement(),
				null);

		System.out.println("time per swim: " + timePerSwim + "  ms");
		footer("Swim UNIFORM");
	}

	private static void testAdaptive(int numTimes) {
		// header("SwimZ ADAPTIVE");
		//
		// // the new swimmer
		// SwimZStateVector start = new SwimZStateVector(xo, yo, zo, p, theta,
		// phi);
		//
		// SwimZResult result = null;
		// double hdata[] = new double[3];
		//
		// SwimZ sz = new SwimZ(MagneticFields.getInstance().getActiveField());
		// long startTime = System.currentTimeMillis();
		// for (int i = 0; i < numTimes; i++) {
		// try {
		// result = sz.adaptiveRK(Q, p, start, zf, adaptiveInitStepSize,
		// adaptiveAbsError, hdata);
		// } catch (SwimZException e) {
		// e.printStackTrace();
		// }
		// }
		// double timePerSwim = ((double) (System.currentTimeMillis() -
		// startTime)) / numTimes;
		// partialReport(result, timePerSwim, "Z ADAPTIVE");
		// hdataReport(hdata, 1);
		// footer("SwimZ ADAPTIVE");
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

	private static void hdataReport(double hdata[], double toCM) {
		String s = String.format("Min step: %-12.5e   Avg Step: %-12.5f   Max Step: %-12.5f cm", toCM * hdata[0],
				toCM * hdata[1], toCM * hdata[2]);
		System.out.println(s);
	}
}
