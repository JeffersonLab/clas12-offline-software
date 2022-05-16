package cnuphys.swimtest;

import java.awt.BorderLayout;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.test.AdaptiveBeamlineSwimTest;
import cnuphys.adaptiveSwim.test.AdaptiveSectorSwimTest;
import cnuphys.adaptiveSwim.test.AdaptiveTests;
import cnuphys.adaptiveSwim.test.InitialValues;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.lund.LundSupport;
import cnuphys.lund.LundTrackDialog;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFieldCanvas;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFieldInitializationException;
import cnuphys.magfield.MagneticFields;

import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.splot.plot.UnicodeSupport;
import cnuphys.swim.BeamLineStopper;
import cnuphys.swim.DefaultSwimStopper;
import cnuphys.swim.NumStepStopper;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;
import cnuphys.swimZ.SwimZStateVector;
import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.geometry.Plane;

public class SwimTest {
	
	//which swimmer
	public enum SWIMMER {OLD, NEW};

	private static String _homeDir = System.getProperty("user.home");
	private static String _currentWorkingDirectory = System.getProperty("user.dir");

	// we only need one swimmer it will adapt to changing fields.
	private static Swimmer _swimmer;

	private static int _sector = 1;
	final static MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
			MagneticFieldCanvas.CSType.XZ);
