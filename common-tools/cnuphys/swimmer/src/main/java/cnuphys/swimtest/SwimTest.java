package cnuphys.swimtest;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import cnuphys.lund.LundStyle;
import cnuphys.lund.LundTrackDialog;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFieldCanvas;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFieldInitializationException;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;

public class SwimTest {
	
	private static int _sector = 1;
	final static MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(_sector, -10, -10, 680, 460.,
			MagneticFieldCanvas.CSType.XZ);
	final static MagneticFieldCanvas canvas2 = new MagneticFieldCanvas(_sector, -10, -10, 680, 460.,
			MagneticFieldCanvas.CSType.XZ);


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
	
	private static void initMagField() {
		// test specific load
		final MagneticFields mf = MagneticFields.getInstance();
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			// mf.initializeMagneticFields(mfdir.getPath(), "torus.dat",
			// "Symm_solenoid_r601_phi1_z1201_2008.dat");
			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_08May2018.dat",
					"Symm_solenoid_r601_phi1_z1201_2008.dat");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		MagneticFields.getInstance().setActiveField(FieldType.TORUS);

	}
	
	private static JMenu getTestMenu() {
		JMenu menu = new JMenu("Tests");
		
		return menu;
	}
	
	private static JFrame createFrame() {
		final JFrame testFrame = new JFrame("Swim Test Frame");
		testFrame.setLayout(new BorderLayout(4, 4));
		

		final MagneticFields mf = MagneticFields.getInstance();

		
		final JLabel label = new JLabel(" Torus: " + MagneticFields.getInstance().getTorusPath());
		label.setFont(new Font("SandSerif", Font.PLAIN, 10));
		testFrame.add(label, BorderLayout.SOUTH);


		// drawing canvas
		JPanel magPanel1 = canvas1.getPanelWithStatus(680, 460);
		JPanel magPanel2 = canvas2.getPanelWithStatus(680, 460);
		canvas2.setShowGradient(true);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		MagneticFieldChangeListener mfcl = new MagneticFieldChangeListener() {

			@Override
			public void magneticFieldChanged() {
				label.setText(" Torus: " + MagneticFields.getInstance().getTorusPath());
				System.err.println("Field changed. Torus path: " + MagneticFields.getInstance().getTorusPath());
//				Swimming.clearAllTrajectories();
//				canvas1.clearTrajectories();
//				Swimming.clearAllTrajectories();
//				canvas2.clearTrajectories();
			}
			
		};
		MagneticFields.getInstance().addMagneticFieldChangeListener(mfcl);
		
		
		// add the menu
		JMenuBar mb = new JMenuBar();
		testFrame.setJMenuBar(mb);
		mb.add(mf.getMagneticFieldMenu(true, true));
		mb.add(SwimMenu.getInstance());
		
		JMenu testMenu = getTestMenu();

		mb.add(testMenu);
		
		JPanel cpanel = new JPanel();
		cpanel.setLayout(new GridLayout(2, 1, 4, 4));
				
		cpanel.add(magPanel1);
		cpanel.add(magPanel2);
		
		testFrame.add(cpanel, BorderLayout.CENTER);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();
		
		return testFrame;

	}
	
	private static void createMCanvasTrajectories() {
		canvas1.clearTrajectories();
		canvas2.clearTrajectories();
		ArrayList<SwimTrajectory> trajectories = Swimming.getMCTrajectories();
		
		if ((trajectories == null) || (trajectories.size() < 1)) {
			return;
		}
		
		for (SwimTrajectory traj : trajectories) {
			double x[] = traj.getX();
			double y[] = traj.getY();
			double z[] = traj.getZ();
			
			LundStyle ls = LundStyle.getStyle(traj.getLundId());
			canvas1.addTrajectory(x, y, z, ls.getLineColor(), ls.getStroke());
			canvas2.addTrajectory(x, y, z, ls.getLineColor(), ls.getStroke());
		}
	}

	public static void main(String arg[]) {

		initMagField();

		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());

		FastMath.setMathLib(FastMath.MathLib.SUPERFAST);
		// MagneticField.setMathLib(MagneticField.MathLib.DEFAULT);
		int numTest = 10000;
		
		JFrame testFrame = createFrame();
		
		SwimTrajectoryListener trajListener = new SwimTrajectoryListener() {

			@Override
			public void trajectoriesChanged() {
				ArrayList<SwimTrajectory> trajectories = Swimming.getMCTrajectories();
				System.out.println("Now have " + trajectories.size() +  " trajectories");
				createMCanvasTrajectories();
			}
			
		};
		
		
		Swimming.addSwimTrajectoryListener(trajListener);
		
		LundTrackDialog.getInstance().setFixedZSelected(true);
		
		// testParabolicApproximation(numTest);
		// testOldUniform(numTest);
		// testOldAdaptive(numTest);
		// testAdaptiveEndpointOnly(numTest);
		// testCovMatProp(numTest);
		// testAdaptive(numTest);
		//testUniform(numTest);
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});		

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
