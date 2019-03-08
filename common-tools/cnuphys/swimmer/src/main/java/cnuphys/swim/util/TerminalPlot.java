package cnuphys.swim.util;

/**
 * Just what it sounds like, crude 2D (one data set) terminal scatter plot for
 * quick checks and debugging
 * 
 * @author heddle
 *
 */
public class TerminalPlot {

	public static final int HGAP = 10;
	public static final int HMOD = 10;
	public static final int VMOD = 10;
	public static final int STRINGSPACE = 9;

	public static void plot2D(int width, int height, String title, double x[], double y[]) {

		// width must be multiple of HMOD
		width = (HMOD * width) / HMOD;

		// height must be multiple of VMOD
		height = (VMOD * height) / VMOD;

		// The first col that can hold a data point. One col to right
		// of y axis
		int firstDataCol = HGAP + 1;

		// The first row that can hold a data point. One row above
		// the x axis
		int firstDataRow = 1;

		// this is the max data col. The righthand frame is
		// one col beyond this
		int lastDataCol = firstDataCol + width - 1;

		// this is the max data row. The top frame is
		// one row above this
		int lastDataRow = firstDataRow + height - 1;

		// prepare the data
		double xmin = 0.0;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = 0.0;
		double ymax = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < x.length; i++) {
			xmin = Math.min(xmin, x[i]);
			xmax = Math.max(xmax, x[i]);
			ymin = Math.min(ymin, y[i]);
			ymax = Math.max(ymax, y[i]);
		}

		double dx = (xmax - xmin) / (width - 1);
		double dy = (ymax - ymin) / (height - 1);
		int rows[] = new int[x.length];
		int cols[] = new int[y.length];
		for (int i = 0; i < x.length; i++) {
			cols[i] = firstDataCol + (int) ((x[i] - xmin) / dx);
			rows[i] = firstDataRow + (int) ((y[i] - ymin) / dy);
		}

		// centered plot title
		printPlotTitle(title, width);
		blankLines(1);

		// top of box
		yborder(ymax, firstDataCol, lastDataCol);

		// each row
		for (int row = lastDataRow; row >= firstDataRow; row--) {
			double aval = (row - firstDataRow) * dy;
			printRow(aval, row, firstDataRow, lastDataRow, firstDataCol, lastDataCol, rows, cols);
			blankLines(1);
		}

		// bottom of box
		yborder(Double.NaN, firstDataCol, lastDataCol);

		// x values
		printXValues(xmin, xmax, width);
	}

	// print x axis values
	private static void printXValues(double xmin, double xmax, int width) {

		int hmod = HMOD;
		int dx = hmod - STRINGSPACE;

		while (dx < 1) {
			hmod += HMOD;
			dx = hmod - STRINGSPACE;
		}

		int numVals = 1 + (width / hmod);
		double del = (xmax - xmin) / (numVals - 1);

		for (int j = 0; j < (HGAP - STRINGSPACE / 2); j++) {
			System.out.print(' ');
		}

		for (int i = 0; i < numVals; i++) {
			double x = xmin + i * del;
			for (int j = 0; j < dx; j++) {
				System.out.print(' ');
			}
			System.out.print(fnum(x));
		}
	}

	// format a number
	private static String fnum(double v) {
		return String.format("%-9.2e", v);
	}

	// top and bottom of box
	private static void yborder(double val, int firstDataCol, int lastDataCol) {

		if (Double.isNaN(val)) {
			hgap();
		} else {
			System.out.print(fnum(val));
			for (int j = 0; j < (HGAP - STRINGSPACE); j++) {
				System.out.print(' ');
			}
		}

		System.out.print('|');
		for (int i = firstDataCol; i <= lastDataCol; i++) {
			int dx = i - firstDataCol;
			boolean usePlus = (dx % HMOD) == 0;
			if (usePlus) {
				System.out.print('+');
			} else {
				System.out.print('-');
			}
		}
		System.out.print('|');
		blankLines(1);
	}

	// print a row
	private static void printRow(double aval, int row, int firstDataRow, int lastDataRow, int firstDataCol,
			int lastDataCol, int rows[], int cols[]) {
		int leftBorder = firstDataCol - 1;
		int rightBoder = lastDataCol + 1;

		int dy = row - firstDataRow;
		boolean usePlus = (dy % VMOD) == 0;

		if (!usePlus) {
			for (int col = 0; col < HGAP; col++) {
				System.out.print(' ');
			}
		} else {
			System.out.print(fnum(aval));
			for (int i = 0; i < (HGAP - STRINGSPACE); i++) {
				System.out.print(' ');
			}

		}

		for (int col = HGAP; col <= rightBoder; col++) {

			if ((col == leftBorder) || (col == rightBoder)) {
				if (usePlus) {
					System.out.print('+');
				} else {
					System.out.print('|');
				}
			} else {
				if (isDataPoint(row, col, rows, cols)) {
					System.out.print('*');
				} else {
					System.out.print(' ');
				}
			}
		}
	}

	// check whether the row and column should be printed
	// as a data point
	private static boolean isDataPoint(int row, int col, int rows[], int cols[]) {
		for (int i = 0; i < rows.length; i++) {
			if ((row == rows[i]) && (col == cols[i])) {
				return true;
			}
		}
		return false;
	}

	// print the plot title
	private static void printPlotTitle(String title, int width) {
		int len = title.length();
		int xo = Math.max(0, (width - len) / 2);
		repeat(xo, ' ');
		System.out.println(title);
	}

	// print repeated chars
	private static void repeat(int n, char c) {
		hgap();
		for (int i = 0; i < n; i++) {
			System.out.print(c);
		}
	}

	// blanklines
	private static void blankLines(int n) {
		for (int i = 0; i < n; i++) {
			System.out.println("");
		}
	}

	// horizontal gap or margin
	private static void hgap() {
		for (int i = 0; i < HGAP; i++) {
			System.out.print(' ');
		}
	}

	public static void main(String arg[]) {

		// create some x values
		double x[] = new double[16];
		for (int i = 0; i < x.length; i++) {
			x[i] = i * 0.1;
		}

		// crate some y values
		double y[] = new double[x.length];
		double v = 4.;
		double g = 9.8;
		double theta = Math.PI / 4.;

		for (int i = 0; i < y.length; i++) {
			double cost = Math.cos(theta);
			y[i] = x[i] * Math.tan(theta) - g * x[i] * x[i] / (2 * v * v * cost * cost);
		}

		plot2D(80, 40, "Sample Plot: height y (m) vs. distance x (m)", x, y);
	}
}
