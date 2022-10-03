package cnuphys.swimtest;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import cnuphys.adaptiveSwim.test.CylinderTest;
import cnuphys.adaptiveSwim.test.InterpPlaneTest;
import cnuphys.adaptiveSwim.test.PlaneTest;
import cnuphys.adaptiveSwim.test.RhoTest;
import cnuphys.adaptiveSwim.test.SphereTest;
import cnuphys.adaptiveSwim.test.ZTest;
import cnuphys.lund.LundStyle;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFieldCanvas;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFieldInitializationException;
import cnuphys.magfield.MagneticFields;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;
import cnuphys.swimZ.SwimZStateVector;

public class SwimTest {


	// we only need one swimmer it will adapt to changing fields.
	private static Swimmer _swimmer;

	private static int _sector = 1;
	final static MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
			MagneticFieldCanvas.CSType.XZ);



	//cpu time
    public static long cpuTime(long threadID) {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        if (mxBean.isThreadCpuTimeSupported()) {
            try {
                return mxBean.getThreadCpuTime(threadID);
            } catch (UnsupportedOperationException e) {
                System.out.println(e.toString());
            }
        } else {
            System.out.println("Not supported");
        }
        return 0;
    }

	// create the test menu
	private static JMenu getTestMenu() {
		JMenu menu = new JMenu("Tests");

		final JMenuItem createTrajItem = new JMenuItem("Create Test Trajectories...");
		final JMenuItem testSectorItem = new JMenuItem("Test Sector Swim");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == createTrajItem) {
					CreateTestTrajectories.createTestTraj(3344632211L, 1000);
				} else if (e.getSource() == testSectorItem) {
					SectorTest.testSectorSwim(50000);
				} 

			}

		};

		createTrajItem.addActionListener(al);
		testSectorItem.addActionListener(al);

		menu.add(adaptiveTestMenu());

		menu.add(createTrajItem);
		menu.add(testSectorItem);

		return menu;
	}

	private static JMenu adaptiveTestMenu() {
		JMenu atmenu = new JMenu("Test AdaptiveSwim Package");

		final JMenuItem swimZItem = new JMenuItem("Fixed Z test");
		final JMenuItem rhoItem = new JMenuItem("Rho Test");
		final JMenuItem planeItem = new JMenuItem("Plane Test");
		final JMenuItem interpPlaneItem = new JMenuItem("Interpolate to Plane Test");

		final JMenuItem cylinderItem = new JMenuItem("Cylinder Test");
		final JMenuItem sphereItem = new JMenuItem("Sphere Test");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == swimZItem) {
					ZTest.swimZTest(10000, 73557799);
				}  else if (e.getSource() == cylinderItem) {
					CylinderTest.cylinderTest();
				} else if (e.getSource() == sphereItem) {
					SphereTest.sphereTest();
				} else if (e.getSource() == rhoItem) {
					RhoTest.rhoTest(10000, 73557799);
				} else if (e.getSource() == planeItem) {
					PlaneTest.planeTest();
				} else if (e.getSource() == interpPlaneItem) {
					InterpPlaneTest.interpPlaneTest();
				}
			}
		};

		addMenuItem(atmenu, al, rhoItem);
		addMenuItem(atmenu, al, planeItem);
		addMenuItem(atmenu, al, interpPlaneItem);
		addMenuItem(atmenu, al, swimZItem);
		addMenuItem(atmenu, al, cylinderItem);
		addMenuItem(atmenu, al, sphereItem);
		return atmenu;
	}

	private static void addMenuItem(JMenu menu, ActionListener al, JMenuItem mitem) {
		mitem.addActionListener(al);
		menu.add(mitem);
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
		double phi = Math.atan2(y[1], y[0]);
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



}
