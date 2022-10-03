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
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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

	private static void timeMessage(String msg, long duration) {
		System.out.println(String.format("%s  %7.4f s", msg, (duration)/1.0e9));
	}



	// test speeding up tited system calls
	private static void tiltedTest() {
		System.out.println("\nTilted Sector Speedup Test");

		long seed = 5347632765L;

		int num = 50000000;

		Random rand = new Random(seed);
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		float result[] = new float[3];

		//get the test data
		for (int i = 0; i < num; i++) {
			x[i] = 100 + 20 * rand.nextFloat();
			y[i] = -40 + 20 * rand.nextFloat();
			z[i] = 290 + 30 * rand.nextFloat();
		}

		RotatedCompositeProbe probe = (RotatedCompositeProbe) FieldProbe.factory();

		long threadId = Thread.currentThread().getId();
		long start = cpuTime(threadId);
		for (int i = 0; i < num; i++) {
			probe.field(1, x[i], y[i], z[i], result);
		}
		timeMessage("time ", cpuTime(threadId) - start);


	}

	// timing tests
	private static void timingTest(int option) {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		System.out.println("Timing tests: [" + options[option] + "]");
		long seed = 5347632765L;

		int num = 50000000;

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

				x[i] = (float) (rho * Math.cos(phi));
				y[i] = (float) (rho * Math.sin(phi));
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

		//timing test
		long threadId = Thread.currentThread().getId();

			long time = cpuTime(threadId);

			for (int i = 0; i < num; i++) {
				ifield.field(x[i], y[i], z[i], result);
			}

			timeMessage("time ", cpuTime(threadId) - time);


	}

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


	public static JMenu getTestMenu() {

		JMenu testMenu = new JMenu("Tests");

		final JMenuItem tiltItem = new JMenuItem("Tilted System Test");
		final JMenuItem test0Item = new JMenuItem("Timing Test Random Points");
		final JMenuItem test1Item = new JMenuItem("Timing Test Along a Line");


		final JMenuItem solenoidConvertItem = new JMenuItem("Convert in-memory solenoid to GEMC Format");
		final JMenuItem torusConvertItem = new JMenuItem("Convert in-memory torus to GEMC Format");

		ActionListener al1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == tiltItem) {
					tiltedTest();
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

		System.out.println(sb.toString());
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
				System.out.println("Done");
				System.exit(1);
			}
		};

		MagneticFieldChangeListener mfcl = new MagneticFieldChangeListener() {

			@Override
			public void magneticFieldChanged() {
				label.setText(" Torus: " + MagneticFields.getInstance().getTorusPath());
				System.out.println("Field changed. Torus path: " + MagneticFields.getInstance().getTorusPath());
				fixMenus();
				MagneticFields.getInstance().printCurrentConfiguration(System.out);
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
