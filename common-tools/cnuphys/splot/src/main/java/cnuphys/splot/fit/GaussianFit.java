package cnuphys.splot.fit;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.exp;

import java.awt.Point;

import com.nr.model.FGauss;
import com.nr.model.Fitmrq;

/**
 * For fitting a sum of Gaussians to a curve (DataColumn)
 * 
 * @author heddle
 *
 */
public class GaussianFit extends Fitmrq implements IValueGetter {

	// function that evaluates the Gaussians
	private static FGauss _funcs = new FGauss();

	/**
	 * Create a Gaussian Fit
	 * 
	 * @param fit       the fit data
	 * @param tolerance the tolerance
	 */
	public GaussianFit(Fit fit) throws IllegalArgumentException {
		super(fit.getX(), fit.getY(), fit.getSigmaY(), initialGuess(fit), _funcs, fit.getTolerance());
		// set holds, then fit
		fit.setFitType(FitType.GAUSSIANS);
		for (FitHold fh : fit.getHolds()) {
			hold(fh.index, fh.value);
		}
		fit();
		fit.setFit(this);
	}

	private static final double TINY = 1.0e-20;

	private static boolean tinyDiff(double x1, double x2) {
		return Math.abs(x2 - x1) < TINY;
	}

	/**
	 * A method to generate an initial guess
	 * 
	 * @param xx       the x data
	 * @param yy       the y data
	 * @param numGauss the number of gaussians
	 * @return
	 */
	public static double[] initialGuess(Fit fit) {

		// System.err.println("INIT GUESS");

		double xx[] = fit.getX();
		double yy[] = fit.getY();

		// get rid of long tails
		int start = 0;
		int stop = xx.length - 1;

		double y = yy[0];

		for (int j = 1; j < xx.length; j++) {
			if (!tinyDiff(y, yy[j])) {
				start = Math.max(0, j - 2);
				break;
			}
		}

		for (int j = xx.length - 2; j >= 0; j--) {
			if (!tinyDiff(y, yy[j])) {
				stop = Math.min(xx.length - 1, j + 2);
				break;
			}
		}

		double xxx[] = xx;
		double yyy[] = yy;
		if ((start > 0) || (stop < (xx.length - 1))) {
			// System.err.println("Trimming");
			int length = (stop - start + 1);
			xxx = new double[length];
			yyy = new double[length];
			System.arraycopy(xx, start, xxx, 0, length);
			System.arraycopy(yy, start, yyy, 0, length);
		}

		int len = yyy.length;
		double yabs[] = new double[len];

		for (int i = 0; i < len; i++) {
			yabs[i] = Math.abs(yyy[i]);
		}

		int numGauss = fit.getNumGaussian();
		CubicSpline cs = new CubicSpline(xxx, yabs);

		// the algorithm. The method findXValsOfMaxima will fit a cubic to
		// the data, which is good at fitting the peaks. It will then find
		// and return the x values and y values of the peaks in descending
		// order,
		// i.e. pp[0] is the x and y of the highest maximum. It will then use
		// that info
		// as a very good guess of the gaussian means ()
		Point.Double pp[] = cs.findXValsOfMaxima(xxx[0], xxx[xxx.length - 1]);

		// lets hope this doesn't happen
		if ((pp == null) || (pp.length < numGauss)) {
			System.err.println("Gaussian initial guess algorithm failed.");
			fit.setNumGaussian(numGauss);
			return secondaryInitialGuess(fit);
		}

		int ng3 = 3 * numGauss;
		double aa[] = new double[ng3];

		double stats[] = getWeightedcStdDev(xxx, yyy);

		// re cubic w/o abs vals
		cs = new CubicSpline(xxx, yyy);

		for (int i = 0; i < numGauss; i++) {
			int j = 3 * i;
			// aa[j] = pp[i].y;
			aa[j] = cs.value(pp[i].x);

			// System.err.println(pp[i].x + " " + pp[i].y + " " +
			// cs.value(pp[i].x) + " " + stats[1]);
			aa[j + 1] = pp[i].x;
			// aa[j+2] = 1.0;
			aa[j + 2] = stats[1] / 2;
		}

		return aa;
	}

