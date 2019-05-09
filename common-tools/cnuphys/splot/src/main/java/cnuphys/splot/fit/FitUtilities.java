package cnuphys.splot.fit;

import java.util.Arrays;
import java.util.Comparator;

public class FitUtilities {

	/**
	 * A Straight line fit with errors in y
	 * 
	 * @param _x    the x data
	 * @param _y    the y data
	 * @param sigma the errors on the y data
	 * @return the fit a + bx
	 */
	public static void fitStraightLine(Fit fit) {
		fixNullSig(fit);

		if (fit.getSigmaX() != null) {
			System.err.println("Fitting with X Errors");
			new LinearExyFit(fit);
		}
		else {
			new LineFit(fit);
		}
	}

	/**
	 * A Polynomial fit with errors
	 * 
	 * @param fit the fit data
	 */
	public static void fitPoly(Fit fit) {
		fixNullSig(fit);
		new PolyFit(fit); // errors
	}

	/**
	 * An alternate polynomial fit with errors
	 * 
	 * @param fit the fit data
	 */
	public static void fitAltPoly(Fit fit) {
		fixNullSig(fit);
		new AltPolynomialFit(fit); // errors
	}

	/**
	 * Where the fitting is actually done
	 * 
	 * @param fit
	 * @param className
	 */
	// public static void doFit(Fit fit, String className) {
	// fixNullSig(fit);
	// Class<?> clazz;
	// try {
	// clazz = Class.forName(className);
	// Constructor<?> ctor = clazz.getConstructor(Fit.class);
	// Object object = ctor.newInstance(new Object[] { fit });
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// } catch (NoSuchMethodException e) {
	// e.printStackTrace();
	// } catch (SecurityException e) {
	// e.printStackTrace();
	// } catch (InstantiationException e) {
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// e.printStackTrace();
	// } catch (IllegalArgumentException e) {
	// e.printStackTrace();
	// } catch (InvocationTargetException e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Erf fit with errors
	 * 
	 * @param fit the fit data
	 */
	public static void fitErf(Fit fit) throws IllegalArgumentException {
		fixNullSig(fit);
		new ErfFit(fit); // errors
	}

	/**
	 * Ercf fit with errors
	 * 
	 * @param fit the fit data
	 */
	public static void fitErfc(Fit fit) throws IllegalArgumentException {
		fixNullSig(fit);
		new ErfcFit(fit); // errors
	}

	/**
	 * A Gaussian fit with errors
	 * 
	 * @param fit the fit data
	 */
	public static void fitGaussians(Fit fit) throws IllegalArgumentException {
		System.err.println("Fitting Gaussians");
		fixNullSig(fit);
		GaussianFit gf = new GaussianFit(fit);
		if (fit.getFit() == null) {
			System.err.println("Null fit in fitGausians");
			System.err.println("gf = " + gf);
		}
	}

	/**
	 * A Gaussian and polynomial fit with errors
	 * 
	 * @param fit the fit data
	 */
	public static void fitGaussPlusPoly(Fit fit) throws IllegalArgumentException {
		fixNullSig(fit);
		new PolyAndGaussianFit(fit); // errors
	}

	/**
	 * Checks whether an array is sorted in ascending order
	 * 
	 * @param val the array to check
	 * @return <code>true</code> if the array is sorted
	 */
	public static boolean isSorted(double val[]) {
		if ((val == null) || (val.length < 1)) {
			return true;
		}

		for (int i = 1, n = val.length; i < n; i++) {
			if (val[i - 1] > val[i])
				return false;
		}
		return true;
	}

	/**
	 * Index sort of an array
	 * 
	 * @param val the array to sort
	 * @return the sorted index array
	 */
	public static Integer[] indexSort(final double val[]) {
		if ((val == null) || (val.length < 1)) {
			return null;
		}
		Integer iarry[] = new Integer[val.length];
		for (int i = 0; i < val.length; i++) {
			iarry[i] = i;
		}

		Comparator<Integer> comp = new Comparator<Integer>() {

			@Override
			public int compare(Integer i0, Integer i1) {
				return Double.compare(val[i0], val[i1]);
			}

		};
		Arrays.sort(iarry, comp);
		return iarry;
	}

	/**
	 * Sort a set of parallel arrays. The first one is the one that will end up
	 * sorted. Does nothing if the first array is sorted.
	 * 
	 * @param da a set of arrays
	 */
	public static void parallelSort(double[]... da) {
		if (isSorted(da[0])) {
			return; // done
		}

		Integer iarry[] = indexSort(da[0]);
		for (double[] darry : da) {
			rearranged(iarry, darry);
		}
	}

	/**
	 * Rearrange an array based on an index array
	 * 
	 * @param iarry the index array
	 * @param vals  the array that will be rearranged
	 */
	private static void rearranged(Integer iarry[], double vals[]) {
		double[] newvals = new double[vals.length];

		for (int i = 0; i < vals.length; i++) {
			newvals[i] = vals[iarry[i]];
		}
		for (int i = 0; i < vals.length; i++) {
			vals[i] = newvals[i];
		}
	}

