package cnuphys.magfield;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.magfield.converter.Converter;

/**
 * Static testing of the magnetic field
 * 
 * @author heddle
 *
 */
public class MagTests {
	
	public static boolean TestMode;
	
	private static final JMenuItem reconfigItem = new JMenuItem("Remove Solenoid and Torus Overlap");
	private static int _sector = 1;
	final static MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
			MagneticFieldCanvas.CSType.XZ);


	private static String options[] = { "Random, with active field", " Along line, with active field",
			"Random, with active PROBE", " Along line, with active PROBE" };


	// test the test data file.
	public static void testTestData(String path) {

		File file = new File(path);

		if (!file.exists()) {
			System.err.println("FATAL ERROR did not find file [" + path + "]");
			System.exit(1);
		}

		TestData td = null;
		try {
			td = TestData.serialRead(path);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.err.println("Successfully deserialized TestData file.");
		System.err.println("Number of data points: " + td.count());

		String javaVersion = System.getProperty("java.version");

		boolean sameJava = td.javaVersion.contentEquals(javaVersion);

		System.err.println(String.format("Data java version: [%s] This java version: [%s] same: %s", td.javaVersion,
				javaVersion, sameJava));

		// using the same fields?
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		FieldProbe ifield = FieldProbe.factory();

		String torusFile = new String(MagneticFields.getInstance().getTorusBaseName());

		boolean sameTorus = td.torusFile.contentEquals(torusFile);
		System.err.println(
				String.format("Data Torus: [%s] This Torus: [%s] same: %s", td.torusFile, torusFile, sameTorus));

		String solenoidFile = new String(MagneticFields.getInstance().getSolenoidBaseName());

		boolean sameSolenoid = td.solenoidFile.contentEquals(solenoidFile);
		System.err.println(String.format("Data Solenoid: [%s] This Solenoid: [%s] same: %s", td.solenoidFile,
				solenoidFile, sameSolenoid));

		td.testResult = new float[td.count()][3];

		int nLoop = 10;

		System.err.println("Number of field values in test: " + td.count());
		System.err.println("Number of loops over the points: " + nLoop);
		System.err.println("Priming the pump.");

		for (int j = 0; j < nLoop; j++) {
			for (int i = 0; i < td.count(); i++) {
				ifield.field(td.x[i], td.y[i], td.z[i], td.testResult[i]);
			}
		}

		System.err.println("Timing start.");
		Instant start = Instant.now();
		for (int j = 0; j < nLoop; j++) {
			for (int i = 0; i < td.count(); i++) {
				ifield.field(td.x[i], td.y[i], td.z[i], td.testResult[i]);
			}
		}

		Instant end = Instant.now();
		long millis = Duration.between(start, end).toMillis();
		System.err.println("Calculation time: " + millis + "ms");

		// now look for differences

		int biggestAbsIndex = -1;
		double biggestAbsDiff = 0;

		for (int i = 0; i < td.count(); i++) {
			double delX = td.testResult[i][0] - td.result[i][0];
			double delY = td.testResult[i][1] - td.result[i][1];
			double delZ = td.testResult[i][2] - td.result[i][2];

			double absDiff = Math.sqrt(delX * delX + delY * delY + delZ * delZ);

			if (absDiff > biggestAbsDiff) {
				biggestAbsDiff = absDiff;
				biggestAbsIndex = i;
			}

		}

		if (biggestAbsIndex < 0) {
			System.err.println("The field values produced are exactly the same as the test data.\nDone.");
			System.exit(0);
		} else {

			System.err.println(String.format("Biggest absolute difference: %12.6f kG", biggestAbsDiff));
			System.err.println(String.format("At (x,y,z) = (%9.4f, %9.4f, %9.4f) cm", td.x[biggestAbsIndex],
					td.y[biggestAbsIndex], td.z[biggestAbsIndex]));
			System.err.println(String.format("Test Value (%12.6f, %12.6f, %12.6f) kG", td.result[biggestAbsIndex][0],
					td.result[biggestAbsIndex][1], td.result[biggestAbsIndex][2]));
			System.err
					.println(String.format("This Value (%12.6f, %12.6f, %12.6f) kG", td.testResult[biggestAbsIndex][0],
							td.testResult[biggestAbsIndex][1], td.testResult[biggestAbsIndex][2]));
		}

	}



	// test speeding up tited system calls
	private static void tiltSpeedupTest(int n, long seed) {
		System.err.println("\nTilted Sector Speedup Test");
		Random rand = new Random(seed);
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		//hold the results
		Comparison results[] = new Comparison[n];

		
		//get the test data
		for (int i = 0; i < n; i++) {
			results[i] = new Comparison();
			results[i].sect = 1 + rand.nextInt(6);
			results[i].x = 100 + 20 * rand.nextFloat();
			results[i].y = -40 + 20 * rand.nextFloat();
			results[i].z = 290 + 30 * rand.nextFloat();
		}
		
		RotatedCompositeProbe probe = (RotatedCompositeProbe) FieldProbe.factory();
		
		//prime pump
		for (int i = 0; i < 100; i++) {
			probe.field(results[i].sect, results[i].x, results[i].y, results[i].z, results[i].oldResult);
		}
		
		TestMode = true;
		for (int i = 0; i < 100; i++) {
			probe.field(results[i].sect, results[i].x, results[i].y, results[i].z, results[i].newResult);
		}
		
		TestMode = false;
		
		
		Instant start = Instant.now();
		for (int i = 0; i < n; i++) {
			probe.field(results[i].sect, results[i].x, results[i].y, results[i].z, results[i].oldResult);
		}
		Instant end = Instant.now();
		long oldmillis = Duration.between(start, end).toMillis();
		System.err.println("Old time: " + oldmillis + "ms");
		
		TestMode = true;
		
		start = Instant.now();
		for (int i = 0; i < n; i++) {
			probe.field(results[i].sect, results[i].x, results[i].y, results[i].z, results[i].newResult);
		}
		end = Instant.now();
		long newmillis = Duration.between(start, end).toMillis();
		System.err.println("New time: " + newmillis + "ms");
		System.err.println("percent diff: " + 100.*((oldmillis-newmillis)/((double)oldmillis)));
		
		float maxDiff = 0;
		for (int i = 0; i < n; i++) {
			maxDiff = Math.max(maxDiff, results[i].maxCompDiff());
//			System.err.println(results[i]);
		}
		System.err.println("Number of random points: " + n);
		System.err.println("Max Component Diff: " + maxDiff);

		System.err.println("Using same point over and over to test probe effect");
		TestMode = false;
		
		start = Instant.now();
		for (int i = 0; i < n; i++) {
			probe.field(2, results[0].x, results[0].y, results[0].z, results[0].oldResult);
		}
		end = Instant.now();
		oldmillis = Duration.between(start, end).toMillis();
		System.err.println("Old time: " + oldmillis + "ms");
		
		TestMode = true;
		
		start = Instant.now();
		for (int i = 0; i < n; i++) {
			probe.field(2, results[0].x, results[0].y, results[0].z, results[0].newResult);
		}
		end = Instant.now();
		newmillis = Duration.between(start, end).toMillis();
		System.err.println("New time: " + newmillis + "ms");
		System.err.println("percent diff: " + 100.*((oldmillis-newmillis)/((double)oldmillis)));
		
	}

	// timing tests
	private static void timingTest(int option) {
		System.out.println("Timing tests: [" + options[option] + "]");
		long seed = 5347632765L;

		int num = 10000000;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		float result[] = new float[3];

		IField ifield = FieldProbe.factory();

		Random rand = new Random(seed);

		if (option == 0) {
			for (int i = 0; i < num; i++) {
				z[i] = 600 * rand.nextFloat();
				float rho = 600 * rand.nextFloat();
				double phi = Math.toRadians(30 * rand.nextFloat());

				x[i] = (float) (rho * FastMath.cos(phi));
				y[i] = (float) (rho * FastMath.sin(phi));
			}
		} else if (option == 1) {
			double dT = 1. / (num - 1);
			for (int i = 0; i < num; i++) {
				double t = i * dT;
				x[i] = (float) (85. * t);
				y[i] = (float) (15. * t);
				z[i] = (float) (372. * t);
			}
		}

		// prime the pump
		for (int i = 0; i < num; i++) {
			ifield.field(x[i], y[i], z[i], result);
		}

		double sum = 0;
		for (int outer = 0; outer < 5; outer++) {
			long time = System.currentTimeMillis();

			for (int i = 0; i < num; i++) {
				ifield.field(x[i], y[i], z[i], result);
			}

			double del = (System.currentTimeMillis() - time) / 1000.;
			sum += del;

			System.out.println("loop " + (outer + 1) + " time  = " + del + " sec");

		}
		System.out.println("avg " + (sum / 5.) + " sec");

	}

	public static JMenu getTestMenu() {

		JMenu testMenu = new JMenu("Tests");

		final JMenuItem tiltItem = new JMenuItem("Tilted System Speedup Test");
		final JMenuItem test0Item = new JMenuItem("Timing Test Random Points");
		final JMenuItem test1Item = new JMenuItem("Timing Test Along a Line");


		final JMenuItem solenoidConvertItem = new JMenuItem("Convert in-memory solenoid to GEMC Format");
		final JMenuItem torusConvertItem = new JMenuItem("Convert in-memory torus to GEMC Format");

		ActionListener al1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == tiltItem) {
					tiltSpeedupTest(5000000, 97746291);
				} else if (e.getSource() == test0Item) {
					timingTest(0);
				} else if (e.getSource() == test1Item) {
					timingTest(1);
				} else if (e.getSource() == solenoidConvertItem) {
					Converter.memoryToGEMCSolenoidConverter();
				} else if (e.getSource() == torusConvertItem) {
					Converter.memoryToGEMCTorusConverter();
				}

			}

		};

		tiltItem.addActionListener(al1);
		test0Item.addActionListener(al1);
		test1Item.addActionListener(al1);
		solenoidConvertItem.addActionListener(al1);
		torusConvertItem.addActionListener(al1);

		testMenu.add(tiltItem);
		testMenu.addSeparator();
		testMenu.add(test0Item);
		testMenu.add(test1Item);

		testMenu.addSeparator();

		testMenu.add(solenoidConvertItem);
		testMenu.add(torusConvertItem);

		

		return testMenu;
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

		System.err.println(sb.toString());
	}

	private static void fixMenus() {
		boolean hasSolenoid = MagneticFields.getInstance().hasActiveSolenoid();
		boolean hasTorus = MagneticFields.getInstance().hasActiveTorus();
		reconfigItem.setEnabled(hasSolenoid && hasTorus);
	}

	// set up the frame to run the tests
	public static void runTests() {
		final JFrame testFrame = new JFrame("Magnetic Field");
		testFrame.setLayout(new BorderLayout(4, 4));

		final MagneticFields mf = MagneticFields.getInstance();

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

		final JLabel label = new JLabel(" Torus: " + MagneticFields.getInstance().getTorusPath());
		label.setFont(new Font("SandSerif", Font.PLAIN, 10));
		testFrame.add(label, BorderLayout.SOUTH);

		// drawing canvas
		JPanel magPanel1 = canvas1.getPanelWithStatus(1000, 465);

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
				fixMenus();
				MagneticFields.getInstance().printCurrentConfiguration(System.err);
			}

		};
		MagneticFields.getInstance().addMagneticFieldChangeListener(mfcl);

		// add the menu
		JMenuBar mb = new JMenuBar();
		testFrame.setJMenuBar(mb);
		mb.add(mf.getMagneticFieldMenu(true, true));

		mb.add(sectorMenu());

		JMenu testMenu = MagTests.getTestMenu();

		mb.add(testMenu);

		JPanel cpanel = new JPanel();
		cpanel.setLayout(new GridLayout(1, 1, 4, 4));

		cpanel.add(magPanel1);

		testFrame.add(cpanel, BorderLayout.CENTER);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}

	// the menu for changing sectors
	private static JMenu sectorMenu() {
		JMenu sectorMenu = new JMenu("Sector");
		final JRadioButtonMenuItem sectorButton[] = new JRadioButtonMenuItem[6];
		ButtonGroup bg = new ButtonGroup();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int sector = 1; sector <= 6; sector++) {
					if (sectorButton[sector - 1].isSelected()) {
						if (sector != _sector) {
							_sector = sector;
							System.out.println("Sector is now " + _sector);
							canvas1.setSector(_sector);
							canvas1.repaint();
						}
						break;
					}
				}
			}

		};

		for (int sector = 1; sector <= 6; sector++) {
			sectorButton[sector - 1] = new JRadioButtonMenuItem("Sector " + sector, sector == _sector);
			sectorButton[sector - 1].addActionListener(al);
			bg.add(sectorButton[sector - 1]);
			sectorMenu.add(sectorButton[sector - 1]);
		}

		return sectorMenu;
	}


	

}
