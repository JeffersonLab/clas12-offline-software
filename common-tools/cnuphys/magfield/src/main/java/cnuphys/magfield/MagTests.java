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
import java.util.ArrayList;
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
import cnuphys.magfield.converter.AsciiReader;
import cnuphys.magfield.converter.Converter;

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
	private static String _currentWorkingDirectory = System.getProperty("user.dir");



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

	/**
	 * Write out a test data file
	 */
	public static void serializeTestData(String path, int n, long seed) {

		System.err.println("Test data file: [" + path + "]");
		System.err.println(System.getProperty("java.version"));
		System.err.println(System.getProperty("java.specification.version"));

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		FieldProbe ifield = FieldProbe.factory();

		TestData td = new TestData(n);

		td.torusFile = new String(MagneticFields.getInstance().getTorusBaseName());
		td.solenoidFile = new String(MagneticFields.getInstance().getSolenoidBaseName());

		System.err.println("TORUS: [" + td.torusFile + "]");
		System.err.println("SOLENOID: [" + td.solenoidFile + "]");

	//	Random rand = new Random(seed);

		double phi = Math.toRadians(15.);
		double delRho = 300./(n-1);
		double delZ = 600./(n-1);

		for (int i = 0; i < n; i++) {
//			double rho = 300 * rand.nextFloat();
//			double phi = 2 * Math.PI * rand.nextFloat();
//			td.x[i] = (float) (rho * Math.cos(phi));
//			td.y[i] = (float) (rho * Math.sin(phi));
//			td.z[i] = 600 * rand.nextFloat();
			
			double rho = (float)(i*delRho);
			td.x[i] = (float) (rho * Math.cos(phi));
			td.y[i] = (float) (rho * Math.sin(phi));
			td.z[i] = (float)(i*delZ);
			ifield.field(td.x[i], td.y[i], td.z[i], td.result[i]);
			
//			System.err.println(String.format("(%7.3f, %7.3f, %7.3f), (%7.3f, %7.3f, %7.3f)", td.x[i], td.y[i], td.z[i],
//					td.result[i][0], td.result[i][1], td.result[i][2]));
		}

		TestData.serialWrite(td, path);
	}

	// sameness tests (overlap not overlap)
	public static void samenessTest() {
		System.err.println("Sameness Test Overlap Removal");

		// int num = 10000000;
		int num = 5000000;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		long seed = 5347632765L;

		Random rand = new Random(seed);

		System.err.println("Creating " + num + " random points");
		for (int i = 0; i < num; i++) {

			z[i] = 400 * rand.nextFloat();
			float rho = 400 * rand.nextFloat();
//			double phi = Math.toRadians(-25 + 50*rand.nextFloat());
//			double phi = Math.toRadians(-2 + 4*rand.nextFloat());
			double phi = 0;

			if (i == 0) {
				z[i] = 299.99f;
				rho = 299.99f;
				phi = 0;
			} else if (i == 1) {
				z[i] = 301.01f;
				rho = 301.01f;
				phi = 0;
			}

			x[i] = (float) (rho * FastMath.cos(phi));
			y[i] = (float) (rho * FastMath.sin(phi));
		}

		float result[][][] = new float[2][num][3];
		float diff[] = new float[3];

		System.err.println("Creating space");
		for (int i = 0; i < num; i++) {
			result[0][i] = new float[3];
			result[1][i] = new float[3];
		}

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		FieldProbe ifield = FieldProbe.factory();

		System.err.println("Computing with overlapping");
		long time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			ifield.field(x[i], y[i], z[i], result[0][i]);
		}
		time = System.currentTimeMillis() - time;
		System.err.println("Time for overlapping field: " + (time) / 1000.);

		// now remove overlap

		System.err.println("Computing with non-overlapping");
		MagneticFields.getInstance().removeMapOverlap();
		ifield = FieldProbe.factory();

		time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			ifield.field(x[i], y[i], z[i], result[1][i]);
		}
		time = System.currentTimeMillis() - time;
		System.err.println("Time for non-overlapping field: " + (time) / 1000.);

		System.err.println("Computing biggest diff");
		// now get biggest diff
		double maxDiff = -1;
		int iMax = -1;

		for (int i = 0; i < num; i++) {

			for (int j = 0; j < 3; j++) {
				diff[j] = result[1][i][j] - result[0][i][j];
			}
			double dlen = FastMath.vectorLength(diff);

			if (dlen > maxDiff) {
				iMax = i;
				maxDiff = dlen;
			}
		}

		System.err.println("maxDiff = " + maxDiff + "   at index: " + iMax);
		System.err.println(String.format("xyz = (%8.4f, %8.4f, %8.4f)", x[iMax], y[iMax], z[iMax]));
		double phi = FastMath.atan2Deg(y[iMax], x[iMax]);
		if (phi < 0) {
			phi += 360;
		}
		double rho = FastMath.hypot(x[iMax], y[iMax]);
		System.err.println(String.format("cyl = (%8.4f, %8.4f, %8.4f)", phi, rho, z[iMax]));
		System.err.println(String.format("   Overlapping (%8.4f, %8.4f, %8.4f) %8.4f kG", result[0][iMax][0],
				result[0][iMax][1], result[0][iMax][2], FastMath.vectorLength(result[0][iMax])));
		System.err.println(String.format("NonOverlapping (%8.4f, %8.4f, %8.4f) %8.4f kG", result[1][iMax][0],
				result[1][iMax][1], result[1][iMax][2], FastMath.vectorLength(result[1][iMax])));

	}



	// check active field
	private static void checkSectors() {
		IField field = FieldProbe.factory();

		long seed = 3344632211L;

		int num = 1000000;
		// int num = 1;

		float x[] = new float[num];
		float y[] = new float[num];
		float z[] = new float[num];

		float result[][][] = new float[6][num][3];
		float diff[] = new float[3];

		Random rand = new Random(seed);

		for (int i = 0; i < num; i++) {
			z[i] = 300 + (200 * rand.nextFloat());
//			z[i] = 26f;

			double phi = -10f + 20. * rand.nextFloat();

//			phi = 0;
			phi = Math.toRadians(phi);

			float rho = 50 + 400 * rand.nextFloat();

//			rho = 50;

			x[i] = (float) (rho * Math.cos(phi));
			y[i] = (float) (rho * Math.sin(phi));
		}

		System.err.println("\nSector test created points");

		float delMax = 0;
		int iMax = -1;
		int sectMax = -1;

		for (int i = 0; i < num; i++) {

			float locDelMax = 0;
			int locSectMax = -1;

			// System.err.println("--------");
			for (int sect = 1; sect <= 6; sect++) {

				field.field(sect, x[i], y[i], z[i], result[sect - 1][i]);

//				System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", 
//						sect, result[sect-1][i][0], result[sect-1][i][1], result[sect-1][i][2],
//						FastMath.vectorLength(result[sect-1][i])));

				if (sect > 0) {
					for (int k = 0; k < 3; k++) {
						diff[k] = result[sect - 1][i][k] - result[0][i][k];
					}
					double dlen = FastMath.vectorLength(diff);
					if (dlen > locDelMax) {
						locDelMax = (float) dlen;
						locSectMax = sect;
					}
				}

			} // end sector loop

			if (locDelMax > delMax) {
				delMax = locDelMax;
				iMax = i;
				sectMax = locSectMax;
			}

		}
		System.err.println("\nSector test calculated field");
		System.err.println(" Biggest diff: " + delMax + "  in sector " + sectMax);
		System.err.println(String.format("xyz = (%8.4f, %8.4f, %8.4f)", x[iMax], y[iMax], z[iMax]));

		double phi = FastMath.atan2Deg(y[iMax], x[iMax]);
		if (phi < 0) {
			phi += 360;
		}
		double rho = FastMath.hypot(x[iMax], y[iMax]);
		System.err.println(String.format("cyl = (%8.4f, %8.4f, %8.4f)", phi, rho, z[iMax]));

//		System.err.println(String.format("Sector 1 (%9.5f, %9.5f, %9.5f) %9.5f", 
//				result[0][iMax][0], result[0][iMax][1], result[0][iMax][2], 
//				FastMath.vectorLength(result[0][iMax])));

		for (int sect = 1; sect <= 6; sect++) {
			int sm1 = sect - 1;
			System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", sect, result[sm1][iMax][0],
					result[sm1][iMax][1], result[sm1][iMax][2], FastMath.vectorLength(result[sm1][iMax])));
		}

	}

	// chectk sectors for rotated composite
	private static void checkRotatedSectors() {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		RotatedCompositeProbe probe = (RotatedCompositeProbe) FieldProbe.factory();

		float result[] = new float[3];

//		double x = -60. + 20*Math.random();
		double x = 100. + 20 * Math.random();
		double y = -40. + 20 * Math.random();
		double z = 290. + 30 * Math.random();

		System.err.println(String.format("\n xyz = (%8.4f, %8.4f, %8.4f)", x, y, z));
		for (int sector = 1; sector <= 6; sector++) {
			probe.field(sector, (float) x, (float) y, (float) z, result);
			double Bx = result[0];
			double By = result[1];
			double Bz = result[2];
			double bmag = Math.sqrt(Bx * Bx + By * By + Bz * Bz);
			System.err.println(String.format("Sector %d (%9.5f, %9.5f, %9.5f) %9.5f", sector, result[0], result[1],
					result[2], bmag));
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

	// load the ascii torus
	private static void loadAsciiTorus() {
		ToAscii.readAsciiTorus("/Users/heddle/magfield/FullTorus.txt");
	}

	
	private static void parseMauriFiles(FieldProbe probe, final ArrayList<GEMCCompare> compares) {
		File mfdir = new File(System.getProperty("user.home"), "magfield");
//		File file = new File(mfdir, "gemctorusNone.txt");
		File file = new File(mfdir, "gemctorusLinear.txt");
		
		String lines[] = new String[6];
		
		try {
			AsciiReader ar = new AsciiReader(file) {
				
				int index = 0;


				@Override
				protected void processLine(String line) {
					line = line.replaceAll("[^\\d-.e ]", "");
					line = line.replaceAll("e ", " ");
					line = line.replaceAll(" e", " ");
					line = line.replaceAll("    ", " ");
					line = line.replaceAll("   ", " ");
					line = line.replaceAll("  ", " ");
	//				System.err.println(line);
					
					lines[index] = new String(line);
					
					if (index == 5) {
						parse();
					}
					
					index = (index + 1) % 6;
					
				}

				@Override
				public void done() {
				}
				
				private void parse() {
					String tokens[] = MagneticFields.tokens(lines[0], " ");
					float x = Float.parseFloat(tokens[0]);
					float y = Float.parseFloat(tokens[1]);
					float z = Float.parseFloat(tokens[2]);
					
					tokens = MagneticFields.tokens(lines[3], " ");
					
					int n1 = Integer.parseInt(tokens[0]);
					int n2 = Integer.parseInt(tokens[1]);
					int n3 = Integer.parseInt(tokens[2]);
							
//					tokens = MagneticFields.tokens(lines[4], " ");
//					float bx = Float.parseFloat(tokens[0]);
//					float by = Float.parseFloat(tokens[1]);
//					float bz = Float.parseFloat(tokens[2]);
					
					tokens = MagneticFields.tokens(lines[5], " ");
					float globalbx = Float.parseFloat(tokens[0]);
					float globalby = Float.parseFloat(tokens[1]);
					float globalbz = Float.parseFloat(tokens[2]);

					
					compares.add(new GEMCCompare(probe, x, y, z, n1, n2, n3, globalbx, globalby, globalbz));
					
				}
				
			};
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void compareGEMCTorus() {
		System.out.println("Setting field to torus only.");


		MagneticFields.getInstance().setActiveField(FieldType.TORUS);

		IField ifield = FieldProbe.factory();
		TorusProbe torusProbe = (TorusProbe)ifield;
		
		Torus torus = MagneticFields.getInstance().getTorus();
		torus.setScaleFactor(1.0);
		MagneticField.setInterpolate(true);
		
		ArrayList<GEMCCompare> compares = new ArrayList<>();
		parseMauriFiles(torusProbe, compares);
		
		
		
		for (GEMCCompare compare : compares) {
	//		ifield.field(compare.x, compare.y, compare.z, compare.bced);
			System.err.println(compare);
		}

		


	}

	private static void compareGEMCSolenoid() {
		System.out.println("Setting field to solenoid only.");

		double gemcdata[][] = {
//				//SOLEN LINEAR
//				{252.621, 78.0631, -84.644,   9.78175,  3.02268,  0.0939963},
//				{98.5272, 80.6655, -235.325,   -36.5625,  -29.9342,  9.78602},
//				{145.337, 182.43, 221.267,   3.56792,  4.47853,  -7.70959},
//				{198.186, 138.028, -129.261,   8.68335,  6.04758,  -9.51473},
//				{13.0208, 178.025, -154.435,   0.030217,  0.413138,  -41.6462},
//				{246.237, 144.786, -117.245,   5.07523,  2.9842,  -3.03708},
//				{194.157, 95.8568, -145.274,   8.39041,  4.14241,  -18.6848},
//				{174.724, 237.351, 52.0498,   -2.42903,  -3.29969,  3.16203},
//				{4.7558, 289.171, -204.95,   -0.0144656,  -0.879565,  -4.79184},
//				{188.956, 164.685, 221.243,   2.84971,  2.48367,  -6.39677},
//				{108.793, 130.519, 271.142,   11.2787,  13.5311,  1.21306},
//				{190.365, 226.68, -261.684,   -1.85224,  -2.20557,  -2.70522},
//				{27.2253, 106.754, -183.089,   -32.7932,  -128.587,  -10.4294},
//				{77.9569, 279.363, 166.032,   -0.559574,  -2.00526,  -4.55015},
//				{70.9442, 260.838, -26.198,   1.2042,  4.42745,  7.0013},
//				{185.785, 179.892, 6.0338,   -0.33558,  -0.324935,  10.1066},
//				{81.4187, 88.5149, 64.3861,   -220.186,  -239.376,  490.642},
//				{13.1301, 78.056, -75.8103,   -741.999,  -4411.04,  -9880.13},
//				{114.773, 217.91, 24.153,   -2.3905,  -4.53864,  12.0242},
//				{141.11, 137.61, -60.883,   26.9757,  26.3066,  16.8306},
//				{114.791, 113.642, -160.319,   -12.0075,  -11.8874,  -53.8249},
//				{154.075, 166.87, -18.2631,   5.25176,  5.6879,  17.5978},
//				{268.657, 7.964, 279.725,   4.27454,  0.126713,  -2.26789},
//				{64.2641, 230.487, -114.639,   3.7368,  13.4023,  -7.51401},
//				{172.466, 174.868, 91.8857,   -9.94714,  -10.0857,  -0.545308},
//				{25.532, 94.4568, 230.476,   17.7289,  65.5889,  35.5982},
//				{156.575, 71.0398, -288.072,   -14.0702,  -6.38382,  2.81132},
//				{46.495, 95.6866, 155.67,   88.1142,  181.339,  -94.3699},
//				{244.339, 133.918, -222.247,   -2.03128,  -1.11331,  -4.84167},
//				{102.994, 66.01, 246.892,   34.5357,  22.1344,  13.8334}
//				};

				// SOLENOID NN
				{ 21.0317, 244.232, 95.2729, -1.21889, -14.1544, -1.41521 },
				{ 174.432, 196.567, -263.224, -3.20192, -3.60823, -3.27755 },
				{ 288.243, 7.8778, -82.5142, 6.46423, 0.17667, 0.282259 },
				{ 105.14, 181.542, -84.2208, 15.927, 27.5006, -0.327652 },
				{ 16.576, 148.955, 176.399, 4.42786, 39.7896, -43.2307 },
				{ 72.9677, 230.268, -244.82, -1.95093, -6.15664, -5.06388 },
				{ 246.484, 81.8586, -1.737, 0.96789, 0.321441, 9.91855 },
				{ 251.295, 5.453, -245.891, -5.41577, -0.11752, -4.66959 },
				{ 210.474, 146.127, 275.894, 4.203, 2.91803, -2.52403 },
				{ 195.531, 213.615, 7.4519, -0.236798, -0.258699, 5.47413 },
				{ 83.8357, 23.184, 123.292, 657.595, 181.852, -439.556 },
				{ 105.841, 134.876, -78.5556, 51.3912, 65.4891, 9.14706 },
				{ 11.743, 291.14, -148.5, 0.132803, 3.29254, -4.4603 },
				{ 48.2771, 110.934, -114.821, 34.6511, 79.623, -329.556 },
				{ 131.855, 97.759, -49.7778, 46.9646, 34.8202, 59.0092 },
				{ 220.196, 115.514, 266.187, 5.0157, 2.63122, -3.17758 },
				{ 73.0324, 5.5785, 178.748, 263.64, 20.1379, 136.897 },
				{ 256.495, 153.741, -156.435, 2.08414, 1.24922, -4.16564 },
				{ 36.172, 72.2379, -226.888, -37.0557, -74.0027, 64.6766 },
				{ 39.39, 39.9497, 271.879, 20.1851, 20.4719, 51.8233 },
				{ 24.1363, 138.98, 205.722, 8.55665, 49.2703, -13.3516 },
				{ 264.531, 31.4866, -270.127, -4.63366, -0.551535, -2.84764 },
				{ 54.0208, 55.563, -1.2278, 411.473, 423.22, -23869.1 },
				{ 158.14, 96.7593, 160.347, 0.0603408, 0.0369201, -33.0466 },
				{ 105.24, 100.995, 17.3683, 4.14226, 3.97518, 68.608 },
				{ 152.66, 164.654, 186.291, 1.51438, 1.63335, -13.0138 },
				{ 117.994, 168.875, -88.3551, 19.7803, 28.3099, -3.28007 },
				{ 272.064, 35.7897, -146.729, 4.35926, 0.573456, -5.91188 },
				{ 152.263, 152.57, 285.83, 6.24651, 6.25913, -1.27165 },
				{ 63.5133, 192.669, 298.619, 3.00884, 9.12737, 0.240413 },
				{ 141.172, 16.2907, 220.072, 42.9365, 4.9547, -4.74594 },
				{ 177.99, 38.3277, 23.1713, -13.176, -2.83727, 43.3687 },
				{ 5.788, 178.128, 259.632, 0.561831, 17.2906, -1.51619 },
				{ 241.583, 155.961, -204.312, -0.754936, -0.48737, -4.91092 } };

		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);

		IField ifield = FieldProbe.factory();

		float result[] = new float[3];

		double delsumrel = 0;
		double delsumabs = 0;

		System.err.println();
		for (double v[] : gemcdata) {
			double x = v[0];
			double y = v[1];
			double z = v[2];

			ifield.field((float) x, (float) y, (float) z, result);

			double gBx = v[3];
			double gBy = v[4];
			double gBz = v[5];
			double gB = Math.sqrt(gBx * gBx + gBy * gBy + gBz * gBz);

			double cBx = result[0] * 1000;
			double cBy = result[1] * 1000;
			double cBz = result[2] * 1000;
			double cB = Math.sqrt(cBx * cBx + cBy * cBy + cBz * cBz); // Gauss

			double avg = 0.5 * (gB + cB);
			double delabs = Math.abs(gB - cB);
			double delrel = delabs / avg;
			delsumabs += delabs;
			delsumrel += delrel;

			String s = String.format(
					"(%-8.3f, %-8.3f, %-8.3f) BGSIM = (%-6.4f, %-6.4f, %-6.4f) [%-8.5f] Bced = (%-6.4f, %-6.4f, %-6.4f) [%-8.5f] delrel = %-8.4f delabs = %-8.4f Gauss",
					x, y, z, gBx, gBy, gBz, gB, cBx, cBy, cBz, cB, delrel, delabs);

			System.err.println(s);
		}

		System.err.println(String.format("avg delrel = %-8.5f", delsumrel / gemcdata.length));
		System.err.println(String.format("avg delabs = %-8.5f Gauss", delsumabs / gemcdata.length));

	}



	// convert the torus to ASCII
	private static void convertTorusToAscii() {
//		ToAscii.torusToAscii(MagneticFields.getInstance().getTorus(), 
//				"/Users/heddle/magfield/FullTorus.txt", false);
		ToAscii.torusToAscii(MagneticFields.getInstance().getTorus(), "/Users/heddle/magfield/FullTorus.csv", true);
	}

	// convert the solenoid to ASCII
	private static void convertSolenoidToAscii() {
		ToAscii.solenoidToAscii((SolenoidProbe) FieldProbe.factory(MagneticFields.getInstance().getSolenoid()),
				"/Users/heddle/magfield/Solenoid.csv");
	}

	public static JMenu getTestMenu() {

		JMenu testMenu = new JMenu("Tests");

		final JMenuItem test0Item = new JMenuItem("Timing Test Random Points");
		final JMenuItem test1Item = new JMenuItem("Timing Test Along a Line");
		final JMenuItem test4Item = new JMenuItem("Overlap/No overlap Sameness Test");
		final JMenuItem asciiTorusItem = new JMenuItem("Convert Torus to ASCII");
		final JMenuItem asciiSolenoidItem = new JMenuItem("Convert Solenoid to ASCII");
		final JMenuItem loadItem = new JMenuItem("Load ASCII Torus");
		final JMenuItem gemcSolenoidItem = new JMenuItem("Compare GEMC Solenoid");
		final JMenuItem gemcTorusItem = new JMenuItem("Compare GEMC Torus");

		final JMenuItem solenoidConvertItem = new JMenuItem("Convert in-memory solenoid to GEMC Format");
		final JMenuItem torusConvertItem = new JMenuItem("Convert in-memory torus to GEMC Format");

		ActionListener al1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == test0Item) {
					timingTest(0);
				} else if (e.getSource() == test1Item) {
					timingTest(1);
				} else if (e.getSource() == test4Item) {
					samenessTest();
				} else if (e.getSource() == asciiTorusItem) {
					convertTorusToAscii();
				} else if (e.getSource() == asciiSolenoidItem) {
					convertSolenoidToAscii();
				} else if (e.getSource() == loadItem) {
					loadAsciiTorus();
				} else if (e.getSource() == gemcSolenoidItem) {
					compareGEMCSolenoid();
				} else if (e.getSource() == gemcTorusItem) {
					compareGEMCTorus();
				} else if (e.getSource() == solenoidConvertItem) {
					Converter.memoryToGEMCSolenoidConverter();
				} else if (e.getSource() == torusConvertItem) {
					Converter.memoryToGEMCTorusConverter();
				}

			}

		};

		test0Item.addActionListener(al1);
		test1Item.addActionListener(al1);
		test4Item.addActionListener(al1);
		asciiTorusItem.addActionListener(al1);
		asciiSolenoidItem.addActionListener(al1);
		loadItem.addActionListener(al1);
		gemcSolenoidItem.addActionListener(al1);
		gemcTorusItem.addActionListener(al1);
		solenoidConvertItem.addActionListener(al1);
		torusConvertItem.addActionListener(al1);

		testMenu.add(test0Item);
		testMenu.add(test1Item);
		testMenu.add(test4Item);
		testMenu.addSeparator();
		testMenu.add(asciiTorusItem);
		testMenu.add(asciiSolenoidItem);
		testMenu.add(loadItem);
		testMenu.addSeparator();
		testMenu.add(gemcSolenoidItem);
		testMenu.add(gemcTorusItem);
		testMenu.addSeparator();

		testMenu.add(solenoidConvertItem);
		testMenu.add(torusConvertItem);
		testMenu.addSeparator();

		// now for rectangular grids
		final JMenuItem rotatedSectorItem = new JMenuItem("Check Sectors for Rotated Composite");
		final JMenuItem sectorItem = new JMenuItem("Check Sectors for (Normal) Composite");

		ActionListener al2 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == reconfigItem) {
					MagneticFields.getInstance().removeMapOverlap();
				} else if (e.getSource() == rotatedSectorItem) {
					checkRotatedSectors();
				} else if (e.getSource() == sectorItem) {
					checkSectors();
				}
			}
		};
		reconfigItem.addActionListener(al2);
		rotatedSectorItem.addActionListener(al2);
		sectorItem.addActionListener(al2);
		testMenu.add(reconfigItem);
		testMenu.add(rotatedSectorItem);
		testMenu.add(sectorItem);

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
			// mf.initializeMagneticFields(mfdir.getPath(), "torus.dat",
			// "Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_08May2018.dat",
