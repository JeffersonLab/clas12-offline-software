package cnuphys.swim;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.util.TerminalPlot;

/**
 * This class gives an example of swimming a Lund particle
 * 
 * @author heddle
 * 
 */
public class Example {

	// lets swim an electron. Start by getting its LundId
	private static final LundId electron = LundSupport.getElectron();

	// create a swimmer for the torus field
	private static Swimmer swimmer;
	// vertex position
	private static final double xo = 0.0;
	private static final double yo = 0.0;
	private static final double zo = 0.0;

	// initial angles in degrees
	private static final double theta = 30.0;
	private static final double phi = 0.0;

	// these will be used to create a DefaultStopper
	private static final double rmax = 6.0; // m
	private static final double maxPathLength = 8.5; // m

	// The momentum, if the KE = 1 GeV
	private static final double momentum = 5.0;

	// get some fit results
	private static final double hdata[] = new double[3];

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		MagneticFields.getInstance().initializeMagneticFields();
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		swimmer = new Swimmer();

//		 example1();
//		 example2();
//		 example3();
//		 example4();
		example5();
		example6();
//		example7();
//		example7x();
//		example8();
		example9();
//		example10();

		System.out.println("\nDONE.");
	}

	// example 1: Uniform stepsize
	private static void example1() {
		System.out.println("\n=== EXAMPLE 1 === [Uniform]");

		// step size in m
		double stepSize = 5e-3; // m

		// save about every 20th step
		double distanceBetweenSaves = 20 * stepSize;

		// swim the particle, and return the results in a SwimTrajectory object
		SwimTrajectory traj = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, rmax, maxPathLength,
				stepSize, distanceBetweenSaves);

		// how many steps did we save:
		printSummary("\nresult from uniform step stepsize method with storage", 20 * traj.size(), momentum,
				traj.lastElement(), null);

		// make a crude terminal plot
		terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [FIXED]");

	}

	// example2 uniform stepsize, with default stopper and listener
	private static void example2() {
		// same problem as example 1 but using a listener
		System.out.println("\n=== EXAMPLE 2 === [Uniform]");

		// step size in m
		double stepSize = 5e-3; // m

		DefaultListener listener = new DefaultListener();
		DefaultSwimStopper stopper = new DefaultSwimStopper(rmax);
		int nstep = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, stopper, listener,
				maxPathLength, stepSize);

		double[] lastY = listener.getLastStateVector();
		printSummary("\nresult from uniform step stepsize method with listener", nstep, momentum, lastY, null);
	}

	// example 3 adaptive stepsize, listener and default stopper
	private static void example3() {
		System.out.println("\n=== EXAMPLE 3 === [Adaptive]");

		// same problem using adaptive stepsize
		DefaultListener listener = new DefaultListener();
		DefaultSwimStopper stopper = new DefaultSwimStopper(rmax);

		// step size in m
		double stepSize = 5e-4; // m

		try {
			int nstep = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, stopper, listener,
					maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
			double[] lastY = listener.getLastStateVector();
			printSummary("\nresult from adaptive stepsize method with errvect", nstep, momentum, lastY, hdata);
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

	}

	// example 4 adaptive stepsize with storage and err vector
	private static void example4() {
		System.out.println("\n=== EXAMPLE 4 === [Adaptive]");

		// now try adaptive stepsize with storage
		DefaultSwimStopper stopper = new DefaultSwimStopper(rmax);
		// step size in m
		double stepSize = 5e-4; // m
		try {
			SwimTrajectory traj = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, stopper, 0,
					maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
			double[] lastY = traj.lastElement();
			printSummary("\nresult from adaptive stepsize method with storage and err vector", traj.size(), momentum,
					lastY, hdata);

			terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [ADAPTIVE]");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

	}

	// example 5 adaptive step and y scale
	private static void example5() {
		System.out.println("\n=== EXAMPLE 5 === [Adaptive]");
		DefaultListener listener = new DefaultListener();
		DefaultSwimStopper stopper = new DefaultSwimStopper(rmax);
		double eps = 1.0e-08;
		double stepSize = 5e-4; // m

		try {
			int nstep = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, stopper, listener,
					maxPathLength, stepSize, eps, hdata);

			double[] lastY = listener.getLastStateVector();
			printSummary("\n\n================\nresult from adaptive stepsize method and yscale", nstep, momentum,
					lastY, hdata);

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

	}

	// example 6
	private static void example6() {
		System.out.println("\n=== EXAMPLE 6 [Adaptive] ===");

		// again, with storage
		DefaultSwimStopper stopper = new DefaultSwimStopper(2.0);
		double stepSize = 5e-4; // m
		double eps = 1.0e-08;
		try {
			SwimTrajectory traj = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, stopper,
					maxPathLength, stepSize, eps, hdata);
			double[] lastY = traj.lastElement();
			printSummary("\nresult from adaptive stepsize method with storage and yscale", traj.size(), momentum, lastY,
					hdata);
			terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [ADAPTIVE]");

			// lets try getting integral |b x dl|

			traj.computeBDL(swimmer.getProbe());
//			terminalPlot(
//					traj,
//					"Pathlength (horizontal, m) vs. Int|Bxdl| (vertical, kg-m) [ADAPTIVE] ",
//					0);

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}
	}

	// example 7 adaptive fixed z stop
	private static void example7() {
		System.out.println("\n=== EXAMPLE 7 ===");
		System.out.println("[Adaptive] Fixed Z cutoff");
		double ztarget = 2.75; // where integration should stop
		double accuracy = 10e-6; // 10 microns
		double stepSize = 5e-4; // m
		try {
			SwimTrajectory traj = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, ztarget,
					accuracy, rmax, maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
			double lastY[] = traj.lastElement();
			printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + ztarget, traj.size(),
					momentum, lastY, hdata);
			terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [ADAPTIVE] {STOP at Z=275}");
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

	}

	private static void example8() {

		System.out.println("\n=== EXAMPLE 8 ===");
		System.out.println("[Uniform] Fixed Z cutoff");
		double ztarget = 2.75; // where integration should stop
		double accuracy = 10e-6; // 10 microns
		double stepSize = 5e-3; // m

		// save about every 20th step
		double distanceBetweenSaves = 20 * stepSize;

		SwimTrajectory traj = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, ztarget, accuracy,
				rmax, maxPathLength, stepSize, distanceBetweenSaves);
		double lastY[] = traj.lastElement();
		printSummary("\nresult from fixed stepsize method with storage and Z cutoff at " + ztarget, 20 * traj.size(),
				momentum, lastY, null);
		terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [UNIFORM] {STOP at Z=2.75}");

	}

	// low momentum test
	private static void example9() {
		System.out.println("\n=== EXAMPLE 9 ===");
		System.out.println("[Adaptive] Fixed Z cutoff LOW MOMENTUM");
		// _charge _x0 _y0 _z0 _pTot _theta _phi z accuracy _rMax,
		// _maxPathLength, stepSize
		// 1 0.3336477532980491 -0.04022817961833376 2.3746720000000003
		// 2.040380518389974E-8 170.99407849270204 173.86470849011025 2.292386
		// 2.0E-5 7.0 5.0 5.0E-4

		int charge = 1;
		double xo = 0.3336477532980491;
		double yo = 0.04022817961833376;
		double zo = 2.3746720000000003;

		double pTot = 2.040380518389974E-8;
		double theta = 170.99407849270204;
		double phi = 173.86470849011025;

		double zstop = 2.292386;
		double zacc = 2.0E-5;
		double rMax = 7;
		double maxPath = 3.0;
		double step = 5.0e-4;

		try {
			SwimTrajectory traj = swimmer.swim(charge, xo, yo, zo, pTot, theta, phi, zstop, zacc, rMax, maxPath, step,
					Swimmer.CLAS_Tolerance, hdata);
			double lastY[] = traj.lastElement();
			printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + zstop, traj.size(),
					momentum, lastY, hdata);
			terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [ADAPTIVE] {STOP at Z=275}");
		} catch (RungeKuttaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// example 7 adaptive fixed z stop
	private static void example10() {
		System.out.println("\n=== EXAMPLE 10 ===");
		System.out.println("[Adaptive] Fixed Z cutoff");
		double ztarget = 2.75; // where integration should stop
		double accuracy = 10e-6; // 10 microns
		double stepSize = 5e-4; // m

		int N = 10000;
		long start = System.nanoTime();
		for (int i = 0; i <= N; i++) {
			try {
				SwimTrajectory traj = swimmer.swim(electron.getCharge(), xo, yo, zo, momentum, theta, phi, ztarget,
						accuracy, rmax, maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
				double lastY[] = traj.lastElement();
				if (i == N) {
					printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + ztarget,
							traj.size(), momentum, lastY, hdata);
					terminalPlot(traj, "z (horizontal, cm) vs. x (vertical, cm) [ADAPTIVE] {STOP at Z=275}");
				}
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}
		double totTime = (System.nanoTime() - start) / 1.0e9;

		System.out.println("\nApprox run time: " + totTime);
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
				String.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f m\nP = [%7.4e, %7.4e, %7.4e] |P| =  %9.6e GeV/c",
						Q[0], Q[1], Q[2], R, P * Q[3], P * Q[4], P * Q[5], P));
		System.out.println("norm (should be 1): " + norm);
		System.out.println("--------------------------------------\n");
	}

	// print a crude terminal plot
	private static void terminalPlot(SwimTrajectory traj, String title) {
		// lets create a crude terminal plot
		double xx[] = new double[traj.size()];
		double zz[] = new double[traj.size()];
		int index = 0;
		for (double v[] : traj) {
			xx[index] = 100 * v[0];
			zz[index] = 100 * v[2];
			index++;
		}
		TerminalPlot.plot2D(80, 20, title, zz, xx);
	}

	// print a crude terminal plot
	private static void terminalPlot(SwimTrajectory traj, String title, int opt) {
		// lets create a crude terminal plot
		double ll[] = new double[traj.size()];
		double bb[] = new double[traj.size()];
		int index = 0;
		for (double v[] : traj) {
			ll[index] = v[6];
			bb[index] = v[7];
			index++;
		}
		System.out.println("\n");
		TerminalPlot.plot2D(80, 20, title, ll, bb);
	}

}