//	final static MagneticFieldCanvas canvas2 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
//			MagneticFieldCanvas.CSType.YZ);

	private static final JMenuItem reconfigItem = new JMenuItem("Remove Solenoid and Torus Overlap");

	private static double[] adaptiveAbsError = { 1.0e-5, 1.0e-5, 1.0e-5, 1.0e-5 };

	private static double oldAccuracy = 1.0e-6; // z stopping accuracy in m
	private static double oldAdaptiveInitStepSize = 0.01; // m

	// initialize the magnetic field
	private static void initMagField() {
		// test specific load
		final MagneticFields mf = MagneticFields.getInstance();
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			// Symm_torus_r2501_phi16_z251_24Apr2018
//			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_08May2018.dat",
//					"Symm_solenoid_r601_phi1_z1201_2008.dat");
			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
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

	/**
	 * Print a memory report
	 * 
	 * @param message a message to add on
	 */
	public static void memoryReport(String message) {
		System.gc();
		System.gc();

		StringBuilder sb = new StringBuilder(1024);
		double total = (Runtime.getRuntime().totalMemory()) / 1048576.;
		double free = Runtime.getRuntime().freeMemory() / 1048576.;
		double used = total - free;
		sb.append("==== Memory Report =====\n");
		if (message != null) {
			sb.append(message + "\n");
		}
		sb.append("Total memory in JVM: " + String.format("%6.1f", total) + "MB\n");
		sb.append(" Free memory in JVM: " + String.format("%6.1f", free) + "MB\n");
		sb.append(" Used memory in JVM: " + String.format("%6.1f", used) + "MB\n");

		System.out.println(sb.toString());
	}

	// create the test menu
	private static JMenu getTestMenu() {
		JMenu menu = new JMenu("Tests");

		final JMenuItem createTrajItem = new JMenuItem("Create Test Trajectories...");
		final JMenuItem testSectorItem = new JMenuItem("Test Sector Swim");
		final JMenuItem threadItem = new JMenuItem("Thread Test");
		final JMenuItem oneVtwoItem = new JMenuItem("Swimmer vs. SwimZ Test");
		final JMenuItem polyItem = new JMenuItem("SwimZ vs. Poly Approx Test");

		final JMenuItem csvItem = new JMenuItem("Output to CSV");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == createTrajItem) {
					CreateTestTrajectories.createTestTraj(3344632211L, 1000);
				} else if (e.getSource() == testSectorItem) {
					SectorTest.testSectorSwim(50000);
				} else if (e.getSource() == threadItem) {
					ThreadTest.threadTest(100, 8);
				} else if (e.getSource() == polyItem) {
					SmallDZTest.smallDZTest(3344632211L, 10000, 100);
				} else if (e.getSource() == reconfigItem) {
					MagneticFields.getInstance().removeMapOverlap();
				} else if (e.getSource() == csvItem) {
					outToCSV();
				}

			}

		};

		threadItem.addActionListener(al);
		createTrajItem.addActionListener(al);
		oneVtwoItem.addActionListener(al);
		polyItem.addActionListener(al);
		testSectorItem.addActionListener(al);
		reconfigItem.addActionListener(al);
		csvItem.addActionListener(al);

		menu.add(adaptiveTestMenu());

		menu.add(createTrajItem);
		menu.add(oneVtwoItem);
		menu.add(polyItem);
		menu.add(testSectorItem);
		menu.add(reconfigItem);
		menu.add(threadItem);

		menu.add(csvItem);
		return menu;
	}

	private static JMenu adaptiveTestMenu() {
		JMenu atmenu = new JMenu("Test AdaptiveSwim Package");

		final JMenuItem planeInterpItem = new JMenuItem("Interpolate");
		final JMenuItem rhoItem = new JMenuItem("Rho Test");
		final JMenuItem beamLineItem = new JMenuItem("Beamline Test");
		final JMenuItem retraceItem = new JMenuItem("Retrace Test");
		final JMenuItem planeItem = new JMenuItem("Plane Test");
		final JMenuItem lineItem = new JMenuItem("Line Test");
		final JMenuItem zItem = new JMenuItem("Z Test");
		final JMenuItem cylinderItem = new JMenuItem("Cylinder Test");
		final JMenuItem sphereItem = new JMenuItem("Sphere Test");
		final JMenuItem noStopperItem = new JMenuItem("No Stopper Test");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == rhoItem) {
					AdaptiveTests.rhoTest();
				} else if (e.getSource() == planeInterpItem) {
					interpolateToPlaneTest(20, 33557799);
				} else if (e.getSource() == beamLineItem) {
					AdaptiveBeamlineSwimTest.beamLineTest();
				} else if (e.getSource() == retraceItem) {
					AdaptiveTests.retraceTest();
				} else if (e.getSource() == planeItem) {
					AdaptiveTests.planeTest();
				} else if (e.getSource() == lineItem) {
					AdaptiveTests.lineTest();
				} else if (e.getSource() == zItem) {
					AdaptiveSectorSwimTest.zTest();
				} else if (e.getSource() == cylinderItem) {
					AdaptiveTests.cylinderTest();
				} else if (e.getSource() == sphereItem) {
					AdaptiveTests.sphereTest();
				} else if (e.getSource() == noStopperItem) {
					AdaptiveTests.noStopperTest();
				}
			}
		};

		planeInterpItem.addActionListener(al);
		rhoItem.addActionListener(al);
		beamLineItem.addActionListener(al);
		retraceItem.addActionListener(al);
		planeItem.addActionListener(al);
		lineItem.addActionListener(al);
		zItem.addActionListener(al);
		cylinderItem.addActionListener(al);
		noStopperItem.addActionListener(al);

		atmenu.add(planeInterpItem);
		atmenu.add(rhoItem);
		atmenu.add(beamLineItem);
		atmenu.add(retraceItem);
		atmenu.add(planeItem);
		atmenu.add(lineItem);
		atmenu.add(zItem);
		atmenu.add(cylinderItem);
		atmenu.add(noStopperItem);

		return atmenu;
	}

	// for Nicholas at VaTech
	private static void outToCSV() {

		double rMax = 6;
		double sMax = 8;
		double stepSize = 1.0e-3; // m
		double distanceBetweenSaves = stepSize;

		double xo = 0;
		double yo = 0;
		double zo = 0;

		Swimmer swimmer = new Swimmer();
		MagneticFields mf = MagneticFields.getInstance();

		double torusScale = (mf.hasActiveTorus() ? mf.getTorus().getScaleFactor() : 0);
		double solenoidScale = (mf.hasActiveSolenoid() ? mf.getSolenoid().getScaleFactor() : 0);

		// CHANGE THESE
		LundId lid = LundSupport.getElectron();
		int id = LundSupport.getElectron().getId();
		double p = 2; // GeV/c
		double theta = 15; // degrees
		double phi = 10; // degrees;

		String fn = String.format("%s_%-2.0fGeV.csv", lid.getName(), p);
		fn = fn.replace("  ", "");
		fn = fn.replace(" ", "");
		String dir = System.getProperty("user.home");
		File file = new File(dir, fn);
		String path = file.getPath();

		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(path));

			String header = String.format("%d,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f", id, torusScale,
					solenoidScale, xo, yo, zo, p, theta, phi);
			stringLn(dos, header);

			SwimTrajectory traj = swimmer.swim(lid.getCharge(), xo, yo, zo, p, theta, phi, rMax, sMax, stepSize,
					distanceBetweenSaves);

			for (double u[] : traj) {
				String s = String.format("%f,%f,%f,%f,%f,%f", u[0], u[1], u[2], p * u[3], p * u[4], p * u[5]);
				stringLn(dos, s);
			}
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// for csv output
	private static void stringLn(DataOutputStream dos, String s) {

		s = s.replace("  ", "");
		s = s.replace(" ", "");
		s = s.replace(", ", ",");
		s = s.replace(", ", ",");
		s = s.replace(" ,", ",");

		try {
			dos.writeBytes(s);
			dos.writeBytes("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static JFrame createFrame() {
		final JFrame testFrame = new JFrame("Swim Test Frame");
		testFrame.setLayout(new BorderLayout(4, 4));

		final MagneticFields mf = MagneticFields.getInstance();

		final JLabel label = new JLabel(" Torus: " + MagneticFields.getInstance().getTorusPath());
		label.setFont(new Font("SandSerif", Font.PLAIN, 10));
		testFrame.add(label, BorderLayout.SOUTH);

		// drawing canvas
		JPanel magPanel1 = canvas1.getPanelWithStatus(1000, 465);
//		JPanel magPanel2 = canvas2.getPanelWithStatus(680, 460);
		canvas1.setExtraText("Field Magnitude (T)");
//		canvas2.setExtraText("Gradient Magnitude (T/m)");
//		canvas2.setShowGradient(true);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.out.println("Done");
				System.exit(1);
			}
		};

		MagneticFieldChangeListener mfcl = new MagneticFieldChangeListener() {

			@Override
			public void magneticFieldChanged() {
				label.setText(" Torus: " + MagneticFields.getInstance().getTorusPath());
				System.out.println("Field changed. Torus path: " + MagneticFields.getInstance().getTorusPath());
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
		mb.add(mf.getMagneticFieldMenu(true, false));
		mb.add(SwimMenu.getInstance());

		JMenu testMenu = getTestMenu();

		mb.add(testMenu);

		JPanel cpanel = new JPanel();
		cpanel.setLayout(new GridLayout(1, 1, 4, 4));

		cpanel.add(magPanel1);
//		cpanel.add(magPanel2);

		testFrame.add(cpanel, BorderLayout.CENTER);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		return testFrame;

	}

	// takes all the created SwimTrajectories
	private static void setMCanvasTrajectories() {
		canvas1.clearTrajectories();
//		canvas2.clearTrajectories();
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
//			canvas2.addTrajectory(x, y, z, ls.getLineColor(), ls.getStroke());
		}
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

	/**
	 * Swims a charged particle. This swims to a fixed z value. This is for the
	 * trajectory mode, where you want to cache steps along the path. Uses an
	 * adaptive stepsize algorithm.
	 * 
	 * @param charge       the charge: -1 for electron, 1 for proton, etc
	 * @param xo           the x vertex position in meters
	 * @param yo           the y vertex position in meters
	 * @param zo           the z vertex position in meters
	 * @param momentum     initial momentum in GeV/c
	 * @param theta        initial polar angle in degrees
	 * @param phi          initial azimuthal angle in degrees
	 * @param fixedZ       the fixed z value (meters) that terminates (or
	 *                     maxPathLength if reached first)
	 * @param accuracy     the accuracy of the fixed z termination, in meters
	 * @param sMax         Max path length in meters. This determines the max number
	 *                     of steps based on the step size. If a stopper is used,
	 *                     the integration might terminate before all the steps are
	 *                     taken. A reasonable value for CLAS is 8. meters
	 * @param stepSize     the initial step size in meters.
	 * @param relTolerance the error tolerance as fractional diffs. Note it is a
	 *                     vector, the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata        if not null, should be double[3]. Upon return, hdata[0]
	 *                     is the min stepsize used (m), hdata[1] is the average
	 *                     stepsize used (m), and hdata[2] is the max stepsize (m)
	 *                     used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	private SwimTrajectory traditionalSwimmer(int charge, double momentum, double theta, double phi, double ztarget,
			double accuracy, double maxPathLen, double stepSize, double[] tolerance, double[] hdata)
			throws RungeKuttaException {
		SwimTrajectory traj;
		traj = _swimmer.swim(charge, 0, 0, 0, momentum, theta, phi, ztarget, accuracy, maxPathLen, stepSize,
				Swimmer.CLAS_Tolerance, hdata);
		double finalStateVector[] = traj.lastElement();
		printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + ztarget, traj.size(),
				momentum, finalStateVector, hdata);

		return traj;
	}

	/**
	 * Print a vector to the standard output
	 * 
	 * @param v the double vector
	 * @param s an info string
	 */
	public static void printSwimZ(SwimZStateVector v, String s) {

		String out = String.format("%s [%-12.5f, %-12.5f, %-12.5f] [%-12.5f, %-12.5f]", s, v.x / 100., v.y / 100.,
				v.z / 100., v.tx, v.ty);
		System.out.println(out);
	}

	/**
	 * Print a vector to the standard output
	 * 
	 * @param v the double vector
	 * @param s an info string
	 */
	public static void printSwimZCM(SwimZStateVector v, String s) {

		String out = String.format("%s [%-12.5f, %-12.5f, %-12.5f] [%-12.5f, %-12.5f]", s, v.x, v.y, v.z, v.tx, v.ty);
		System.out.println(out);
	}

	/**
	 * Print a vector to the standard output
	 * 
	 * @param v the double vector
	 * @param s an info string
	 */
	public static void printVect(double v[], String s) {

		if (v.length == 8) {
			String out = String.format("%s [%-12.5f, %-12.5f, %-12.5f, %-12.5f, %-12.5f, %-12.5f, %-12.5f, %-12.5f]", s,
					v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
			System.out.println(out);
			return;
		}

		String out = String.format("%s [%-10.5f, %-10.5f, %-10.5f %-10.5f, %-10.5f, %-10.5f]", s, v[0], v[1], v[2],
				v[3], v[4], v[5]);
		System.out.println(out);
	}

	/**
	 * Computer a random double between two limits
	 * 
	 * @param min  the min value
	 * @param max  the max value
	 * @param rand the generator
	 * @return a random double between two limits
	 */
	public static double randVal(double min, double max, Random rand) {
		return min + (max - min) * rand.nextDouble();
	}

	/**
	 * Compute the distance between two vectors
	 * 
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return the euclidean distance between two vectors
	 */
	public static double locDiff(double v1[], double v2[]) {
		double dx = v2[0] - v1[0];
		double dy = v2[1] - v1[1];
		double dz = v2[2] - v1[2];
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Compute the distance between a vector and a SwimZStateVector
	 * 
	 * @param v1  the vector (in m)
	 * @param szv the SwimZStateVector (assumed in cm)
	 * @return the euclidean distance between two vectors in meters
	 */
	public static double locDiff(double v1[], SwimZStateVector szv) {
		double dx = szv.x / 100. - v1[0];
		double dy = szv.y / 100. - v1[1];
		double dz = szv.z / 100. - v1[2];
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public static void printSummary(String message, int nstep, double momentum, double y[], double hdata[]) {
		System.out.println(message);
		double R = Math.sqrt(y[0] * y[0] + y[1] * y[1] + y[2] * y[2]);
		double norm = Math.sqrt(y[3] * y[3] + y[4] * y[4] + y[5] * y[5]);
		double P = momentum * norm;

		System.out.println("Number of steps: " + nstep);

		if (hdata != null) {
			System.out.println("min stepsize: " + hdata[0]);
			System.out.println("avg stepsize: " + hdata[1]);
			System.out.println("max stepsize: " + hdata[2]);
		}
		System.out.println(
				String.format("R = [%9.7f, %9.7f, %9.7f] |R| = %9.7f m\nP = [%9.7e, %9.7e, %9.7e] |P| =  %9.7e GeV/c",
						y[0], y[1], y[2], R, P * y[3], P * y[4], P * y[5], P));

		// now in cylindrical
		double phi = FastMath.atan2(y[1], y[0]);
		double rho = FastMath.hypot(y[0], y[1]);
		System.out.println(String.format("[phi, rho, z] = [%9.6f, %9.6f, %9.6f]", Math.toDegrees(phi), rho, y[2]));

		System.out.println(String.format("norm (should be 1): %9.7f", norm));
		System.out.println("--------------------------------------\n");
	}

	public static void printSummary(String message, int nstep, double momentum, double theta0, SwimZStateVector sv,
			double hdata[]) {
		System.out.println(message);
		double R = Math.sqrt(sv.x * sv.x + sv.y * sv.y + sv.z * sv.z);

		double pz = momentum / Math.sqrt(1. + sv.tx * sv.tx + sv.ty * sv.ty);
		double px = sv.tx * pz;
		double py = sv.ty * pz;

		double norm = Math.sqrt(px * px + py * py + pz * pz) / momentum;
		double P = momentum * norm;

		System.out.println("Number of steps: " + nstep);

		if (hdata != null) {
			System.out.println("min stepsize: " + hdata[0]);
			System.out.println("avg stepsize: " + hdata[1]);
			System.out.println("max stepsize: " + hdata[2]);
		}
		System.out.println(
				String.format("R = [%9.7f, %9.7f, %9.7f] |R| = %9.7f m\nP = [%9.7e, %9.7e, %9.7e] |P| =  %9.7e GeV/c",
						sv.x / 100, sv.y / 100, sv.z / 100, R / 100, px, py, pz, P));
		System.out.println(String.format("norm (should be 1): %9.7f", norm));
		System.out.println("--------------------------------------\n");
	}

	

	private static void runLineTest() {
		System.out.println("Running line test");

		double px = -7.623109e-02;
		double py = 6.664333e-09;
		double pz = 1.998547e+00;

		double x0 = 0.892645;
		double y0 = 0;
		double z0 = 5.749990;

		double rMax = 7;

		double P = Math.sqrt(px * px + py * py + pz * pz);

		// for swimming backwards
		px = -px;
		py = -py;
		pz = -pz;
		double theta = FastMath.acos2Deg(pz / P);
		double phi = FastMath.atan2Deg(py, px);
		System.out.println(String.format("Swim backwards use P: %9.6f  theta: %9.6f  phi: %9.6f", P, theta, phi));

		// create a stopper
		DefaultSwimStopper stopper = new DefaultSwimStopper(rMax);

//		IStopper stopper = new IStopper() {
//
//			@Override
//			public boolean stopIntegration(double t, double[] y) {
//				return y[2] < 0;
//			}
//
//			@Override
//			public double getFinalT() {
//				return 0;
//			}
//
//			@Override
//			public void setFinalT(double finalT) {
//			}
//			
//		};
//		

		Swimmer swimmer = new Swimmer();
		// swimmer.swim(charge, xo, yo, zo, momentum, theta, phi, rmax, maxPathLength,
		// stepSize, distanceBetweenSaves)

	}

	
	private static void testSwimToBeamLine() {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		Random rand = new Random();
		
        Swimmer swimmer = new Swimmer();
        
        double xB = 0.01;
        double yB = 0.01;
        double distBetweenSaves = 0.0005;
        int charge = -1;
        double xo = 50;
        double yo = 50;
        double zo = 450;
        double pTot = 6;
        double theta = 15;
        double  phi = 0;
        double stepSize = 0.001;

        double maxS = 8;
        BeamLineStopper stopper = new BeamLineStopper(xB, yB);

        //had a hardcoded value of 0.0005 for distance between saves
        SwimTrajectory traj = swimmer.swim(charge, xo, yo, zo, pTot, theta, phi, stopper, maxS, stepSize,
        		distBetweenSaves);
        if(traj==null) {
            System.err.println("null swim trajectory");
        }
        else {
    		double finalStateVector[] = traj.lastElement();
    		printSummary("\nSwim to beam line ", traj.size(),
    				pTot, finalStateVector, null);

        }

		
	//	BeamLineStopper.SwimToBeamLine(charge, xo, yo, zo, pTot, theta, phi, maxS, stepSize, distBetweenSaves, xB, yB);

	}
	
	//swim to a cylinder
	private static void swimToCylinderTest(int n, long seed) {
		
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		
		System.err.println("Number of test swims: " + n);
		
		Swimmer swimmer = new Swimmer();
		
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		//every distance is in meters
		double accuracy = 1e-6; // meters
	    double stepSize = 5e-4; // 500 microns

		
	    RandomData data = new RandomData(n, seed, -0.01, 0.02, -0.01, 0.02, -0.01, 0.02,
	    		0.9, 6, 25, 20, 0, 360);
	    String resStr;
	    //the false means the whole trajectory is not saved
	    AdaptiveSwimResult result = new AdaptiveSwimResult(false);
	    
	    double delMin = Double.POSITIVE_INFINITY;
	    double delMax = Double.NEGATIVE_INFINITY;

	    
	    int numFail = 0;
		double eps = 1.0e-6;
	    double sMax = 8;
	    
		double[] centerLineP1 = {0.5, 0.3, -125}; //cm
		double[] centerLineP2 = {0.5, 0.3, 250};
		double radius = 168/100; //meters
		
		//to meters
		for (int i = 0; i < 3; i++) {
			centerLineP1[i] /= 100;
			centerLineP2[i] /= 100;
		}
		
		//the target cylinder
		Cylinder targetCylinder = new Cylinder(centerLineP1, centerLineP2, radius);

		
		long startTime = 0;
	    for (int i = 0; i < n; i++) {
	    	try {
	    		
	    		if (i == 99) {
	    			startTime = System.currentTimeMillis();
	    		}
	    			    		
				swimmer.swimCylinder(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						centerLineP1, centerLineP2, radius, accuracy, 
						sMax, stepSize, Swimmer.CLAS_Tolerance, result);	

				if (result.getStatus() != 0) {
					numFail++;
				}
				else {
					double u[] = result.getUf();
					double del = Math.abs(targetCylinder.distance(u[0], u[1], u[2]));
					delMin = Math.min(delMin, del);
					delMax = Math.max(delMax, del);
					
				}
								
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
	    	
	    }


	    System.err.println("OLD SWIMMER Number of failures: " + numFail);
	    System.err.println(String.format("%-8.3f sec", ((double)(System.currentTimeMillis() - startTime))/1000.));
	    resStr = String.format("minDel = %-10.6f microns  maxDel = %-10.6f microns ", delMin * 1.0e6, delMax * 1.0e6);
	    
	    System.err.println(resStr);
	    
	    System.err.println("Last swim: " + result);

	    
		
		startTime = 0;
	    for (int i = 0; i < n; i++) {
	    	try {
	    		
	    		if (i == 99) {
	    			startTime = System.currentTimeMillis();
	    		}
	    			    		
				adaptiveSwimmer.swimCylinder(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						targetCylinder, accuracy, sMax, stepSize, eps, result);	

				if (result.getStatus() != 0) {
					numFail++;
				}
				else {
					double u[] = result.getUf();
					double del = Math.abs(targetCylinder.distance(u[0], u[1], u[2]));
					delMin = Math.min(delMin, del);
					delMax = Math.max(delMax, del);
					
				}
								
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}
	    	
	    }
	    
	    

	    
	    System.err.println("NEW SWIMMER Number of failures: " + numFail);
	    System.err.println(String.format("%-8.3f sec", ((double)(System.currentTimeMillis() - startTime))/1000.));
	    resStr = String.format("minDel = %-10.6f microns  maxDel = %-10.6f microns ", delMin * 1.0e6, delMax * 1.0e6);
	    
	    System.err.println(resStr);
	    
	    System.err.println("Last swim: " + result);
	    
	    
	    System.err.println("\nDifference test");
	    AdaptiveSwimResult result2 = new AdaptiveSwimResult(false);
	    
	    double minDiff = Double.POSITIVE_INFINITY;
	    double maxDiff = Double.NEGATIVE_INFINITY;
	    double avgDiff = 0;
	    
	    int minIndex = -1;
	    int maxIndex = -1;
	    
	    for (int i = 0; i < n; i++) {
	    	try {
	    		
	    		if (i == 93449) {
	    			System.err.println();
	    		}
	    		
				swimmer.swimCylinder(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						centerLineP1, centerLineP2, radius, accuracy, 
						sMax, stepSize/10, Swimmer.CLAS_Tolerance, result);	
				
				adaptiveSwimmer.swimCylinder(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						targetCylinder, accuracy, sMax, stepSize, eps, result2);	
				
				double diff = result.delDifference(result2);
				avgDiff += diff;
				
				if (diff < minDiff) {
					minDiff = diff;
					minIndex = i;
				}
				
				if (diff > maxDiff) {
					maxDiff = diff;
					maxIndex = i;
				}

				
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
	    }
	    
	    avgDiff /= n;

	    System.err.println(String.format("Min Euclidean diff old and new swims: %-7.4f microns at index = %d", minDiff * 1.e6, minIndex));
	    System.err.println(String.format("Max Euclidean diff old and new swims: %-7.4f microns at index = %d", maxDiff * 1.e6, maxIndex));
	    System.err.println(String.format("Avg Euclidean diff old and new swims: %-7.4f microns", avgDiff * 1.e6));
	    
	    System.err.println("\nOld swimmer uniform step comparison.");

	    InitialValues iv = result.getInitialValues();
	    
		swimmer.swimCylinder(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, centerLineP1, centerLineP2, radius, accuracy, sMax, 0.01*stepSize, result);

	    
		System.err.println("\nDone with swimRho test");
	    System.err.println("Uniform step last swim: " + result);
	    
	    
//	    InitialValues iv = result.getInitialValues();
//	    double sf = result.getFinalS();
//	    System.err.println("\nUsing SwimS for comparison, old swimmer result, sf = " + sf);
//		try {
//			adaptiveSwimmer.swimS(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, accuracy, sf, stepSize, eps, result);
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//		System.err.println("Swim to old swimmer sf: " + result);
//
//	    
//	    sf = result2.getFinalS();
//	    System.err.println("\nUsing SwimS for comparison, new swimmer result, sf = " + sf);
//		try {
//			adaptiveSwimmer.swimS(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, accuracy, sf, stepSize, eps, result2);
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//		System.err.println("Swim to new swimmer sf: " + result2);

	}
	
	
	private static void gemcTrackTest() {
		System.out.println("GEMC Track Comparison");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);
		MagneticFields.getInstance().getSolenoid().setScaleFactor(-1);
		
		double gemcXF = 272.3931; //mm
		double gemcYF = 62.3494; //mm
		double gemcZF = 102.5624; //mm
		double gemcSF = 299.9000; //mm
		
	    double xo = 0;
	    double yo = 0;
	    double zo = 0;
	    		
		System.out.println("Old Swimmer, Uniform Step, Step = 0.1mm");
		Swimmer swimmer = new Swimmer();
		
	    double stepSize = 1e-4; // 0.1mm
	    double sMax = 5;  //m
	    int charge = 1;
	    double theta = 70;
	    double phi = 0;
	    double distanceBetweenSaves = stepSize;
	    double p = 1.; //GeV
		double accuracy = 1e-7; // meters
		double fixedZ = 102.5624/1000; //meters
		double hdata[] = new double[3];
		
	    //the false means the whole trajectory is not saved
	    AdaptiveSwimResult result = new AdaptiveSwimResult(false);


		
	    NumStepStopper stopper = new NumStepStopper(2999);
	    
		SwimTrajectory traj =  swimmer.swim(charge, xo, yo, zo, p, theta, phi,
				stopper, sMax, stepSize, distanceBetweenSaves);
		
		double finalStateVector[] = traj.lastElement();
		printSummary("\nresult from uniform stepsize method ", traj.size(),
				p, finalStateVector, null);
		
		System.out.println("Sfinal = " + 1000*stopper.getFinalT() + " mm");


		System.out.println("\nOld Swimmer, Adaptive Step, Step = 0.1mm");
		
		try {
			traj = swimmer.swim(charge, xo, yo, zo, p, theta, phi,
					fixedZ, accuracy, sMax, stepSize, Swimmer.CLAS_Tolerance, hdata);
			
			printSummary("\nresult from uniform stepsize method ", traj.size(),
					p, finalStateVector, hdata);
			
			System.out.println("Sfinal = " + 1000*stopper.getFinalT() + " mm");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}


	}
	
	
	//swim to a fixed rho
	private static void swimToRhoTest(int n, long seed) {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		
		System.err.println("Number of test swims: " + n);
		
		Swimmer swimmer = new Swimmer();
		
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		
		//every distance is in meters
		double accuracy = 1e-6; // meters
	    double stepSize = 5e-4; // 500 microns
		
	    System.err.println(String.format("accuracy: %-6.2f microns", accuracy*1.0e6));
	    
	    double rho = 0.2; //200 mm	    
	    double sMax = 5;
	    
	    RandomData data = new RandomData(n, seed);
	    
	    //the false means the whole trajectory is not saved
	    AdaptiveSwimResult result = new AdaptiveSwimResult(false);
	    
	    double delRhoMin = Double.POSITIVE_INFINITY;
	    double delRhoMax = Double.NEGATIVE_INFINITY;
	    
	    int numFail = 0;
	   
		double eps = 1.0e-6;
		
		long startTime = 0;


	    
	    for (int i = 0; i < n; i++) {
	    	try {
	    		
	    		if (i == 99) {
	    			startTime = System.currentTimeMillis();
	    		}

	    		
				swimmer.swimRho(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						rho, accuracy, sMax, stepSize, Swimmer.CLAS_Tolerance, result);
				double rhoFin = result.getFinalRho();

				if (result.getStatus() != 0) {
					numFail++;
					System.err.println("rho final: " + rhoFin + "   status: " + result.getStatus());
				}
								
				double del = Math.abs(rhoFin - rho);
				delRhoMin = Math.min(delRhoMin, del);
				delRhoMax = Math.max(delRhoMax, del);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
	    }
	    
	    
	    System.err.println("OLD SWIMMER  Number of failures: " + numFail);
	    System.err.println(String.format("%-8.3f sec", ((double)(System.currentTimeMillis() - startTime))/1000.));
	    String resStr = String.format("minDel = %-10.6f microns  maxDel = %-10.6f microns ", delRhoMin * 1.0e6, delRhoMax * 1.0e6);
	    
	    System.err.println(resStr);

	    System.err.println("Last swim: " + result);
	    
	    for (int i = 0; i < n; i++) {
	    	try {
	    		
	    		if (i == 99) {
	    			startTime = System.currentTimeMillis();
	    		}
	    		
				adaptiveSwimmer.swimRho(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						rho, accuracy, sMax, stepSize, eps, result);

				double rhoFin = result.getFinalRho();

				if (result.getStatus() != 0) {
					numFail++;
				}
								
				double del = Math.abs(rhoFin - rho);
				delRhoMin = Math.min(delRhoMin, del);
				delRhoMax = Math.max(delRhoMax, del);
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}
	    	
	    }
	    
	    

	    
	    System.err.println("NEW SWIMMER Number of failures: " + numFail);
	    System.err.println(String.format("%-8.3f sec", ((double)(System.currentTimeMillis() - startTime))/1000.));
	    resStr = String.format("minDel = %-10.6f microns  maxDel = %-10.6f microns ", delRhoMin * 1.0e6, delRhoMax * 1.0e6);
	    
	    System.err.println(resStr);
	    
	    System.err.println("Last swim: " + result);
	    
	    
	    
	    System.err.println("\nDifference test");
	    AdaptiveSwimResult result2 = new AdaptiveSwimResult(false);
	    
	    double minDiff = Double.POSITIVE_INFINITY;
	    double maxDiff = Double.NEGATIVE_INFINITY;
	    double avgDiff = 0;
	    
	    int minIndex = -1;
	    int maxIndex = -1;
	    
	    for (int i = 0; i < n; i++) {
	    	try {
	    		
				swimmer.swimRho(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						rho, accuracy, sMax, stepSize, Swimmer.CLAS_Tolerance, result);
				adaptiveSwimmer.swimRho(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						rho, accuracy, sMax, stepSize, eps, result2);
				double diff = result.delDifference(result2);
				avgDiff += diff;
				
				if (diff < minDiff) {
					minDiff = diff;
					minIndex = i;
				}
				
				if (diff > maxDiff) {
					maxDiff = diff;
					maxIndex = i;
				}

				
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
	    }
	    
	    avgDiff /= n;

	    System.err.println(String.format("Min Euclidean diff old and new swims: %-7.4f microns at index = %d", minDiff * 1.e6, minIndex));
	    System.err.println(String.format("Max Euclidean diff old and new swims: %-7.4f microns at index = %d", maxDiff * 1.e6, maxIndex));
	    System.err.println(String.format("Avg Euclidean diff old and new swims: %-7.4f", avgDiff * 1.e6));
	    
	    
//	    System.err.println("\nOld swimmer uniform step comparison.");
//
//	    InitialValues iv = result.getInitialValues();
//	    
//		swimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rho, accuracy, sMax, 0.01*stepSize, result);
//
//	    
//		System.err.println("\nDone with swimRho test");
//	    System.err.println("Uniform step last swim: " + result);
	    
	    
//	    InitialValues iv = result.getInitialValues();
//	    double sf = result.getFinalS();
//	    System.err.println("\nUsing SwimS for comparison, old swimmer result, sf = " + sf);
//		try {
//			adaptiveSwimmer.swimS(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, accuracy, sf, stepSize, eps, result);
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//		System.err.println("Swim to old swimmer sf: " + result);
//
//	    
//	    sf = result2.getFinalS();
//	    System.err.println("\nUsing SwimS for comparison, new swimmer result, sf = " + sf);
//		try {
//			adaptiveSwimmer.swimS(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, accuracy, sf, stepSize, eps, result2);
//		} catch (AdaptiveSwimException e) {
//			e.printStackTrace();
//		}
//		System.err.println("Swim to new swimmer sf: " + result2);

	   

	}
	
	//swim all or some of the random data
	private static void SwimRandomData(Swimmer swimmer, AdaptiveSwimmer newSwimmer, RandomData data, int nmax) {
		if (swimmer != null) {
			
			double rMax = 4;
			double sMax = 8;
			double stepSize = 1.0e-3; // m
			DefaultSwimStopper stopper = new DefaultSwimStopper(rMax);
			
			int len = Math.min(data.count, nmax);
			for (int i = 0; i < len; i++) {
				
//				public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
//						IStopper stopper, double sMax, double stepSize, double relTolerance[], double hdata[])			
				
				
				try {
					SwimTrajectory traj = swimmer.swim(data.charge[i], data.xo[i], 
							data.yo[i], data.zo[i], 
							data.p[i], data.theta[i], data.phi[i], stopper, sMax,
							stepSize, Swimmer.CLAS_Tolerance, null);
					
					Swimming.addMCTrajectory(traj);
				} catch (RungeKuttaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//interpolate to plane
	private static void interpolateToPlaneTest(int n, long seed) {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
	    System.err.println("Interpolate to plane test");
		
		Swimmer swimmer = new Swimmer();
		
		//generate some random data
//		public RandomData(int n, long seed,
//				double xmin, double dx,
//				double ymin, double dy,
//				double zmin, double dz,
//				double pmin, double dp,
//				double thetamin, double dtheta,
//				double phimin, double dphi) {
			
		RandomData data = new RandomData(n, seed);
		
		SwimRandomData(swimmer, null, data, 100);
	}
	
	//test the uniform step size swim to rho
	private static void uniformSwimToRhoTest(int n, long seed) {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
	    System.err.println("SwimToRho Uniform Stepsize Test");
		
		Swimmer swimmer = new Swimmer();
				
		//every distance is in meters
		double accuracy = 20e-6; // meters
	    double[] stepSize = {1e-5, 4e-5, 9.e-5, 2.5e-4, 3.5e-4, 5e-4}; // 500 microns
			    
	    double rho = 0.3; //200 mm	    
	    double sMax = 5;
	    
	    System.err.println(String.format("Target RHO: %-7.4f m", rho));
	    System.err.println(String.format("Desired Accuracy: %6.2f microns\n", 1.0e6*accuracy));
	    
	    RandomData data = new RandomData(n, seed);
	    
	    //the false means the whole trajectory is not saved
	    AdaptiveSwimResult result = new AdaptiveSwimResult(false);
	    
	    System.err.print("Q  xo (m)    yo (m)   zo (m)    p (GeV) " + UnicodeSupport.SMALL_THETA + " (deg)  "+ UnicodeSupport.SMALL_PHI+ " (deg) ");

	    for (double ss : stepSize) {
	    	System.err.print(String.format("%7.0f   ", 1.e6*ss));
	    }
	    
	    System.err.println();
	    
	    
	    
	    for (int i = 0; i < n; i++) {

			System.err.print(data.toStringRaw(i));

	    	for (int j = 0; j < stepSize.length; j++) {
				swimmer.swimRho(data.charge[i], data.xo[i], data.yo[i], data.zo[i], data.p[i], data.theta[i], data.phi[i], 
						rho, accuracy, sMax, stepSize[j], result);
				

				double del = 1.0e6*Math.abs(result.getFinalRho() - rho);
				System.err.print(String.format("   %7.2f", del));
	    	}
				
	    	System.err.println();

	    }
	    
	    System.err.println("\nFor last track, final locations for different step sizes:\n");
	    System.err.println("step (microns)   ");
	    
	    int nm1 = n - 1;
    	for (int j = 0; j < stepSize.length; j++) {
			swimmer.swimRho(data.charge[nm1], data.xo[nm1], data.yo[nm1], data.zo[nm1], data.p[nm1], data.theta[nm1], data.phi[nm1], 
					rho, accuracy, sMax, stepSize[j], result);
			String s = String.format("%7.0f  %s",
					1.e6*stepSize[j], result.finalLocationString());
			
			System.err.println(s);
			
    	}

	    
	    
	    

	   // System.err.println("\n\nLast swim: " + result);
	}

	/**
	 * main program
	 * 
	 * @param arg command line arguments (ignored)
	 */
	public static void Xmain(String arg[]) {
		final MagneticFields mf = MagneticFields.getInstance();
		FastMath.setMathLib(FastMath.MathLib.FAST);


		// test specific load
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
					"Symm_solenoid_r601_phi1_z1201_13June2018.dat");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//test the swim to a fixed rho
		//swimToRhoTest(10000, 33557799);
		
		//test the swim to a cylinder
		//swimToCylinderTest(93450, 33557799);
		
		//gemcTrackTest();
		
		//uniformSwimToRhoTest(20, 33557799);
		interpolateToPlaneTest(20, 33557799);


		// write out data file
//		String path = (new File(_homeDir, "swimTestData")).getPath();
//
//		generateTestData(path, 1000, 19923456L);
//		
//		
//		//look for the file
//		
//		
//		File file = new File(_currentWorkingDirectory, "swimTestData");
//		if (!file.exists()) {
//			file = new File(_homeDir, "swimTestData");
//			if (!file.exists()) {
//				System.err.println("FATAL ERROR Could not find test data file.");
//			}
//		}
//		
//		System.err.println("Will test on data file: [" + file.getPath() + "]");
//
//		testTestData(file.getPath());


		System.err.println("\ndone");
	}
	
	/**
	 * main program
	 * 
	 * @param arg command line arguments (ignored)
	 */
	public static void main(String arg[]) {

		final MagneticFields mf = MagneticFields.getInstance();
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
					"Symm_solenoid_r601_phi1_z1201_13June2018.dat");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());

		FastMath.setMathLib(FastMath.MathLib.SUPERFAST);
		// MagneticField.setMathLib(MagneticField.MathLib.DEFAULT);
		int numTest = 10000;

		JFrame testFrame = createFrame();

		SwimTrajectoryListener trajListener = new SwimTrajectoryListener() {

			@Override
			public void trajectoriesChanged() {
				ArrayList<SwimTrajectory> trajectories = Swimming.getMCTrajectories();
				System.out.println("Now have " + trajectories.size() + " trajectories");
				setMCanvasTrajectories();
			}

		};

		Swimming.addSwimTrajectoryListener(trajListener);

		_swimmer = new Swimmer();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
//				runLineTest();
			}
		});

	}
	

    class InterpPlaneResult {
    	
    	//state vectors: [x, y, z, px/p, py/p, pz/p]
    	
    	//last state vector on one side of plane
    	public double[] u0;
    	
    	//last state vector on other side of plane
    	public double[] u1;
    	
    	// which swimmer
    	public SWIMMER whichSwimmer;
    	
    	//the plane
    	public Plane plane;
    	
    }

}
