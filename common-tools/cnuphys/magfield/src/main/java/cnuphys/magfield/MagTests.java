package cnuphys.magfield;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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

/**
 * Static testing of the magnetic field
 * 
 * @author heddle
 *
 */
public class MagTests {
	private static final JMenuItem reconfigItem = new JMenuItem("Remove Solenoid and Torus Overlap");
	private static int _sector = 1;
	final static MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
			MagneticFieldCanvas.CSType.XZ);
//	final static MagneticFieldCanvas canvas2 = new MagneticFieldCanvas(_sector, -50, 0, 650, 350.,
//			MagneticFieldCanvas.CSType.YZ);


	private static String options[] = { "Random, with active field", " Along line, with active field",
			"Random, with active PROBE", " Along line, with active PROBE" };

	private static String _homeDir = System.getProperty("user.home");
	
	// sameness tests (probes and not probes)
	private static void samenessTest() {
		System.out.println("Sameness Test Probe v. Not Probe");

		int num = 10000000;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		float result1[] = new float[3];
		float result2[] = new float[3];
		float diff[] = new float[3];

		IField ifield1 = MagneticFields.getInstance().getActiveField();
		IField ifield2 = FieldProbe.factory(MagneticFields.getInstance().getActiveField());

		double dT = 1. / (num - 1);
		for (int i = 0; i < num; i++) {
			double t = i * dT;
			x[i] = (float) (85. * t);
			y[i] = (float) (15. * t);
			z[i] = (float) (372. * t);
		}

		double maxDiff = -1;
		for (int i = 0; i < num; i++) {
			ifield1.field(x[i], y[i], z[i], result1);
			ifield2.field(x[i], y[i], z[i], result2);

			for (int j = 0; j < 3; j++) {
				diff[j] = result2[j] - result1[j];
				double dlen = FastMath.vectorLength(diff);
				maxDiff = Math.max(dlen, maxDiff);
			}
		}

		System.err.println("maxDiff = " + maxDiff);
	}

	
	private static void checkSectors() {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		RotatedCompositeProbe probe =  (RotatedCompositeProbe) FieldProbe.factory();

		float result[] = new float[3];
		
//		double x = -60. + 20*Math.random();
		double x = 100. + 20*Math.random();
		double y = -40. + 20*Math.random();
		double z = 290. + 30*Math.random();
		
		for (int sector = 1; sector <= 6; sector++) {
			probe.field(sector, (float)x, (float)y, (float)z, result);
			double Bx = result[0];
			double By = result[1];
			double Bz = result[2];
			double bmag = Math.sqrt(Bx*Bx + By*By + Bz*Bz);
			System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", sector, result[0], result[1], result[2], bmag));
		}
		
	}
	
	//compare the rectangular and cylindrical grids
	private static void rectCylCompareTest() {
		try {
			
			long seed = 3344632211L;

			int num = 1000000;

			float x[] = new float[num];
			float y[] = new float[num];
			float z[] = new float[num];
	
			float result1[][] = new float[num][3];
			float result2[][] = new float[num][3];
			float res[] = new float[3];
			long time1;
			long time2;

			Random rand = new Random(seed);
			
			for (int i = 0; i < num; i++) {
				z[i] = 100 + (500 * rand.nextFloat());
				double phi = -10f + 20.*rand.nextFloat();
				
				int randSect = rand.nextInt(6);
				phi = phi + (60*randSect);
				phi = Math.toRadians(phi);
				
				float rho = 50 + 400*rand.nextFloat();
				
				
				x[i] = (float)(rho*Math.cos(phi));
				y[i] = (float)(rho*Math.sin(phi));
			}

			MagneticFields.getInstance().openNewTorus("/Users/heddle/magfield/Full_torus_r251_phi181_z251_08May2018.dat");
			IField ifield = FieldProbe.factory(MagneticFields.getInstance().getTorus());
			
			for (int i = 0; i < num; i++) {
				ifield.field(x[i], y[i], z[i], result1[i]);
			}
			
			//for timing
			time1 = System.currentTimeMillis();
			for (int j = 0; j < 10; j++) {
				for (int i = 0; i < num; i++) {
					ifield.field(x[i], y[i], z[i], res);
				}
			}
			time1 = System.currentTimeMillis() - time1;

			
			MagneticFields.getInstance().openNewTorus("/Users/heddle/magfield/rectTorus.dat");
			ifield = FieldProbe.factory(MagneticFields.getInstance().getTorus());
			
			for (int i = 0; i < num; i++) {
				ifield.field(x[i], y[i], z[i], result2[i]);
				for (int k = 0; k < 3; k++) {
					result2[i][k] = -result2[i][k];
				}
			}
			
			//for timing
			time2 = System.currentTimeMillis();
			for (int j = 0; j < 10; j++) {
				for (int i = 0; i < num; i++) {
					ifield.field(x[i], y[i], z[i], res);
				}
			}
			time2 = System.currentTimeMillis() - time2;


			
			double maxDiff = -1;
			int maxIndex = -1;
			float diff[] = new float[3];
			double sum = 0;
			for (int i = 0; i < num; i++) {

				for (int j = 0; j < 3; j++) {
					diff[j] = result2[i][j] - result1[i][j];
					double dlen = FastMath.vectorLength(diff);
					sum += dlen;
					
					if (dlen > maxDiff) {
						maxDiff = dlen;
						maxIndex = i;
					}
					
					maxDiff = Math.max(dlen, maxDiff);
				}
			}

			System.err.println("maxDiff = " + maxDiff);
			System.err.println("avgDiff = " + (sum/num));
			System.err.println(String.format("Location (%-8.3f, %-8.3f, %-8.3f)", x[maxIndex], y[maxIndex], z[maxIndex]));
			
			
			System.err.println("PHI = " + FastMath.atan2Deg(y[maxIndex], x[maxIndex]));
			
			System.err.println(String.format("B1 (%-8.3f, %-8.3f, %-8.3f)", result1[maxIndex][0], result1[maxIndex][1], result1[maxIndex][2]));
			System.err.println(String.format("B2 (%-8.3f, %-8.3f, %-8.3f)", result2[maxIndex][0], result2[maxIndex][1], result2[maxIndex][2]));
			
			System.err.println("Time 1: " + ((double)time1)/1000.);
			System.err.println("Time 1: " + ((double)time2)/1000.);
			
			System.out.println("Done Rect-Cyl Comparison");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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

		IField ifield;

		if ((option == 2) || (option == 3)) {
			ifield = FieldProbe.factory(MagneticFields.getInstance().getActiveField());
		} else {
			ifield = MagneticFields.getInstance().getActiveField();
		}

		Random rand = new Random(seed);

		if ((option == 0) || (option == 2)) {
			for (int i = 0; i < num; i++) {
				z[i] = 600 * rand.nextFloat();
				float rho = 600 * rand.nextFloat();
				double phi = Math.toRadians(30 * rand.nextFloat());

				x[i] = (float) (rho * FastMath.cos(phi));
				y[i] = (float) (rho * FastMath.sin(phi));
			}
		} else if ((option == 1) || (option == 3)) {
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

			double del = ((double) (System.currentTimeMillis() - time)) / 1000.;
			sum += del;

			System.out.println("loop " + (outer + 1) + " time  = " + del + " sec");

		}
		System.out.println("avg " + (sum / 5.) + " sec");

	}
	
	

	private static float getMax(float min, float del, int n) {
		return min + (n - 1) * del;
	}

	// create a rectangular torus map
	// creates a full rectangular map from the current
	private static void createRectangularTorus(float xmin, float delX, int nX, float ymin, float delY, int nY,
			float zmin, float delZ, int nZ) {

		try {
			IField ifield = FieldProbe.factory(MagneticFields.getInstance().getTorus());
			float result[] = new float[3];
			// FloatVect[][][] bvals = new FloatVect[nX][nY][nZ];

			File bfile = new File(_homeDir, "rectTorus.dat");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(bfile));

			// write header
			dos.writeInt(0xced); // magic word
			dos.writeInt(1); // Cartesian grid
			dos.writeInt(1); // Cartesian field
			dos.writeInt(0); // cm
			dos.writeInt(0); // degrees
			dos.writeInt(0); // kG
			dos.writeFloat(xmin);
			dos.writeFloat(getMax(xmin, delX, nX));
			dos.writeInt(nX);
			dos.writeFloat(ymin);
			dos.writeFloat(getMax(ymin, delY, nY));
			dos.writeInt(nY);
			dos.writeFloat(zmin);
			dos.writeFloat(getMax(zmin, delZ, nZ));
			dos.writeInt(nZ);

			long unixTime = System.currentTimeMillis();

			int high = (int) (unixTime >> 32);
			int low = (int) unixTime;

			// write reserved
			dos.writeInt(high); // first word of unix time
			dos.writeInt(low); // second word of unix time
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);

			int size = 3 * 4 * nX * nY * nZ;
			byte bytes[] = new byte[size];
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			System.err.println("FILE SIZE = " + size + " bytes");

			float x = Float.NaN;
			float y = Float.NaN;
			float z = Float.NaN;

			for (int ix = 0; ix < nX; ix++) {
				x = xmin + ix * delX;
				System.out.println("X = " + x);
				for (int iy = 0; iy < nY; iy++) {
					y = ymin + iy * delY;
					for (int iz = 0; iz < nZ; iz++) {
						z = zmin + iz * delZ;
						ifield.field(x, y, z, result);

						byteBuffer.putFloat(result[0]);
						byteBuffer.putFloat(result[1]);
						byteBuffer.putFloat(result[2]);

						// dos.writeFloat(result[0]);
						// dos.writeFloat(result[1]);
						// dos.writeFloat(result[2]);

						// bvals[ix][iy][iz] = new FloatVect(result[0],
						// result[1],result[2]);
					}
				}
			}

			dos.write(bytes);

			System.out.println(String.format("Final location: (%-7.3f, %-7.3f, %-7.3f)", x, y, z));

			dos.flush();
			dos.close();

			System.out.println("Binary file: " + bfile.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static JMenu getTestMenu() {

		JMenu testMenu = new JMenu("Tests");

		final JMenuItem test0Item = new JMenuItem("Timing Test Random Points");
		final JMenuItem test1Item = new JMenuItem("Timing Test Along a Line");
		final JMenuItem test2Item = new JMenuItem("PROBE Timing Test Random Points");
		final JMenuItem test3Item = new JMenuItem("PROBE Timing Test Along a Line");
		final JMenuItem test4Item = new JMenuItem("Sameness Test");

		ActionListener al1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == test0Item) {
					timingTest(0);
				} else if (e.getSource() == test1Item) {
					timingTest(1);
				} else if (e.getSource() == test2Item) {
					timingTest(2);
				} else if (e.getSource() == test3Item) {
					timingTest(3);
				} else if (e.getSource() == test4Item) {
					samenessTest();
				}
			}

		};

		test0Item.addActionListener(al1);
		test1Item.addActionListener(al1);
		test2Item.addActionListener(al1);
		test3Item.addActionListener(al1);
		test4Item.addActionListener(al1);
		testMenu.add(test0Item);
		testMenu.add(test1Item);
		testMenu.add(test2Item);
		testMenu.add(test3Item);
		testMenu.add(test4Item);
		testMenu.addSeparator();

		// now for rectangular grids
		final JMenuItem rectTorusItem = new JMenuItem("Create Rectangular Torus");
		final JMenuItem rectCylItem = new JMenuItem("Compare Rectangular and Cylindrical");
		final JMenuItem rotatedSectorItem = new JMenuItem("Check Sectors for Rotated Composite");

		ActionListener al2 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == rectTorusItem) {
					createRectangularTorus(-500, 2, 501, -500, 2, 501, 100, 2, 251);
				}
				else if (e.getSource() == rectCylItem) {
					rectCylCompareTest();
				}
				else if (e.getSource() == reconfigItem) {
					MagneticFields.getInstance().removeMapOverlap();
				}
				else if (e.getSource() == rotatedSectorItem) {
					checkSectors();
				}
			}
		};
		rectTorusItem.addActionListener(al2);
		rectCylItem.addActionListener(al2);
		reconfigItem.addActionListener(al2);
		rotatedSectorItem.addActionListener(al2);
		testMenu.add(rectTorusItem);
		testMenu.add(rectCylItem);
		testMenu.addSeparator();
		testMenu.add(reconfigItem);
		testMenu.add(rotatedSectorItem);


		return testMenu;
	}
	
	private static void fixMenus() {
		boolean hasSolenoid = MagneticFields.getInstance().hasSolenoid();
		boolean hasTorus = MagneticFields.getInstance().hasTorus();
		reconfigItem.setEnabled(hasSolenoid && hasTorus);
	}



	//set up the frame to run the tests
	public static void runTests() {
		final JFrame testFrame = new JFrame("Magnetic Field");
		testFrame.setLayout(new BorderLayout(4, 4));
		

		final MagneticFields mf = MagneticFields.getInstance();

		// test specific load
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			// mf.initializeMagneticFields(mfdir.getPath(), "torus.dat",
			// "Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_08May2018.dat",
//					"Symm_solenoid_r601_phi1_z1201_2008.dat");
			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_18Apr2018.dat",
					"Symm_solenoid_r601_phi1_z1201_2008.dat");
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
//		JPanel magPanel2 = canvas2.getPanelWithStatus(1000, 465);

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
//		cpanel.add(magPanel2);
		
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


	//the menu for changing sectors
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
//							canvas2.setSector(_sector);
							
							canvas1.repaint();
//							canvas2.repaint();
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
			sectorMenu.add(sectorButton[sector-1]);
		}
		
		
		return sectorMenu;
	}
}