//					"Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
//					"Symm_solenoid_r601_phi1_z1201_2008.dat");
//			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
//					"SolenoidMarch2019_BIN.dat");
			mf.initializeMagneticFields(mfdir.getPath(), "Symm_torus_r2501_phi16_z251_24Apr2018.dat",
					"Symm_solenoid_r601_phi1_z1201_13June2018.dat", "Full_transsolenoid_x161_y161_z321_March2021.dat");
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
			sectorMenu.add(sectorButton[sector - 1]);
		}

		return sectorMenu;
	}

	public static void main(String arg[]) {

		final MagneticFields mf = MagneticFields.getInstance();

		// test specific load
		File mfdir = new File(System.getProperty("user.home"), "magfield");
		System.out.println("mfdir exists: " + (mfdir.exists() && mfdir.isDirectory()));
		try {
			mf.initializeMagneticFields(mfdir.getPath(), "Full_torus_r251_phi181_z251_08May2018.dat",
					"Symm_solenoid_r601_phi1_z1201_13June2018.dat", "Full_transsolenoid_x161_y161_z321_March2021.dat");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// write out data file
//		String path = (new File(_homeDir, "magTestData")).getPath();
//		serializeTestData(path, 1000000, 19923456L);
//		serializeTestData(path, 100, 19923456L);
		
		
		//look for the file
		
		
		File file = new File(_currentWorkingDirectory, "magTestData");
		if (!file.exists()) {
			file = new File(_homeDir, "magTestData");
			if (!file.exists()) {
				System.err.println("FATAL ERROR Could not find test data file.");
			}
		}
		
		System.err.println("Will test on data file: [" + file.getPath() + "]");

		testTestData(file.getPath());

		System.err.println("Done");

	}
	


}