	/**
	 * Compute the "standard" standard deviation (divide variance by N) using an
	 * accurate one-pass method.
	 * 
	 * @param x the data
	 * @return the standard deviation
	 */
	public static double standardDev(double x[]) {
		if ((x == null) || (x.length == 0)) {
			return Double.NaN;
		}

		int n = x.length;
		if (n == 1) {
			return 0;
		}

		double m = x[0];
		double q = 0;
		for (int k = 2; k <= n; k++) {
			double fac = (x[k - 1] - m);
			double fac2 = fac / k;
			m = m + fac2;
			q = q + (k - 1) * fac * fac2;
		}
		double var = q / n;

		if (var <= 0.) {
			return 0.0;
		}
		return Math.sqrt(var);

	}

	/**
	 * Get the mean, min and max of an array.
	 * 
	 * @param v the array
	 * @return a double[3] containing, in order, the mean, min and max
	 */
	public static double[] stats(double v[]) {

		double result[] = { Double.NaN, Double.NaN, Double.NaN };

		if ((v != null) && (v.length > 0)) {

			double sum = 0;
			double vmin = Double.POSITIVE_INFINITY;
			double vmax = Double.NEGATIVE_INFINITY;
			for (double val : v) {
				sum += val;
				vmin = Math.min(vmin, val);
				vmax = Math.max(vmax, val);
			}

			result[0] = sum / v.length; // mean
			result[1] = vmin;
			result[2] = vmax;
		}
		return result;
	}

	/**
	 * Another stats convenience method
	 * 
	 * @param v the input array
	 * @return a double[4] containing, in order, the mean, min, max, and standard
	 *         deviation
	 */
	public static double[] muSigma(double v[]) {
		double result0[] = stats(v);
		double result[] = new double[4];
		for (int i = 0; i < 3; i++) {
			result[i] = result0[i];
		}
		result[3] = standardDev(v);
		return result;
	}

	/**
	 * Get a weighted x mean
	 * 
	 * @param x the x values
	 * @param y the x values
	 * @return the weighted x mean
	 */
	public static double[] getWeightedXAverage(double x[], double y[]) {
		// average will be in 0, stddev in 1
		double result[] = { Double.NaN, Double.NaN };

		int n = Math.min(x.length, y.length);

		if (n == 0) {
			return result;
		}
		else if (n == 1) {
			result[0] = x[0];
			result[1] = 0;
			return result;
		}

		double sumX = 0.;
		double sumY = 0.;
		double sumXY = 0.;
		double xy[] = new double[n];
		for (int i = 0; i < n; i++) {
			double yabs = Math.abs(y[i]);
			sumX += x[i];
			sumY += yabs;
			xy[i] = x[i] * yabs;
			sumXY += xy[i];
		}

		if (sumY < 1.0e-20) {
			result[0] = sumX / n;
			result[1] = standardDev(x);
		}
		result[0] = sumXY / sumY;
		result[1] = n * standardDev(xy) / sumY;
		return result;
	}

	/**
	 * This deals with missing or bad error data. If the sigma array is null, it
	 * creates a new array and assigns a value of 1 to each error. It does the same
	 * if the sigma array has all zero values. The real troubling case is when sigma
	 * has some zero values. This will cause many fits to fail. So in that case it
	 * repleaces all zero values with the mean value. At that point the fit should
	 * be considered suspect.
	 * 
	 * @param sig the sigma array, which might be null
	 * @param n   the length of the array, if it has to be created
	 * @return a hopefully safe sigma array
	 */
	protected static void fixNullSig(Fit fit) {

		double sigY[] = fit.getSigmaY();
		double sigX[] = fit.getSigmaX();

		boolean hasXErr = (sigX != null);
		boolean hasYErr = (sigY != null);

		if (hasXErr && hasYErr) {
			fit.setErrorType(Fit.ErrorType.XY_Errors);
		}
		else if (hasXErr && !hasYErr) {
			fit.setErrorType(Fit.ErrorType.X_Errors);
		}
		else if (!hasXErr && hasYErr) {
			fit.setErrorType(Fit.ErrorType.Y_Errors);
		}
		else {
			fit.setErrorType(Fit.ErrorType.No_Errors);
		}

		if (sigY == null) {
			int n = fit.getX().length;
			sigY = new double[n];
			for (int i = 0; i < n; i++) {
				sigY[i] = 1;
			}

			fit.setSigmaY(sigY);
		}

		else {
			// just check there are n0 zero errors
			double result[] = stats(sigY);
			double mean = result[0];

			// zero errors will cause the fit to fail.
			// Use a minimum error based on mean error
			double minsig = 1.0e-8 * mean;
			for (int i = 0; i < sigY.length; i++) {
				if (sigY[i] < minsig) {
					sigY[i] = mean;
					fit.setErrorType(Fit.ErrorType.Massaged_Zeroes);
				}
			}
		}

		// don't give it x errors if missing, but do fix zeroes
		if (hasXErr) {
			double result[] = stats(sigX);
			double mean = result[0];

			// zero errors will cause the fit to fail.
			// Use a minimum error based on mean error
			double minsig = 1.0e-8 * mean;
			for (int i = 0; i < sigX.length; i++) {
				if (sigX[i] < minsig) {
					sigX[i] = mean;
					fit.setErrorType(Fit.ErrorType.Massaged_Zeroes);
				}
			}
			parallelSort(fit.getX(), fit.getY(), fit.getSigmaX(), fit.getSigmaY());
		}
		else {
			parallelSort(fit.getX(), fit.getY(), fit.getSigmaY());
		}

	}

}
