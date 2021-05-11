package cnuphys.rk4;

public class ButcherTableau {

	public static ButcherTableau RK4 = new ButcherTableau("RK4", false, makeRow(0.), makeRow(1. / 2., 1. / 2.),
			makeRow(1. / 2., 0, 1. / 2), makeRow(1., 0, 0, 1.), makeRow(1. / 6., 1. / 3., 1. / 3., 1. / 6.) // the
																											// b's
	);

	public static ButcherTableau RULE38 = new ButcherTableau("RULE38", false, makeRow(0.), makeRow(1. / 3., 1. / 3.),
			makeRow(2. / 3., -1. / 3., 1.), makeRow(1., 1., -1., 1.), makeRow(1. / 8., 3. / 8., 3. / 8., 1. / 8.) // the
																													// b's
	);

	public static ButcherTableau FEHLBERG_ORDER5 = new ButcherTableau("FEHLBERG_ORDER5", true, makeRow(0.),
			makeRow(1. / 4., 1. / 4.), makeRow(3. / 8., 3. / 32., 9. / 32.),
			makeRow(12. / 13, 1932. / 2197, -7200. / 2197., 7296. / 2197.),
			makeRow(1., 439. / 216., -8., 3680. / 513., -845. / 4104.),
			makeRow(1. / 2., -8. / 27., 2., -3544. / 2565, 1859. / 4104., -11. / 40.),
			makeRow(16. / 135., 0., 6656. / 12825., 28561. / 56430., -9. / 50., 2. / 55.), // b's
			makeRow(25. / 216., 0., 1408. / 2565., 2197. / 4104., -1. / 5., 0.) // bstars's
	);

	public static ButcherTableau DORMAND_PRINCE = new ButcherTableau("DORMAND_PRINCE", true, makeRow(0.),
			makeRow(1. / 5., 1. / 5.), makeRow(3. / 10., 3. / 40., 9. / 40.),
			makeRow(4. / 5., 44. / 45., -56. / 15, 32. / 9.),
			makeRow(8. / 9., 19372. / 6561., -25360. / 2187., 64448. / 6561., -212. / 729.),
			makeRow(1., 9017. / 3168., -355. / 33., 46732. / 5247., 49. / 176., -5103. / 18656.),
			makeRow(1., 35. / 384., 0, 500. / 1113., 125. / 192., -2187. / 6784., 11. / 84.),
			makeRow(5179. / 57600., 0, 7571. / 16695., 393. / 640., -92097. / 339200., 187. / 2100., 1. / 40.), // b's
			makeRow(35. / 384., 0, 500. / 1113., 125. / 192., -2187. / 6784., 11. / 84., 0) // bstars's
	);

	public static ButcherTableau CASH_KARP = new ButcherTableau("CASH_KARP", true, makeRow(0.),
			makeRow(1. / 5., 1. / 5.), 
			makeRow(3. / 10., 3. / 40., 9. / 40.),
			makeRow(3. / 5, 3. / 10., -9. / 10., 6. / 5.), 
			makeRow(1., -11. / 54., 5. / 2., -70. / 27., 35. / 27.),
			makeRow(7. / 8., 1631. / 55296., 175. / 512., 575. / 13824., 44275. / 110592., 253. / 4096.),
			makeRow(37. / 378., 0, 250. / 621., 125. / 594., 0., 512. / 1771.), // b's
			makeRow(2825. / 27648., 0, 18575. / 48384., 13525. / 55296., 277. / 14336., 1. / 4.) // bstars's
	);

	private double a[][];
	private double b[];
	private double bstar[];
	private double c[];
	private boolean _augmented;
	private double bdiff[];
	private String _name;

	private int s; // number of stages

	private ButcherTableau(String name, boolean augmented, double[]... rows) {
		_name = name;
		_augmented = augmented;
		if (augmented) {
			s = rows.length - 2;
		} else {
			s = rows.length - 1;
		}

		// we make things big enough that we can ignore the zero indices
		// and use the wikipedia format. E.g., the relevant a's are
		// a[2][1]
		// a[3][1] a[3][2]
		// up to a[s][s-1]
		a = new double[s + 1][s + 1];
		b = new double[s + 1];
		c = new double[s + 1];

		for (int row = 0; row <= s; row++) {
			for (int col = 0; col <= s; col++) {
				if ((row < 2) || (col == 0) || (col >= row)) {
					a[row][col] = Double.NaN;
				} else {
					a[row][col] = rows[row - 1][col];
				}
			}
		}

		b[0] = Double.NaN; // never used
		for (int i = 1; i <= s; i++) {
			b[i] = rows[s][i - 1];
		}

		if (augmented) {
			bstar = new double[s + 1];
			bdiff = new double[s + 1];
			bstar[0] = Double.NaN; // never used
			bdiff[0] = Double.NaN; // never used
			for (int i = 1; i <= s; i++) {
				bstar[i] = rows[s + 1][i - 1];
				bdiff[i] = b[i] - bstar[i];
			}

		}

		c[0] = Double.NaN; // never used
		c[1] = Double.NaN; // never used
		for (int i = 2; i <= s; i++) {
			c[i] = rows[i - 1][0];
		}

	}

	/**
	 * Get the number of stages
	 * 
	 * @return the number of stages
	 */
	public int getNumStage() {
		return s;
	}

	public double a(int i, int j) {
		return a[i][j];
	}

	public double b(int index) {
		return b[index];
	}

	public double bstar(int index) {
		return bstar[index];
	}

	public double bdiff(int index) {
		return bdiff[index];
	}

	public double c(int index) {
		return c[index];
	}

	public boolean isAugmented() {
		return _augmented;
	}

	// used to specify a row to the constructor
	private static double[] makeRow(double... vals) {
		return vals;
	}

	private double asum(int index) {
		double sum = 0;
		for (int j = 1; j < index; j++) {
			sum += a[index][j];
		}
		return sum;
	}

	private void printSumStr(int index) {
		double sum = asum(index);
		double c = c(index);
		double diff = Math.abs(sum - c);
		System.out.println("consistency sum check [" + index + "] sum: " + sum + "  c: " + c + " diff: " + diff + "  "
				+ ((Math.abs(diff) < 1.0e-15) ? "PASS" : "FAIL"));
	}

	public void report() {
		System.out.println("=============");
		System.out.println(_name);
		System.out.println("augmented: " + isAugmented());
		System.out.println("s = " + getNumStage());

		int s = getNumStage();
		for (int row = 2; row <= s; row++) {
			for (int col = 1; col < row; col++) {
				printVal(row, col);
			}
			System.out.println();
		}

		for (int i = 2; i <= s; i++) {
			printSumStr(i);
		}

	}

	private void printVal(int i, int j) {
		String s = String.format("a[%d][%d] = %-12.5f ", i, j, a[i][j]);
		System.out.print(s);
	}

	public static void main(String arg[]) {
		RK4.report();
		RULE38.report();
		FEHLBERG_ORDER5.report();
		DORMAND_PRINCE.report();
		CASH_KARP.report();
	}
}