	/**
	 * A method to generate an initial guess
	 * 
	 * @param xx       the x data
	 * @param yy       the y data
	 * @param numGauss the number of gaussians
	 * @return
	 */
	public static double[] secondaryInitialGuess(Fit fit) {
		System.err.println("trying secondary initial guess got Gaussians");
		fit.setPolynomialOrder(fit.getNumGaussian() + 3);
		FitUtilities.fitPoly(fit);
		Polynomial poly = new Polynomial(((PolyFit) (fit.getFit())).a);

		// the algorithm. The method findXValsOfMaxima will fit a polynomial to
		// the data, which is usually good at fitting the peaks. It will then
		// find
		// and return the x values and y values of the peaks in descending
		// order,
		// i.e. pp[0] is the x and y of the highest maximum. It will then use
		// that info
		// as a very good guess of the gaussian means ()
		double xx[] = fit.getX();
		Point.Double pp[] = poly.findXValsOfMaxima(xx[0], xx[xx.length - 1]);

		// lets hope this doesn't happen
		if ((pp == null) || (pp.length < fit.getNumGaussian())) {
			System.err.println("Gaussian secondary guess algorithm failed.");
			return tertiaryInitialGuess(fit);
		}

		int ng3 = 3 * fit.getNumGaussian();
		double aa[] = new double[ng3];

		for (int i = 0; i < fit.getNumGaussian(); i++) {
			int j = 3 * i;
			aa[j] = pp[i].y;
			aa[j + 1] = pp[i].x;
			aa[j + 2] = 1.0;
		}

		return aa;
	}

	// a less reliable initial guess strategy bases on some statistics
	private static double[] tertiaryInitialGuess(Fit fit) {
		System.err.println("trying tertiary initial guess got Gaussians");

		double xx[] = fit.getX();
		double yy[] = fit.getY();
		int numGauss = fit.getNumGaussian();
		int ng3 = 3 * numGauss;
		double result[] = FitUtilities.getWeightedXAverage(xx, yy);

		double ypeak = 0.0;
		for (int i = 0; i < yy.length; i++) {
			ypeak = Math.max(ypeak, Math.abs(yy[i]));
		}

		double aa[] = new double[ng3];
		if (!Double.isNaN(result[0])) {
			double delX = result[1] / (numGauss + 1);
			double xmin = result[1] - delX;
			for (int i = 0; i < ng3; i += 3) {
				aa[i] = ypeak;
				aa[i + 1] = xmin + delX * (i / 3);
				aa[i + 2] = result[1] / numGauss;
			}
		}
		else {
			for (int i = 0; i < ng3; i++) {
				aa[i] = 1.;
			}
		}

		return aa;
	}

	/**
	 * Get the fit parameters
	 * 
	 * @return the fit parameters
	 */
	public double[] getFitParameters() {
		return a;
	}

	/**
	 * Get the covariance matrix
	 * 
	 * @return the covariance matrix
	 */
	public double[][] getCovarianceMatrix() {
		return covar;
	}

	/**
	 * Get the chi square of the fit
	 * 
	 * @return the chi square
	 */
	public double getChiSquare() {
		return chisq;
	}

	// public void funk(final double x, final double[]a, final doubleW y, final
	// double[] dyda){

	@Override
	public double value(double x) {
		int na = a.length;
		double ex, arg;
		double y = 0.;
		for (int i = 0; i < na - 1; i += 3) {
			arg = (x - a[i + 1]) / a[i + 2];
			ex = exp(-SQR(arg));
			y += a[i] * ex;
		}
		return y;
	}

	/**
	 * Get the means and standard deviation
	 * 
	 * @return an array with the mean in the 0 index and standard deviation is the 1
	 *         index
	 */
	private static double[] getWeightedcStdDev(double xx[], double yy[]) {

		double stats[] = new double[2];
		stats[0] = Double.NaN;
		stats[1] = Double.NaN;

		int n = xx.length;

		double sum = 0;
		double sumsq = 0;
		double ysum = 0;
		for (int i = 0; i < n; i++) {
			double yabs = Math.abs(yy[i]);
			double x = xx[i];
			double wx = yabs * x;
			sum += wx;
			sumsq += x * wx;
			ysum += yabs;
		}

		double avgSq = sumsq / ysum;

		stats[0] = sum / ysum; // mean
		stats[1] = Math.sqrt(avgSq - (stats[0] * stats[0])); // stdDev

		return stats;
	}

}
